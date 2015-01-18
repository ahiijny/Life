import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

/** The class representing the JPanel that contains
 * and handles the Colony class.
 * 
 * @author Jiayin
 * @date 2013-12-12
 */
public class LifePlane extends JPanel 
{
	public Colony colony;

	private Color background = new Color (128, 128, 128);

	private int prevRow;
	private int prevCol;

	private boolean setMode = true;
	private boolean selected = false;
	private boolean selecting = false;
	private boolean moving = false;

	private GeneralPath select;

	private Point click;
	private Point selectCurrent;
	private Point start;

	private GraphicUI gui;

	/** Creates a new LifePlane.
	 * 
	 * @param parent	the GraphicUI object that parents this LifePlane
	 */
	public LifePlane (GraphicUI parent)
	{		
		gui = parent;		
		setBackground (background);
		colony = new Colony (0, 250, 150);

		addMouseListener (new MyMouseListener ());
		addMouseMotionListener (new MyMouseListener ());
		addMouseWheelListener (new MyMouseListener());
	}

	/** Sets the colony offset such that the center of
	 * the grid is positioned at the center of this panel. 
	 */
	public void centerOnGrid ()
	{
		Dimension grid = colony.getSize();
		Dimension panel = getSize();
		int zoom = colony.getZoom();
		colony.offset.x = panel.width / 2 - grid.width * zoom / 2;
		colony.offset.y = panel.height / 2 - grid.height * zoom / 2;
	}




	/** Completely eradicates any living cells
	 * contained within the area specified by the
	 * select shape. Does nothing if there is currently
	 * no selection.
	 * 
	 * @return true if the eradication was carried out;
	 * false if no selection currently exists 
	 */	
	public boolean eradicateSelection ()
	{
		return eradicateSelection (1.0);
	}

	/** Eradicates any living cells contained within the 
	 * area specified by the select shape. The probability 
	 * of success of each individual eradication is specified
	 * by the given efficiency value. 1.0 specifies certain success.
	 * 0.0 specifies certain failure. Does nothing if there is currently no selection.
	 * 
	 * @param efficiency	he probability of any individual cell being successfully eradicated
	 * @return true if the eradication was carried out;
	 * false if no selection currently exists 
	 */	
	public boolean eradicateSelection (double efficiency)
	{
		boolean successful = false;
		if (selected)
		{
			successful = true;
			colony.eradicate(select, efficiency);
			cancelSelection ();
		}	
		return successful;
	}

	/** Completely populates any living cells
	 * contained within the area specified by the
	 * select shape. Does nothing if there is currently
	 * no selection.
	 * 
	 * @return true if the populates was carried out;
	 * false if no selection currently exists 
	 */	
	public boolean populateSelection ()
	{
		return populateSelection (1.0);
	}

	/** Populates any living cells contained within the 
	 * area specified by the select shape. The probability 
	 * of success of each individual population is specified
	 * by the given efficiency value. 1.0 specifies certain success.
	 * 0.0 specifies certain failure. Does nothing if there is currently no selection.
	 * 
	 * @param efficiency	he probability of any individual cell being successfully populated
	 * @return true if the population was carried out;
	 * false if no selection currently exists 
	 */	
	public boolean populateSelection (double efficiency)
	{
		boolean successful = false;
		if (selected)
		{
			successful = true;
			colony.populate(select, efficiency);
			cancelSelection ();
		}		
		return successful;
	}

	/** Draws the colony onto the panel to the offset specified
	 * in the colony object.	
	 */
	@Override
	public void paintComponent (Graphics g)
	{
		super.paintComponent(g);
		colony.show (g, getSize());			

		if (selecting || selected)
		{
			GeneralPath preview = new GeneralPath (select);
			if (selectCurrent != null) // draw preview selection of mouse hover, if valid
				preview.lineTo(selectCurrent.x, selectCurrent.y);

			colony.showSelection(g, preview);
		}

		if (colony.getZoom() > 2) // only draw grids if zoom is greater than 2
			colony.showGrid (g, getSize());		
	}	

	/** Sets the background color of this panel
	 * to the specified color.
	 * 
	 * @param bg	the new background color
	 */
	@Override
	public void setBackground (Color bg)
	{
		super.setBackground (bg);
		background = bg;
	}

	/** Sets the colony zoom to the specified value.
	 * Sets the colony offset such that that the cell 
	 * at the center of the screen before the zoom change
	 * remains at the center of the screen after the zoom. 
	 * 
	 * @param zoom	the new zoom value
	 */
	public void setZoom (int zoom)
	{
		setZoom (new Point (getWidth() / 2, getHeight() / 2), zoom);
	}

	/** Sets the colony zoom to the specified value.
	 * Sets the colony offset such that that the cell 
	 * at the specified location on the panel before 
	 * the zoom change remains at the same location on
	 * the screen after the zoom. 
	 * 
	 * @param focus		the anchor point for the zoom, in pixels
	 * @param zoom		the new zoom value
	 */
	public void setZoom (Point focus, int zoom)
	{
		if (zoom > 0)
		{
			double scale = zoom * 1.0 / colony.getZoom();
			int dx = (int) ((focus.x - colony.offset.x) - scale * (focus.x - colony.offset.x));
			int dy = (int) ((focus.y - colony.offset.y) - scale * (focus.y - colony.offset.y));
			colony.setZoom (zoom);
			colony.offset.translate(dx, dy);
			repaint();
		}
	}

	/** Enables selection mode; Starts a new selection.  
	 */
	public void startSelection ()
	{
		select = new GeneralPath ();
		selecting = true;
		selected = false;
	}

	/** Finalizes the current selection, thereby
	 * allowing it to be used for processes such as
	 * population or eradication.
	 */
	public void finalizeSelection ()
	{
		cancelSelection ();		
		selected = true;
		repaint ();
	}

	/** Disables selection mode; Terminates the current selection 
	 *process if  a selection process is ongoing. Also sets the
	 * selection thus far to null, thereby removing it from the
	 * screen and preventing it from being used for anything. 
	 */
	public void cancelSelection ()
	{
		if (select.getCurrentPoint() != null)
			select.closePath ();
		selecting = false;
		selected = false;
		start = null;
		selectCurrent = null;
		repaint ();
	}
	
	public GeneralPath getSelection ()
	{
		return select;
	}

	/** Enables moving mode. Sets it so that clicking 
	 * and dragging will navigate around the colony, 
	 * rather than adding or removing live cells. 
	 */
	public void startMoving ()
	{
		moving = true;
	}

	/** Called by the MouseListener for this panel.
	 * Updates the current colony offset by comparing 
	 * the current mouse location and the location of 
	 * the last checked mouse location. Does nothing if 
	 * currently not in moving mode. 
	 * 
	 * @param now	the current location of the mouse
	 */
	public void updateMove (Point now)
	{
		if (moving)
		{
			int dx = now.x - click.x;
			int dy = now.y - click.y;

			click = now;
			colony.offset.translate(dx, dy);
		}
	}

	/** Disables moving mode. Sets it so that clicking
	 * and dragging will add and remove live cells, 
	 * rather than navigating around the colony. 
	 * 
	 */
	public void stopMoving ()
	{
		moving = false;
	}

	/** Class for MouseEvents. This class receives
	 * and acts upon various MouseEvents.
	 */
	private class MyMouseListener extends MouseAdapter
	{	
		public void mousePressed (MouseEvent e)
		{
			click = e.getPoint();		
			int row = colony.getRow (click.y);
			int col = colony.getCol (click.x);

			if (!moving) // if not in moving mode
			{
				if (!selecting) // if not in selection mode
				{
					if (colony.rowValid(row) && colony.colValid(col)) // do normal bit flip						 
					{
						colony.flipCell (row, col);
						setMode = colony.getCell(row, col);
					}
				}
				else // if currently in selection mode
				{							
					if (start == null) // if selection currently has no points
					{
						// Initialize initial point
						select.moveTo (col, row);
						Point2D temp = select.getCurrentPoint();
						start = new Point ((int)temp.getX (), (int)temp.getY());
					}
					else 
					{
						// Finalize selection if last clicked cell equals the current selected cell;
						// i.e. Quit selection mode if double clicked.s
						Point2D temp = select.getCurrentPoint();
						Point cell = new Point (col, row);
						if (cell.equals(start) || cell.equals(temp))
							finalizeSelection ();	
						else
							select.lineTo (col, row);											
					}					
				}
				prevRow = row;
				prevCol = col;	

				repaint();
			}
		}		

		public void mouseDragged (MouseEvent e)
		{	
			Point now = e.getPoint();

			if (moving) // if in moving mode
				updateMove (now);	
			else // if not in moving mode
			{
				int row = colony.getRow (now.y);
				int col = colony.getCol (now.x);

				if (!selecting) // if not in selection mode
				{
					if (colony.rowValid (row) && colony.colValid(col)) // if valid
					{
						if (row != prevRow || col != prevCol) // if this is a different cell
						{
							prevRow = row;
							prevCol = col;			
							
							// Flip cell state, if necessary, so that this cell's state
							// becomes equal to the state of the cell of the original click

							if (colony.getCell(row, col) != setMode)							
								colony.flipCell (row, col);						
						}
					}					
				}
				else // if in selection mode
				{							
					select.lineTo (col, row);
					selectCurrent = new Point (col, row);											
				}
			}	
			repaint();
		}

		public void mouseMoved (MouseEvent e)
		{
			Point now = e.getPoint ();
			int row = colony.getRow (now.y);
			int col = colony.getCol (now.x);

			if (selecting && start != null) // if in selection mode
			{
				selectCurrent = new Point (col, row); // for preview selection
				repaint ();
			}		
		}

		public void mouseWheelMoved (MouseWheelEvent e)
		{
			// Increase or decrease zoom value, as necessary
			
			int newZoom = -e.getWheelRotation() + colony.getZoom ();
			gui.zoom.setValue (newZoom);
			setZoom (e.getPoint(), newZoom);
			repaint();
		}
	}
}
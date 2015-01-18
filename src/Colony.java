import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

/** The life representing the grid
 * of alive or dead cells.
 * 
 * @author Jiayin Huang
 * @date CE 2013-12-12
 */
public class Colony 
{
	private boolean grid [][];	

	public Point offset = new Point ();

	public Color dead = Color.black;	
	public Color alive = Color.green;
	public Color deadSelect = Color.blue;
	public Color aliveSelect = Color.yellow;	
	public Color gridLines = new Color (128, 128, 128);	

	private int iteration = 0;
	private int zoom = 7;

	/** Creates a colony of the specified density.
	 * A density of 1.0 or greater will set all of the cells to alive.
	 * A density of 0.0 or less will set all of the cells to dead.
	 * 
	 * @param density		the probability of any individual cell being alive
	 */
	public Colony (double density)
	{		
		this (density, 100, 100); // default size is 100 x 100
	}

	/** Creates an empty colony with the
	 * specified dimensions.
	 * 
	 * @param width		the width of the colony
	 * @param height	the height of the colony
	 */
	public Colony (int width, int height)
	{
		this (0, width, height);
	}		

	/** Creates a colony of the specified density and 
	 * dimensions.
	 * 
	 * @param density		the probability of a cell being alive
	 * @param width			the width of the colony
	 * @param height		the height of the colony
	 */
	public Colony (double density, int width, int height)
	{
		grid = new boolean [height] [width];	

		if (density > 0)
			populate (density);
	}

	/** Advances the colony by one iteration, or generation. 
	 */
	public void advance ()
	{		
		boolean[][] temp = new boolean [grid.length][grid[0].length];

		for (int i = 0; i < grid.length; i++)		
			for (int j = 0; j < grid[0].length; j++)
				temp[i][j] = live (i, j);

		grid = temp;
		iteration++;
	}


	/** Attempts to eliminate all of the cells within the bounds of the 
	 * given shape. In the given shape, x represents the column and y
	 * represents the row. Any coordinates that are not within the bounds
	 * of this colony are ignored.
	 * 
	 * The efficiency states the probability that any cell will be
	 * successfully eradicated. 1.0 indicates complete extermination
	 * within the given shape. 0.0 indicates the change of absolutely 
	 * nothing.
	 * 
	 * @param select		the shape within which to eradicate cells
	 * @param efficiency	the probability of any individual cell being successfully eradicated
	 */
	public void eradicate (Shape select, double efficiency)
	{
		Rectangle bound = select.getBounds();
		int left = bound.x;
		int right = bound.x + bound.width;
		int up = bound.y;
		int down = bound.y + bound.height;

		for (int col = left; col < right; col++)		
			for (int row = up; row < down; row++)			
				if (select.contains(col, row))
					if (colValid (col) && rowValid (row))
						if (Math.random () < efficiency)
							grid[row][col] = false;			
	}

	/** Attempts to populate all of the cells within the bounds of the 
	 * given shape. In the given shape, x represents the column and y
	 * represents the row. Any coordinates that are not within the bounds 
	 * of this colony are ignored.
	 * 
	 * The efficiency states the probability that any cell will be
	 * successfully populated. 1.0 indicates certainty. 0.0
	 * indicates definite failure.
	 * 
	 * @param select		the shape within which to populate cells
	 * @param efficiency	the probability of any individual cell being successfully populated
	 */
	public void populate (Shape select, double efficiency)
	{
		Rectangle bound = select.getBounds();
		int left = bound.x;
		int right = bound.x + bound.width;
		int up = bound.y;
		int down = bound.y + bound.height;

		for (int col = left; col < right; col++)		
			for (int row = up; row < down; row++)			
				if (select.contains(col, row))
					if (colValid (col) && rowValid (row))
						if (Math.random () < efficiency)
							grid[row][col] = true;				
	}

	/** Populates the entire colony based on the specified weight factor.
	 * A density of 1.0 will set all of the cells to alive.
	 * A density of 0.0 will set all of the cells to dead.
	 * 
	 * @param density	the probability of a cell being alive
	 */
	public void populate (double density)
	{		
		for (int row = 0 ; row < grid.length ; row++)
			for (int col = 0 ; col < grid [0].length ; col++)
				grid [row] [col] = Math.random () < density;
	}	

	/** Inverts the state of the cell at the specified location.
	 * If it was alive, it will be set to dead. If it was dead,
	 * it will be set to alive.
	 * 
	 * @param row	the row of the cell
	 * @param col	the column of the cell 
	 * @throws IndexOutOfBoundsException if the column or
	 * row is out of bounds
	 */
	public void flipCell (int row, int col)
	{
		grid[row][col] = !grid[row][col];
	}

	/** Gets the cell at the specified location.
	 * 
	 * @param row	the row of the cell
	 * @param col	the column of the cell
	 * @return true if the cell is alive; false otherwise
	 * @throws IndexOutOfBoundsException if the column or
	 * row is out of bounds
	 */
	public boolean getCell (int row, int col)
	{		
		return grid[row][col];
	}

	/** Calculates the index of the column that contains 
	 * the given x-coordinate, using the colony's current 
	 * x-offset value.
	 *  
	 * @param x			the x-coordinate
	 * @return the column index
	 */
	public int getCol (int x)
	{
		return (x - offset.x) / zoom; 
	}

	/** Gets the number of rows in this colony.
	 * 
	 * @return the height of this colony
	 */
	public int getHeight ()
	{
		return grid.length;
	}

	/** Gets the current iteration, or generation,
	 * of this colony.
	 * 
	 * @return the current iteration
	 */
	public int getItt ()
	{
		return iteration;
	}

	/** Calculates the index of the row that contains 
	 * the given y-coordinate, using the colony's current 
	 * y-offset value.
	 * 	 
	 * @param y			the y-coordinate
	 * @return the row index
	 */
	public int getRow (int y)
	{
		return (y - offset.y) / zoom; 
	}

	/** Returns the dimensions of this colony in terms of width and height.
	 * In other words, the number of columns by the number of rows.
	 * 
	 * @return the size of this colony
	 */
	public Dimension getSize ()
	{
		return new Dimension (grid[0].length, grid.length);
	}

	/** Returns the number of columns in this colony.
	 * 
	 * @return the width of this colony
	 */
	public int getWidth ()
	{
		return grid[0].length;
	}

	/** Calculates the x-coordinate of the given column, using
	 * the current x-offset value.
	 *  
	 * @param col		the column index
	 * @return the x-coordinate
	 */
	public int getX (int col)
	{
		return (int)Math.round(col * zoom) + offset.x;
	}

	/** Calculates the y-coordinate of the given row, using 
	 * the current y-offset value.
	 * 	 
	 * @param row		the row index
	 * @return the y-coordinate
	 */
	public int getY (int row)
	{
		return (int)Math.round(row * zoom) + offset.y;
	}		

	
	/** Gets the current zoom for this colony.
	 * 
	 * @return the current zoom
	 */
	public int getZoom ()
	{
		return zoom;
	}

	/** Determines whether the given column is
	 * within the boundaries of this colony.
	 * 
	 * @param col	the column to be checked
	 * @return true if within bounds; false otherwise
	 */
	public boolean colValid (int col)
	{
		return col >= 0 && col < grid[0].length;
	}

	/** Determines whether the given row is
	 * within the boundaries of this colony.
	 * 
	 * @param row	the row to be checked
	 * @return true if within bounds; false otherwise
	 */
	public boolean rowValid (int row)
	{
		return row >= 0 && row < grid.length;
	}

	
	

	/** Determines whether the indicated cell will be alive
	 * in the next iteration, based on its surrounding cells.
	 * 
	 * @param row	the row of the cell
	 * @param col	the column of the cell
	 * @return true if the cell will live; false otherwise
	 */
	private boolean live (int row, int col)
	{
		int liveCounter = 0;		

		// Iterate through surrounding 3x3 grid

		for (int i = row - 1; i <= row + 1; i++)		
			for (int j = col - 1; j <= col + 1; j++)			
				if (rowValid(i) && colValid(j))				
					if (grid[i][j])
						liveCounter++;						

		// Subtract a count if the cell itself was counted

		if (grid[row][col])
			liveCounter--;

		return grid[row][col] && liveCounter == 2 || liveCounter == 3;
	}	

	/** Attempts to load all of the data structures and variables from
	 * the specified save String. Throws an exception if there is a 
	 * formatting error.
	 * 
	 * @param save	the correctly formated save String
	 * @return the last line processed
	 */
	public void load (String save) throws Exception
	{
		// Declaration of Variables

		String[] parameters = save.split("\n");		
		int line = 0;		
		int col = 0;

		// Iterate through save String

		try
		{
			for ( ; line < parameters.length; line++)
			{				
				String text = parameters[line];	
				if (!text.startsWith (";")) // Line starting with ";" are comments
				{
					if (text.startsWith("grid :")) // This is the grid portion
					{	
						line++;
						int index = line;
						int height = 0;

						// Find height of grid

						while (index < parameters.length && Character.isDigit(parameters[index].charAt(0)))
							index++;
						height = index - line;
						index = line;

						// Initialize new grid

						grid = new boolean [height][parameters[line].length()];

						// Iterate through rows of grid

						for (; line < index + height; line++)
						{
							String slice = parameters[line];	
							int row = line - index;

							// Iterate through columns of grid, setting cells to the indicated values

							for (col = 0; col < grid[row].length; col++)
								grid[row][col] = slice.charAt(col) == '1' ? true : false;
						}							
					}						
					else
						parseLine (text);
				}
			}	
		}
		catch (Exception e)
		{
			throw new Exception ("" + (line + 1) + "," + (col + 1) + " :\n" + e.getMessage());
		}
	}

	/** Called by the load method. This method interprets and acts upon 
	 * lines of data concerning variable assignments.
	 * 
	 * @param line			the line to be interpreted
	 * @throws Exception thrown if there was a parsing error; some possibilities
	 * include attempting to parse a letter to an integer, or trying to access a
	 * parameter that is out of bounds because there was an incorrect number of parameters
	 * in a line.
	 */
	private void parseLine (String line) throws Exception
	{
		String[] parameters = line.split(" ");

		// Zoom input: "zoom = [int]"
		if (parameters[0].equals ("zoom"))					
			zoom = Integer.parseInt(parameters[2]);

		// Iteration input: "iteration = [int]"
		else if (parameters[0].equals("iteration"))
			iteration = Integer.parseInt(parameters[2]);

		// Offset input: "offset = [int x] [int y]"
		else if (parameters[0].equals("offset"))
		{
			offset.x = Integer.parseInt (parameters[2]);
			offset.y = Integer.parseInt (parameters[3]);
		}

		// Color input: "colorName = [int red] [int green] [int blue]"
		else if (parameters.length == 5)
		{
			int r = Integer.parseInt (parameters[2]);
			int g = Integer.parseInt(parameters[3]);
			int b = Integer.parseInt(parameters[4]);

			Color color = new Color (r,g,b);			

			if (parameters[0].equals ("alive"))
				alive = color;
			else if (parameters[0].equals("dead"))
				dead = color;
			else if (parameters[0].equals("aliveSelect"))
				aliveSelect = color;
			else if (parameters[0].equals("deadSelect"))
				deadSelect = color;
			else if (parameters[0].equals("gridLines"))
				gridLines = color;
		}
		else
		{
			throw new Exception ("Unrecognized command");
		}
	}
	
	/** Sets the state of the cell at the specified location.
	 * 
	 * @param row		the row of the cell
	 * @param col	 	the column of the cell
	 * @param state		the state to which to set the cell
	 * @throws IndexOutOfBoundsException if the column or
	 * row is out of bounds
	 */
	public void setCell (int row, int col, boolean state)
	{		
		grid[row][col] = state;
	}

	/** Sets the current iteration, or generation.
	 * 
	 * @param itt	the new iteration value
	 */
	public void setItt (int itt)
	{
		iteration = itt;
	}

	/** Resizes the colony using (0,0) as an anchor point. Any
	 * existing cells that are within the bounds of the new colony
	 * grid will be copied over. Does nothing if any of the dimensions
	 * are less than or equal to 0.
	 * 
	 * @param size	the dimensions of the new colony
	 */
	public void setSize (Dimension size)
	{		
		if (size.width > 0 && size.height > 0)
		{
			if (size.width != getWidth() || size.height != getHeight())
			{
				boolean[][] temp = new boolean [size.height][size.width];
				for (int row = 0; row < temp.length && row < getHeight(); row++)
					for (int col = 0; col < temp[0].length && col < getWidth(); col++)
						temp[row][col] = grid[row][col];
				grid = temp;
			}
		}
	}

	/** Sets the zoom value. 
	 * Ignores values less than or equal to 0.
	 * 
	 * @param zoom	the new zoom value
	 */
	public void setZoom (int zoom)
	{
		if (zoom > 0)
			this.zoom = zoom;
	}

	/** Draws the colony on the specified Graphics object, using
	 * the colony's current offset value. Draws the colony only to
	 * the extent of the given dimensions. These dimensions are
	 * meant to be the size of the component to which the Graphics
	 * object belongs. This is to save on processing power by 
	 * only drawing the visible portion of the colony.
	 * 
	 * @param g			the Graphics context in which to paint
	 * @param size		the Dimensions in which to paint
	 */
	public void show (Graphics g, Dimension size)
	{       
		// Initialize bounds of drawing area

		int left = Math.max(getCol (0), 0);
		int right = Math.min(getCol (size.width) + 1, grid[0].length);
		int up = Math.max(getRow (0), 0);
		int down = Math.min (getRow (size.height) + 1, grid.length);

		// Iterate through grid selection and draw cells

		for (int row = up ; row < down ; row++)
		{
			int y = getY (row);
			for (int col = left ; col < grid [row].length && col < right; col++)
			{
				int x = getX (col);

				Color color = grid [row][col] ? alive : dead;
				g.setColor (color);                     
				g.fillRect (x, y, zoom, zoom); // draw life form
			}
		}
	} 

	/** Draws the grid lines for the colony. Draws the grid lines
	 * only up to the extent of the indicated dimensions.
	 * 
	 * @param g			the Graphics context in which to paint
	 * @param size		the Dimensions in which to paint
	 */
	public void showGrid (Graphics g, Dimension size)
	{	
		// Initialize bounds of drawing area

		int left = Math.max(getCol (0), 0);
		int right = Math.min(getCol (size.width) + 1, grid[0].length);
		int up = Math.max(getRow (0), 0);
		int down = Math.min (getRow (size.height) + 1, grid.length);

		int width = right - left;
		int height = down - up;
		
		g.setColor(gridLines);

		// Iterate through x-values and y-values, and draw grid lines

		for (int row = up; row < down; row++)           
			g.fillRect(getX(left), getY (row), width * zoom, 1);

		for (int col = left; col < right; col++)
			g.fillRect(getX (col), getY(up), 1, height * zoom);                 
	}


	/** Draws the cells that are contained within the bounds of the
	 * given shape. Uses the "aliveSelect" and "deadSelect" color
	 * schemes for these cells.
	 * 
	 * @param g			the Graphics context in which to paint
	 * @param select	the Shape representing the boundary of the selection
	 */
	public void showSelection (Graphics g, Shape select)
	{
		// Initialize bounds of checking area

		Rectangle bounds = select.getBounds ();
		int left = bounds.x;
		int up = bounds.y;
		int right = left + bounds.width;
		int down = up + bounds.height;

		// Iterate through grid selection and draw cells, if within shape

		for (int col = left; col < right; col++)
		{
			int x = getX (col);

			for (int row = up; row < down; row++)
			{			
				if (select.contains(col, row) && rowValid (row) && colValid(col))
				{
					int y = getY (row);
					Color color = grid[row][col] ? aliveSelect : deadSelect;
					g.setColor (color);
					g.fillRect (x, y, zoom, zoom); // draw life form										
				}
			}
		}
	}		

	/** Returns a String representation of the current state
	 * of this colony. The output of this method is used for
	 * loading and writing saves. 
	 * 
	 * Note that in the resulting String, all of the lines 
	 * are separated by a LF ("\n") character. However, in 
	 * Windows, CRLF ("\r\n") is usually used for a new line. 
	 * Thus, if this String were to be output to a file, and 
	 * that file were to be opened in Notepad, a user wouldn't 
	 * see any line breaks.
	 * 
	 * However, most other programs such as Wordpad and Notepad++
	 * interpret and display the new lines correctly. 
	 * 
	 * @return the String representation of this colony
	 */
	@Override
	public String toString ()
	{
		String str = "iteration = " + iteration + "\n";
		str += "offset = " + offset.x + " " + offset.y + "\n";
		str += "zoom = " + zoom + "\n";
		str += "alive = " + getStringRGB (alive) + "\n";
		str += "aliveSelect = " + getStringRGB (aliveSelect) + "\n";
		str += "dead = " + getStringRGB (dead) + "\n";		
		str += "deadSelect = " + getStringRGB (deadSelect) + "\n";
		str += "gridLines = " + getStringRGB (gridLines) + "\n";
		str += "grid :\n";
		for (int row = 0; row < grid.length; row++)
		{
			for (int col = 0; col < grid[0].length; col++)
				str += grid[row][col] ? "1" : "0";
			str += "\n";
		}
		return str;		
	}		

	/** Returns a String representation of a sRGB value
	 * in the format "[int red] [int green] [int blue]"
	 * 
	 * @param color		the color to codify
	 * @return the String representation of the color
	 */
	private String getStringRGB (Color color)
	{
		return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
	}
}

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/** The GUI for this implementation of Conway's Game of Life.
 * Contains a LifePlane object, which contains a Colony object,
 * which contains the actual grid of life forms. 
 * 
 * @author Jiayin
 * @date CE 2013-12-12
 */
public class GraphicUI extends JFrame 
{	
	public JPanel content;
	public LifePlane plane;

	private JLabel iteration;
	private JButton select;
	private JButton move;
	private JButton simulate;

	protected JSlider speed;
	protected JSlider efficiency;
	protected JSlider zoom;

	private JTextField _width;
	private JTextField _height;

	private KeyboardFocusManager manager;
	private MyDispatcher keyDispatcher;
	private Timer timer;

	private JTextArea controls;	

	private File directory = new File ("."); // initialize dir to current dir

	/** Creates a new GraphicUI of the specified title, width, 
	 * and height.
	 * 
	 * @param title		the title of the GUI
	 * @param width		the width of the GUI
	 * @param height	the height of the GUI
	 */
	public GraphicUI (String title, int width, int height)
	{
		super (title);	

		setDefaultCloseOperation (EXIT_ON_CLOSE);
		setExtendedState(MAXIMIZED_BOTH);
		setSize (width, height);		

		initContent ();			
		setContentPane (content);
		setVisible (true);
		plane.centerOnGrid();
		toggleShowControls();
	}

	/** Initializes the content pane, including the LifePlane and 
	 * the toolbar. Also initializes and sets a custom KeyEventDispatcher
	 * to deal with inputed keys.
	 */
	private void initContent ()
	{
		content = new JPanel (new BorderLayout ());
		plane = new LifePlane (this);		
		content.add (plane, "Center");	

		initToolBar ();			

		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyDispatcher = new MyDispatcher ();
		manager.addKeyEventDispatcher(keyDispatcher);	
	}

	/** Initializes the toolbar, including both rows of sliders and
	 * buttons. Adds the sliders, buttons, as well as action listeners 
	 * for each individual component as necessary.
	 */
	private void initToolBar ()
	{
		// Declaration of Variables

		ButtonListener buttonListener = new ButtonListener ();

		JPanel toolbars = new JPanel (new BorderLayout());		
		JPanel bar1 = new JPanel ();	
		JPanel bar2 = new JPanel ();
		bar1.setLayout (new FlowLayout (FlowLayout.LEFT));
		bar2.setLayout (new FlowLayout (FlowLayout.LEFT));

		JButton button;
		JLabel label;

		// TOP ROW:			
		// File Buttons

		button = new JButton ("Save");
		button.addActionListener (buttonListener);	
		bar1.add (button);

		button = new JButton ("Load");
		button.addActionListener (buttonListener);	
		bar1.add (button);	

		// Simulation Buttons

		button = new JButton ("Next");
		button.addActionListener (buttonListener);	
		bar1.add (button);

		simulate = new JButton ("Start Timer");
		simulate.addActionListener (buttonListener);	
		bar1.add (simulate);

		// Simulation Speed Slider

		label = new JLabel ("Speed:  Slow");
		bar1.add (label);

		speed = new JSlider (0, 9, 2);		
		speed.setMajorTickSpacing (2);
		speed.setMinorTickSpacing (1);
		speed.setPaintTicks (true);
		speed.setPreferredSize (new Dimension (150, 30));
		speed.addChangeListener(new SliderListener());				
		bar1.add(speed);

		label = new JLabel ("Fast             ");
		bar1.add (label);


		// Zoom slider

		label = new JLabel ("Zoom:  Small");
		bar1.add (label);

		zoom = new JSlider (1, 50, 7);
		zoom.setMajorTickSpacing (5);
		zoom.setMinorTickSpacing (1);
		zoom.setPaintTicks (true);
		zoom.setSnapToTicks(true);
		zoom.setPreferredSize (new Dimension (150, 30));
		zoom.addMouseListener(new MyMouseListener ());
		zoom.addMouseMotionListener(new MyMouseListener ());
		bar1.add(zoom);

		label = new JLabel ("Large");
		bar1.add (label);

		// Iteration Counter

		label = new JLabel ("                     Iteration: ");
		bar1.add (label);

		iteration = new JLabel ("" + plane.colony.getItt());
		bar1.add (iteration);


		// BOTTOM ROW:

		// Move Button

		move = new JButton ("Move");		
		move.addActionListener (buttonListener);	
		bar2.add (move);

		// Selection Buttons	

		select = new JButton ("Select");
		select.addActionListener (buttonListener);	
		bar2.add (select);

		button = new JButton ("Eradicate");
		button.addActionListener (buttonListener);	
		bar2.add (button);

		button = new JButton ("Populate");
		button.addActionListener (buttonListener);	
		bar2.add (button);

		// Eradicate and Populate Efficiency Slider

		label = new JLabel ("Efficiency:  0.0");
		bar2.add (label);

		efficiency = new JSlider (0, 1000, 1000); // Conversion factor = 0.001
		efficiency.setMajorTickSpacing (200);
		efficiency.setMinorTickSpacing (50);
		efficiency.setPaintTicks (true);
		efficiency.setPreferredSize (new Dimension (150, 30));								
		bar2.add(efficiency);

		label = new JLabel ("1.0           ");
		bar2.add (label);	
		
		// Colony Grid Size Text Fields

		label = new JLabel ("Width:");
		bar2.add (label);
		_width = new JTextField ("" + plane.colony.getWidth(), 3);		
		bar2.add (_width);

		label = new JLabel ("Height:");
		bar2.add (label);
		_height = new JTextField ("" + plane.colony.getHeight(), 3);		
		bar2.add (_height);

		button = new JButton ("Set Size");
		button.addActionListener (buttonListener);	
		bar2.add (button);

		button = new JButton ("Controls...");
		button.addActionListener (buttonListener);	
		bar2.add (button);


		// Putting Everything Together

		toolbars.add (bar1, "North");
		toolbars.add (bar2, "South");
		content.add(toolbars, "North");		
	}
	
	/** Advances the current iteration, or generation,
	 * of the colony by 1. Then, updates the UI, drawing
	 * the new colony state onto the screen. 
	 */
	public void advance ()
	{
		plane.colony.advance();
		updateUI ();
	}	
	
	/** The class the runs the advance() method. This
	 * class is used by the simulation timer.
	 */
	private class Advance extends TimerTask
	{
		@Override
		public void run() 
		{
			advance();
		}		
	}
	
	/** Eradicates the cells within the area bounded by
	 * the shape specified by the select Shape in the 
	 * LifePlane instance. The eradication efficiency
	 * is specified by the current value of the efficiency
	 * slider component. 
	 */
	public void eradicate ()
	{
		double density = efficiency.getValue() / 1000.0;

		boolean successful = plane.eradicateSelection (density);
		select.setText ("Select");
		if (!successful)
		{
			String message = "Please finish selecting an area first.\n(Double click to finalize a selection.)";
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(GraphicUI.this, message, "Eradicate", type);
		}
	}
	
	/** Populates the cells within the area bounded by
	 * the shape specified by the select Shape in the 
	 * LifePlane instance. The population efficiency
	 * is specified by the current value of the efficiency
	 * slider component. 
	 */
	public void populate ()
	{
		double density = efficiency.getValue() / 1000.0;

		boolean successful = plane.populateSelection (density);
		select.setText ("Select");
		if (!successful)
		{
			String message = "Please finish selecting an area first.\n(Double click to finalize a selection.)";
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(GraphicUI.this, message, "Populate", type);
		}
	}


	/** Opens a JFileChooser to select a text file. Attempts
	 * to load that text file into the Colony object contained
	 * by the LifePlane panel contained in this JFrame. Pops
	 * up an alert if there was an error in loading the file.
	 */
	public void loadFile ()
	{	
		// Set up JFileChooser
		
		JFileChooser fc = new JFileChooser ();		
		fc.setFileFilter(new FileNameExtensionFilter ("Text file (*.txt)", "txt"));		
		fc.setAcceptAllFileFilterUsed(false);		
		fc.setCurrentDirectory(directory);

		// Show JFileChooser dialog
		
		int result = fc.showOpenDialog(this);
		
		// Act upon JFileChooser result

		if (result == JFileChooser.APPROVE_OPTION)
		{
			File load = fc.getSelectedFile();
			directory = load;
			if (load.canRead())
			{
				try // try loading the file
				{					
					plane.colony.load(readSave (load));
				}
				catch (Exception e) // show error dialog
				{
					int type = JOptionPane.INFORMATION_MESSAGE;
					String message = "Error: Corrupt save file.\n";
					message += "Could not parse file at line and column " + e.getMessage ();					
					JOptionPane.showMessageDialog(this, message, "Error", type);
				}
			}
		}		
	}

	/** Opens a JFileChooser to select a location to save
	 * a text file. Gets the String representation of the
	 * current state of the colony, and writes that to the
	 * indicated path. This method prompts the user for an
	 * overwrite if a file of the indicated name already
	 * exists. 
	 */
	public void saveFile ()
	{		
		// Set up JFileChooser 
		
		JFileChooser fc = new JFileChooser ();
		fc.setFileFilter(new FileNameExtensionFilter ("Text file (*.txt)", "txt"));
		fc.setAcceptAllFileFilterUsed(false);
		fc.setCurrentDirectory(directory);

		// Show JFileChosoer dialog
		
		int result = fc.showSaveDialog(this);
		
		// Act upon JFileChooser result
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File save = fc.getSelectedFile();
			if (!save.getName().endsWith (".txt"))
				save = new File (save + ".txt");
			directory = save;

			if (save.canRead()) // prompt overwrite if file exists
			{
				String message = "Overwrite file?";
				int type = JOptionPane.YES_NO_OPTION;
				result = JOptionPane.showConfirmDialog(GraphicUI.this, message, "Save file", type);				
			}
			
			// Write the file

			if (result == JFileChooser.APPROVE_OPTION || result == JOptionPane.YES_OPTION)
				writeSave (save);
		}
	}
		

	/** Reads the file at the indicated path and returns
	 * a String representation of the contents of the file.
	 * 
	 * @param path	the file to be read
	 * @return the file's contents in String format
	 */
	public String readSave (File path)
	{
		// Declaration of Variables
		String save = "";
		BufferedReader reader = null;
		boolean reading = true;
		
		try	// try reading the file
		{
			reader = new BufferedReader (new FileReader (path));

			while (reading)
			{		
				String line = reader.readLine();				
				if (line != null)
					save += line + "\n";
				else
					reading = false;
			}

			reader.close();
		}
		catch (Exception e) // show error dialog
		{
			String message = "Error. Could not read file:" + path;
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(this, message, "Load", type);
		}
		return save;
	}

	/** Gets the String representation of the current state 
	 * of the colony, and writes that to the file at the 
	 * indicated path. The writer for this format encodes 
	 * the text in UTF-8 format.
	 * 
	 * @param path	the file to write to
	 */
	public void writeSave (File path)
	{
		BufferedWriter writer = null;

		try // try writing to the file
		{
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			writer.write(plane.colony.toString());
		} 
		catch (Exception e) // show error dialog
		{
			String message = "Error. Could not write file.";
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(this, message, "Save", type);
		} 
		finally 
		{
			try 
			{
				writer.close();
			} 
			catch (Exception ex) {}
		}
	}
	
	/** Attempts to resize the colony to the dimensions
	 * specified in the _width and _height JTextFields.
	 * Sets the text of the two JTextFields to the current
	 * dimensions of the colony if the text in either of 
	 * those JTextFields are invalid dimensions. 
	 */
	public void setSize ()
	{
		int width;
		int height;

		stopTimer();		

		try
		{
			width = Integer.parseInt(_width.getText());
			height = Integer.parseInt(_height.getText());	
			if (width <= 0 || height <= 0)
				throw new NumberFormatException ();						
			plane.colony.setSize(new Dimension (width, height));
			plane.centerOnGrid();
		}
		catch (NumberFormatException ex)
		{		
			width = plane.colony.getWidth();
			height = plane.colony.getHeight();
		}	

		_width.setText ("" + width);
		_height.setText ("" + height);
		updateUI();	
	}

	/** Toggles the state of the JTextArea that displays
	 * the list of keyboard and mouse controls. Removes the
	 * list from the contentPane if the list is currently visible.
	 * Adds the list to the contentPane if the list is not 
	 * currently visible.  
	 */
	public void toggleShowControls ()
	{
		if (controls == null)
		{	
			controls = new JTextArea ();
			controls.setBackground(new Color (238, 238, 238));
			controls.setPreferredSize(new Dimension (200, 600));			
			controls.setMargin(new Insets (2,5,5,2));
			controls.setEditable(false);

			controls.append("Mouse Functions:\n");
			controls.append("CLICK\tToggle Cell\n");	
			controls.append("SCROLL\tZoom\n");
			controls.append("SHIFT+DRAG\tMove\n");
			controls.append("CTRL+MOUSE\tSelect\n\n");


			controls.append("Keyboard Shortcuts:\n");

			controls.append("=\tZoom in\n");
			controls.append("-\tZoom out\n");	
			controls.append("N\tNext iteration\n");
			controls.append("SPACE\tStart/Stop Timer\n");			
			controls.append("T\tIncrease speed\n");
			controls.append("R\tDecrease speed\n");		
			controls.append("F1\tPopulate\n");
			controls.append("F2\tEradicate\n");

			content.add (controls, "East");
		}
		else
		{
			content.remove (controls);
			controls = null;
		}
		setContentPane (content);
	}
	
	/** Starts the simulation timer. The frequency
	 * of the timer is determined by the current value
	 * of the speed slider component. Specifically, the
	 * timer of the delay is equal to 1000 milliseconds 
	 * divided by 2 to the power of the current slider value. 
	 */
	public void startTimer ()
	{
		simulate.setText("Stop Timer");
		timer = new Timer();
		long delay = 1000 / (long)Math.pow(2, speed.getValue());		
		timer.scheduleAtFixedRate (new Advance(), 0, delay);		
	}	

	/** Stops the simulation timer if it is not stopped.
	 * Sets the simulation timer to null. 
	 */
	public void stopTimer ()
	{
		if (timer != null)
		{
			simulate.setText("Start Timer");
			timer.cancel();
			timer = null;
		}
	}	
	
	/** Enables moving mode if moving mode is disabled.
	 * Disables moving mode if moving mode is enabled. 
	 */
	public void toggleMove ()
	{
		if (move.getText().equals ("Move"))
		{
			move.setText("Edit");
			plane.startMoving ();
		}
		else if (move.getText().equals("Edit"))
		{
			move.setText("Move");
			plane.stopMoving ();					
		}
	}

	/** Starts the simulation timer if it is currently stopped.
	 * Stops the simulation timer if it is currently running. 
	 */
	public void toggleTimer ()
	{
		if (simulate.getText().equals("Start Timer"))		
			startTimer ();		
		else if (simulate.getText().equals("Stop Timer"))		
			stopTimer ();		
	}
		
	/** Repaints the LifePlane panel, and updates the
	 * iteration JLabel in the toolbar with the colony's
	 * current iteration value. 
	 */
	public void updateUI ()
	{				
		iteration.setText ("" + plane.colony.getItt());
		plane.repaint();
	}

	/** Listens to and acts upon the clicking of various
	 * buttons in the toolbar.
	 */
	private class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{			
			JButton button = (JButton) e.getSource();

			if (button.getText().equals("Save"))
			{
				stopTimer ();
				manager.removeKeyEventDispatcher(keyDispatcher);					
				saveFile ();
				manager.addKeyEventDispatcher(keyDispatcher);
			}
			else if (button.getText().equals("Load"))
			{
				stopTimer ();
				manager.removeKeyEventDispatcher(keyDispatcher);					
				loadFile ();
				updateUI ();					
				manager.addKeyEventDispatcher(keyDispatcher);
			}				
			else if (button.getText().equals ("Next"))
			{
				advance ();
			}
			else if (button.getText().equals("Start Timer")
					|| button.getText().equals("Stop Timer"))
			{
				toggleTimer ();
			}
			else if (button.getText().equals("Select"))
			{
				button.setText("Cancel");
				plane.startSelection ();
			}
			else if (button.getText().equals("Cancel"))
			{
				button.setText("Select");
				plane.cancelSelection ();					
			}
			else if (button.getText().equals("Move")
					|| button.getText().equals("Edit"))
			{
				toggleMove();
			}			
			else if (button.getText().equals("Eradicate"))
			{
				eradicate ();
			}
			else if (button.getText().equals("Populate"))
			{
				populate ();
			}
			else if (button.getText().equals("Set Size"))
			{
				setSize();										
			}
			else if (button.getText().equals("Controls..."))
			{
				toggleShowControls();
			}
		}		
	}
	
	/** Listens to and acts upon changes in the speed slider component.
	 */
	private class SliderListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e) 
		{
			JSlider source = (JSlider)e.getSource();
			if (source.equals(speed))
			{
				if (timer != null)
				{
					// Reset timer to update Timer delay
					stopTimer();
					startTimer();
				}
			}
		}
	}

	/** Listens to and acts upon changes in the zoom slider component.
	 */
	private class MyMouseListener extends MouseAdapter
	{
		@Override
		public void mouseClicked (MouseEvent e)	
		{
			plane.setZoom (zoom.getValue());
		}

		@Override
		public void mouseDragged (MouseEvent e)
		{
			plane.setZoom(zoom.getValue());
		}
	}

	/** Listens to and acts upon keyboard inputs. This KeyEventDispatcher
	 * is temporally disabled when JFileChooser is brought up, so that all
	 * keyboard inputs are isolated to the JFileChooser, and so that nothing
	 * happens while the user is using the JFileChooser.
	 */
	private class MyDispatcher implements KeyEventDispatcher 
	{
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) 
		{			
			boolean consumed = false;

			if (e.getID() == KeyEvent.KEY_PRESSED) 
			{
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_CONTROL)
				{
					if (select.getText().equals ("Select"))
					{
						select.setText("Cancel");
						plane.startSelection ();
					}            			
				}
				else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				{
					if (move.getText().equals("Move"))
					{
						move.setText("Edit");
						plane.startMoving();
					}					
				}
				else if (key == KeyEvent.VK_F1)
				{
					populate ();
				}
				else if (key == KeyEvent.VK_F2)
				{
					eradicate ();
				}				
				else if (key == KeyEvent.VK_N)
				{
					advance();
				}	
				else if (key == KeyEvent.VK_SPACE)
				{
					toggleTimer();	
					consumed = true; // to prevent buttons from being pressed
				}
				else if (key == KeyEvent.VK_T)
				{
					int rate = speed.getValue();
					rate++;
					speed.setValue(rate);
				}
				else if (key == KeyEvent.VK_R)
				{
					int rate = speed.getValue();
					rate--;
					speed.setValue(rate);
				}	
				else if (key == KeyEvent.VK_EQUALS)
				{
					int n = plane.colony.getZoom() + 1;
					zoom.setValue(n);
					plane.setZoom(getMousePosition(), n);
				}
				else if (key == KeyEvent.VK_MINUS)
				{
					int n = plane.colony.getZoom() - 1;
					zoom.setValue(n);
					plane.setZoom(getMousePosition(), n);
				}				
			} 
			else if (e.getID() == KeyEvent.KEY_RELEASED) 
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					select.setText("Select");
					plane.finalizeSelection();					
					if (plane.getSelection().getCurrentPoint() == null)
						plane.cancelSelection();
				}  
				else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				{
					move.setText("Move");
					plane.stopMoving();
				}
			}  
			updateUI();
			return consumed;
		}
	}
}

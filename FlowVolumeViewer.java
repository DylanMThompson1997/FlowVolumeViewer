/* 
	Author:	Dylan Thompson
	UPI:	DTHO410
	AUID:	81483811
*/

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FlowVolumeViewer extends JFrame implements ComponentListener, ItemListener {
    
	private Set<String> sourceNamesSet;
	private Set<String> destinationNamesSet;
	private ArrayList<String> sourceNames;
	private ArrayList<String> destinationNames;
	private ArrayList<LineOfData> rawData;
	private ArrayList<Double> valuesForPlotting;
	private ArrayList<LineOfData> pertinentIPs;
	
	private JComboBox<String> IPComboBox;
	private Font font;
	private JPanel radioButtonPanel;
    private ButtonGroup radioButtons;
    private JRadioButton radioButtonSourceHosts;
    private JRadioButton radioButtonDestinationHosts;
	private JPanel graphPanel;
	private Scanner scanner;
	private int fileOpened = 0;
	private int selectedButton = 0;

	/**
	 * This is a constructor for the program, setting up and calling other methods.
	 */
	public FlowVolumeViewer() {
    	super("Flow Volume Viewer");
    	setLayout(new FlowLayout());		

    	setSize(1000,500);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	font = new Font("Sans-serif", Font.PLAIN, 20);
    	setupArrayLists();
    	setupMenu();
		setupButtons();
		setupComboBox();
		setGraphDefault();
    	
    	this.addComponentListener(this);
    	setVisible(true);	
	}
		
	/**
	 * Initialises the two JButtons for Source and Destination.
	 * ActionListener included to call method that changes the combo box contents
	 * depending on which button had been selected.
	 */
	private void setupButtons() {
		radioButtonPanel = new JPanel();
		//radioButtonPanel.setBackground(Color.GREEN);
		radioButtonPanel.setPreferredSize(new Dimension(200,100));
		
    	radioButtonPanel.setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = 0;
    	c.gridy = GridBagConstraints.RELATIVE;
    	c.anchor = GridBagConstraints.WEST;
    	// Group the buttons so only one can be selected at a time
        radioButtons = new ButtonGroup();
    	// Create the buttons allowing only one to be selected
        // Add to button group and panel
    	radioButtonSourceHosts = new JRadioButton("Source hosts");
    	radioButtonSourceHosts.setFont(font);
     	radioButtonSourceHosts.setSelected(true);
		radioButtonSourceHosts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedButton = 0;
				updateComboBoxSource();
				}});
		radioButtons.add(radioButtonSourceHosts);
        radioButtonPanel.add(radioButtonSourceHosts, c);
		
    	radioButtonDestinationHosts = new JRadioButton("Destination hosts");
    	radioButtonDestinationHosts.setFont(font);
		radioButtonDestinationHosts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedButton = 1;
				updateComboBoxDestination();
				}});
        radioButtons.add(radioButtonDestinationHosts);
    	radioButtonPanel.add(radioButtonDestinationHosts, c);
    	add(radioButtonPanel);
	}
	
	/**
	 * Initialises all the array lists and other data structures appropriately.
	 */
	private void setupArrayLists() {
		sourceNamesSet = new HashSet<String>();
		destinationNamesSet = new HashSet<String>();
		sourceNames = new ArrayList<String>();
		destinationNames = new ArrayList<String>();
		rawData = new ArrayList<LineOfData>();
	}
	
	/**
	 * Initialises the menu of the GUI. The data reading occurs inside
	 * the fileMenuOpen object, and calls are made to various other
	 * pre-processing stages for final graphing.
	 */
	private void setupMenu() {
    	JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		fileMenu.setFont(font);
		menuBar.add(fileMenu);
		JMenuItem fileMenuOpen = new JMenuItem("Open trace file");
		fileMenuOpen.setFont(font);
		fileMenuOpen.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
    					JFileChooser fileChooser = new JFileChooser(".");
    					int retval = fileChooser.showOpenDialog(FlowVolumeViewer.this);
    					if (retval == JFileChooser.APPROVE_OPTION) {
    						File f = fileChooser.getSelectedFile();
									
							try {
								scanner = new Scanner(new File(f.getAbsolutePath()));
							} catch(FileNotFoundException fnfe) {
								JOptionPane.showMessageDialog(null, fnfe.getMessage());
								System.exit(0);
							}
							
							// Hashset for later storage of unique elements for display in combobox
    						sourceNamesSet = new HashSet<String>();
							destinationNamesSet = new HashSet<String>();
							sourceNames = new ArrayList<String>();
							destinationNames = new ArrayList<String>();
							
							// read in lines of data and process into special lod format
							rawData = new ArrayList<LineOfData>();
							LineOfData lod = new LineOfData();
							while (scanner.hasNext()) {
								String row = scanner.nextLine();
								lod = readLine(row);
								if (lod.getSuccess() == 1) {	// only add if the line read contains valid data
									rawData.add(lod);
									sourceNamesSet.add(lod.getSource());
									destinationNamesSet.add(lod.getDestination());
								}
								
							}
							sourceNames.addAll(sourceNamesSet);
							// sort the IP addresses according to special method
							Collections.sort(sourceNames, new Comparator<String>() {
								@Override
								public int compare(String s1, String s2) {
									String[] IP1 = s1.split("\\.");
									String[] IP2 = s2.split("\\.");
									String newIP1 = String.format("%3s.%3s.%3s.%3s", IP1[0],IP1[1],IP1[2],IP1[3]);
									String newIP2 = String.format("%3s.%3s.%3s.%3s", IP2[0],IP2[1],IP2[2],IP2[3]);
									return newIP1.compareTo(newIP2);
								}
							});
							destinationNames.addAll(destinationNamesSet);
							Collections.sort(destinationNames, new Comparator<String>() {
								@Override
								public int compare(String s1, String s2) {
									String[] IP1 = s1.split("\\.");
									String[] IP2 = s2.split("\\.");
									String newIP1 = String.format("%3s.%3s.%3s.%3s", IP1[0],IP1[1],IP1[2],IP1[3]);
									String newIP2 = String.format("%3s.%3s.%3s.%3s", IP2[0],IP2[1],IP2[2],IP2[3]);
									return newIP1.compareTo(newIP2);
								}
							});
							//System.out.println(rawData.get(rawData.size()-1));
							//.split("/t");
							fileOpened = 1;
							//updateComboBoxDestination();
							if (selectedButton == 0) {
								updateComboBoxSource();
							} else {
								updateComboBoxDestination();
							}
	        				
							
    					}
					}
				}
        );
		fileMenu.add(fileMenuOpen);
		JMenuItem fileMenuQuit = new JMenuItem("Quit");
		fileMenuQuit.setFont(font);
		fileMenu.add(fileMenuQuit);
		fileMenuQuit.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				}
				);
	}
	
	public class LineOfData {
		private double time;
		private String source;
		private String destination;
		private double size;
		private int success = 0;
		
		public void setTime(double t) {
			time = t;
		}
		public void setSource(String s) {
			source = s;
		}
		public void setDestination(String d) {
			destination = d;
		}
		public void setSize(double sz) {
			size = sz;
		}
		public void setSuccess(int sss) {
			success = sss;
		}
		public String getSource() {
			return source;
		}
		public String getDestination() {
			return destination;
		}
		public double getSize() {
			return size;
		}
		public double getTime() {
			return time;
		}
		public int getSuccess() {
			return success;
		}
	}
	
	/**
	 * Reads line after line of raw data, converting them into
	 * LineOfData objects containing segregated information.
	 *
	 * @param	line	a raw data line to be read into a LineOfData object
	 * @return	lod		a LineOfData object containing segregated information
	 */
	public LineOfData readLine(String line) {
		String[] columns = line.split("\t",-1);
		LineOfData lod = new LineOfData();
		if (!((columns[2].isEmpty()) || (columns[4].isEmpty()) || (columns[7].isEmpty()) || (columns[1].isEmpty()))) {
			lod.setTime(Double.parseDouble(columns[1]));
			lod.setSource(columns[2]);
			lod.setDestination(columns[4]);
			lod.setSize(Double.parseDouble(columns[7]));
			lod.setSuccess(1);
		}
		return lod;
	}

	/**
	 * Processes the raw data once into forms that later
	 * graphing methods can reuse.
	 *
	 * @param	hostIndex			the IP address in question
	 * @return	valuesForPlotting	double values that are used for plotting later
	 */
	private ArrayList<Double> processData(int hostIndex) {
		if (selectedButton == 0) {
			valuesForPlotting = new ArrayList<Double>();
			pertinentIPs = new ArrayList<LineOfData>();
			double currTime = 2.0;
			int currIndex = 0;
			String currIP = sourceNames.get(hostIndex);
			for (LineOfData lod : rawData) {
				if (lod.getSource().equals(currIP)) {
					pertinentIPs.add(lod);
				}
			}
			int max_time = (int) Math.ceil(pertinentIPs.get(pertinentIPs.size()-1).getTime());
			for (int i = 0; i < max_time/2; i++) {
				valuesForPlotting.add(0.0);
			}
			for (LineOfData lod : rawData) {
				if (lod.getSource().equals(currIP)) {
					if (lod.getTime() <= currTime) {
						valuesForPlotting.set(currIndex,valuesForPlotting.get(currIndex)+lod.getSize());
					} else{
						currTime += 2.0;
						currIndex +=1;
					}
				}
			}
			return valuesForPlotting;
			
		} else  {
			valuesForPlotting = new ArrayList<Double>();
			pertinentIPs = new ArrayList<LineOfData>();
			double currTime = 2.0;
			int currIndex = 0;
			String currIP = destinationNames.get(hostIndex);
			for (LineOfData lod : rawData) {
				if (lod.getDestination().equals(currIP)) {
					pertinentIPs.add(lod);
				}
			}
			int max_time = (int) Math.ceil(pertinentIPs.get(pertinentIPs.size()-1).getTime());
			for (int i = 0; i < max_time/2; i++) {
				valuesForPlotting.add(0.0);
			}
			for (LineOfData lod : rawData) {
				if (lod.getDestination().equals(currIP)) {
					if (lod.getTime() <= currTime) {
						valuesForPlotting.set(currIndex,valuesForPlotting.get(currIndex)+lod.getSize());
					} else{
						currTime += 2.0;
						currIndex +=1;
					}
				}
			}
			return valuesForPlotting;
		}
	}
	
	/**
	 * At program onset, this sets up the default graph containing 
	 * no data yet, only empty axes
	 */
	private void setGraphDefault(){
		graphPanel = new PlottingPanel(new ArrayList<Double>(Arrays.asList(16.70,10.111,4.0,29.0,80.0, 50.3, 40.00, 32.00)));
		graphPanel.setPreferredSize(new Dimension(900, 300));
		//graphPanel.setBackground(Color.GREEN);

		add(graphPanel);
	}
	
	/**
	 * Create the graph for the source address
	 *
	 * @param  hostIndex  index of the current IP address in the Source list
	 */
	private void setGraphSource(int hostIndex){
		remove(graphPanel);
		valuesForPlotting = processData(hostIndex);
		graphPanel = new PlottingPanel(valuesForPlotting);
		graphPanel.setPreferredSize(new Dimension(900, 300));
		//graphPanel.setBackground(Color.GREEN);
		add(graphPanel);
	}
	
	/**
	 * Create the graph for the destination address
	 *
	 * @param  hostIndex  index of current IP address in the Destination list
	 */
	private void setGraphDestination(int hostIndex){
		remove(graphPanel);
		valuesForPlotting = processData(hostIndex);
		graphPanel = new PlottingPanel(valuesForPlotting);
		graphPanel.setPreferredSize(new Dimension(900, 300));
		//graphPanel.setBackground(Color.GREEN);
		add(graphPanel);
	}
	
	/**
	 * At program onset, initialise the combobox, but set to invisible,  
	 * and ensure the elements can change.
	 */
	private void setupComboBox() {
		IPComboBox = new JComboBox<String>();
		IPComboBox.setModel((MutableComboBoxModel<String>) IPComboBox.getModel());
		IPComboBox.setMaximumRowCount(8);
		IPComboBox.addItemListener(this);
		IPComboBox.setFont(font);
		IPComboBox.setMinimumSize(new Dimension(200,100));
		IPComboBox.setVisible(false);
		add(IPComboBox);
	}
	
	/**
	 * Updates the combo box because the source button has been selected
	 * Calls the setGraph... method
	 */
	private void updateComboBoxSource() {
		if (fileOpened == 1) {
			IPComboBox.removeAllItems();
			for (String sourceName : sourceNames) {
				IPComboBox.addItem(sourceName);
			}
			IPComboBox.setSelectedIndex(0);
			IPComboBox.setVisible(true);
			setGraphSource(0);
		}
	}
	
	/**
	 * Updates the combo box because the destination button has been selected.
	 * Calls the setGraph... method.
	 */
	private void updateComboBoxDestination() {
		if (fileOpened == 1) {
			IPComboBox.removeAllItems();
			for (String destinationName : destinationNames) {
				IPComboBox.addItem(destinationName);
			}
			IPComboBox.setSelectedIndex(0);
			IPComboBox.setVisible(true);
			setGraphDestination(0);
		}
	}
	
	// ComponentListener methods
	
	/**
	 * Catches the event when the window is hidden
	 *
	 * @param	e	a component event when the hidden status is elicited
	 */
	public void componentHidden(ComponentEvent e) {
        return;
    }

	/**
	 * Catches the event when the window is moved
	 *
	 * @param	e	a component event when the moved status is elicited
	 */
    public void componentMoved(ComponentEvent e) {
        return;
    }

	/**
	 * Catches the event when the window is resized
	 *
	 * @param	e	a component event when the resized status is elicited
	 */
    public void componentResized(ComponentEvent e) {
	return;
    }

	/**
	 * Catches the event when the window is shown
	 *
	 * @param	e	a component event when the shown status is elicited
	 */
    public void componentShown(ComponentEvent e) {
        return;
    }
	
    // ItemListener method
	
	/**
	 * Catches the event when the combo box selection changes.
	 * Ensure that according to which radio button is selected,
	 * the correct graphing method is called.
	 *
	 * @param	e	an item event when the combo box selection changes
	 */
    public void itemStateChanged(ItemEvent e) {
    	if (e.getStateChange() == ItemEvent.SELECTED) {
			if (selectedButton == 0) {
				setGraphSource(IPComboBox.getSelectedIndex());
			} else if (selectedButton == 1) {
				setGraphDestination(IPComboBox.getSelectedIndex());
			}
    	}
    	return;
    }
	
	
	public class PlottingPanel extends JPanel {

		private int axesPadding = 55;
		private int outerPadding = 30;
		private int markerSize = 5;
		private int n_Yticks = 8;
		private int n_Xticks = 16;
		private int tickLength = 5;
		
		private Color markerColor = Color.red;
		private Color backgroundColor = Color.white;
		private Color axesColor = Color.black;
		
		private ArrayList<Double> sizes;
		private double max_size;
		private int xExtent;
		private int yExtent;

		public PlottingPanel(ArrayList<Double> doubleValues) {
			sizes = doubleValues;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			FontMetrics metrics = g2.getFontMetrics();
			
			// calculate max size of all values
			max_size = -999999999;
			for (Double a_size : sizes) {
				if (a_size > max_size){
					max_size = a_size;
				}
			}

			xExtent = getWidth() - axesPadding-outerPadding;
			yExtent = getHeight() - axesPadding-outerPadding;
			
			// colour the background
			g2.setColor(backgroundColor);
			g2.fillRect(axesPadding, outerPadding, xExtent, yExtent);
			
			// determine axes tick spacings
			double xUnit = (double)xExtent / (sizes.size() - 1);
			double yUnit = (double)yExtent / max_size;
			ArrayList<Point> Points = new ArrayList<>();
			for (int i = 0; i < sizes.size(); i++) {
				int x = (int)(xUnit*i + axesPadding);
				int y = (int)((max_size - sizes.get(i)) * yUnit + outerPadding);
				Points.add(new Point(x,y));
			}
			
			// mark the data points IF NOT DEFAULT
			if (sizes.get(1)!=10.111) {
				g2.setColor(markerColor);
				for (int i = 0; i < Points.size(); i++) {
					g2.fillOval(Points.get(i).x-markerSize/2, Points.get(i).y-markerSize/2, markerSize, markerSize);
				}
			}

			// create x and y axes
			g2.setColor(axesColor);			
			g2.drawLine(axesPadding, outerPadding, axesPadding, getHeight() - axesPadding);
			g2.drawLine(axesPadding, getHeight() - axesPadding, getWidth()-outerPadding, getHeight() - axesPadding);
	
			// label axes
			String xLabel = "Time [s]";
			g2.drawString(xLabel, getWidth()/2, getHeight() - axesPadding/4);
			String yLabel = "Volume [bytes]";
			int labelWidth = metrics.stringWidth(yLabel);
			g2.drawString(yLabel, axesPadding/4, outerPadding/3);
						
			// x axis tick marks
			for (int i = 0; i < n_Xticks+1; i++) {
				int x = i*(getWidth()-axesPadding-outerPadding) / (n_Xticks) + axesPadding;
				int y1 = getHeight() - axesPadding;
				int y2 = y1 - tickLength;
				if (sizes.size() > 0) {
						g2.setColor(axesColor);
						String xTick = (2*(sizes.size())/n_Xticks)*i + "";
						labelWidth = metrics.stringWidth(xTick);
						g2.drawString(xTick, x - labelWidth / 2, y1 + metrics.getHeight() + 5);
						g2.drawLine(x, y1, x, y2);
				}
			}
			
			if (sizes.get(1)==10.111) {
				// y axis tick marks
				for (int i = 0; i < n_Yticks + 1; i++) {
					int x1 = axesPadding;
					int x2 = axesPadding + tickLength;
					int y = getHeight() - ((i * (getHeight() - axesPadding-outerPadding)) / n_Yticks)- axesPadding;
					if (sizes.size() > 0) {
						g2.setColor(axesColor);
						String yTick = Math.round((max_size * ((double)i / n_Yticks))* 100.0) / 100.0 + "";
						labelWidth = metrics.stringWidth(yTick);
						g2.drawString(yTick, x1 - labelWidth - 5, y + (metrics.getHeight() / 2) - 5);
						g2.drawLine(x1, y, x2, y);
					}
				}
			} else{
				// y axis tick marks
				for (int i = 0; i < n_Yticks + 1; i++) {
					int x1 = axesPadding;
					int x2 = axesPadding + tickLength;
					int y = getHeight() - ((i * (getHeight() - axesPadding-outerPadding)) / n_Yticks)- axesPadding;
					if (sizes.size() > 0) {
						g2.setColor(axesColor);
						String yTick = (int) Math.ceil((Math.round((max_size * ((double)i / n_Yticks))* 100.0) / 100.0)/1000.0) + "k";
						labelWidth = metrics.stringWidth(yTick);
						g2.drawString(yTick, x1 - labelWidth - 5, y + (metrics.getHeight() / 2) - 5);
					}
					g2.setColor(axesColor);
					g2.drawLine(x1, y, x2, y);
				}
			}
		}	
		
	}
    
}

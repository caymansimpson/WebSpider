package crawler; //written by Cayman Simpson ©

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class WebFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton load;
	private JButton destination;
	private DefaultTableModel model;
	private JButton downloadButton;
	private JTextField numThreads;
	private JLabel checkLabel;
	private JCheckBox crawl;
	private WebWorker[] array;
	
	protected JTable table;
	protected Semaphore semaphore;

	private Thread launcherThread;
	private String destinationDirectory;

	private String helpMessage() {
		return WSC.HELP;
	}

	/* -------------------- addTable() -----------------------
	 * Creates the default table with WebSpiderConstants constants
	 * 	Specifications:
	 * 		1) Contains anonymous function that makes second column
	 * 		   of the table uneditable
	 * 		2) Creates and returns a JPanel containing the table. 
	 * */
	private void addTable() {
		JPanel panel = new JPanel();
		String[][] data = new String[WSC.DEFAULT_TABLE_SIZE][2];
		model = new DefaultTableModel(data, WSC.TABLE_HEADERS) {

			private static final long serialVersionUID = 1L;

			/* ----------------- Anonymous Function ------------------
			 * Makes it so that the second column (index 1) is not editable
			 * */

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}
		};

		table = new JTable(model); 
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); 
		table.setGridColor(Color.BLACK);

		JScrollPane scrollpane = new JScrollPane(table); 
		scrollpane.setPreferredSize(new Dimension(WSC.TABLE_SCROLLPANE_WIDTH,WSC.TABLE_SCROLLPANE_HEIGHT)); 
		panel.add(scrollpane); 
		add(panel);
	}

	/* -------------------- readLinks() -----------------------
	 * Reads links from a file, one at a time. Links are assumed to be
	 * separated by a new line. One link per line.
	 * */
	private ArrayList<String> readLinks(String filename) {
		ArrayList<String> links = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line = "";
			while( (line = br.readLine()) != null) links.add(line.trim());

			br.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to read file: " + filename);
		}
		return links;
	}

	/* -------------------- loadLinks() -----------------------
	 * Loads links from the parameter filename and inserts them
	 * into the loaded table. Will create new rows if table does not
	 * contain enough empty rows
	 * */
	private void loadLinks(String filename) {

		ArrayList<String> links = readLinks(filename);

		int count = 0;
		for(int i = 0; i < table.getRowCount(); i++) {
			if(table.getValueAt(i, 0) == null && count < links.size()) {
				table.setValueAt(links.get(count++), i, 0);
				if(count == links.size()) break; //if we're done
			}
		}

		while(count <= links.size() - 1) { //if we ran out of table space but still have more links, add rows
			((DefaultTableModel) table.getModel()).addRow(new String[]{links.get(count++),""});
		}
	}

	private void initializeLauncherThread(int num) {
		Launcher launcher = new Launcher(Integer.parseInt(numThreads.getText())); //initialize launcher with numthreads
		launcherThread = new Thread(launcher); //create launcher thread

		launcherThread.start(); //start launcher thread
	}
	
	private void interruptAllThreads() {
		for(int i = 0; i < array.length; i++) { //interrupt all workers
			if(array[i] != null) array[i].interrupt();
		}

		launcherThread.interrupt(); //interrupt launcher thread
	}
	

	private void clearTableColumn(int col) { //clears second column
		for(int i = 0; i < table.getRowCount(); i++) table.setValueAt("", i, col);
	}

	private void initializeDownloadButton() {
		downloadButton = new JButton(WSC.DOWNLOAD_BUTTON_DEFAULT_TEXT); //create download button
		
		downloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!downloadButton.getText().equals(WSC.DOWNLOAD_BUTTON_STOP_TEXT)) {

					if(table.getCellEditor() != null) table.getCellEditor().stopCellEditing();//accept all values in table
					
					if(!Utilities.isNumeric(numThreads.getText())) {
						JOptionPane.showMessageDialog(null, "Please enter a valid number into the Text Field");
						return; //bad user input, do nothng
					}
					
					clearTableColumn(1); //clear second column of table
					crawl.setEnabled(false);
					downloadButton.setText(WSC.DOWNLOAD_BUTTON_STOP_TEXT);

					initializeLauncherThread(Integer.parseInt(numThreads.getText())); //initialize launcher with numthreads


				} else {
					
					interruptAllThreads();

					//enable single thread and download thread buttons
					crawl.setEnabled(true);
					downloadButton.setText(WSC.DOWNLOAD_BUTTON_DEFAULT_TEXT);
				}
			}	
		});
	}
	
	private void initializeNumThreads() {
		numThreads = new JTextField("", 10); //initialize textfield
		numThreads.setMaximumSize(new Dimension(150, 20));
		numThreads.setText("1");
	}

	//adds bottom scroll panel
	private void addControlPanel() {
		JPanel controlPanel = new JPanel();

		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		controlPanel.add(topPanel());
		controlPanel.add(bottomPanel());

		add(controlPanel);
	}
	
	private void initializeDestinationButton() {
		destination = new JButton("Select Destination Directory");
		destination.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Select destination directory");
				
				if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return; 
				
				File f = fc.getSelectedFile();
				if (f != null) {
					
					destinationDirectory = f.getAbsolutePath(); //get the absolute path to selected file in global
					
					//format button text
					String str = destinationDirectory.substring(destinationDirectory.lastIndexOf("/"));
					int buttonlen = "Select Destination Directory".length();
					int length = str.length() + "/Web Data.txt".length();
					if(length - buttonlen > 0) destination.setText("..." + str.substring(length - buttonlen + 3) + "/Web Data.txt");
					else destination.setText("..." + str + "/Web Data.txt");
				}

			}
		});
	}

	private void initializeLoadButton() {
		load = new JButton("Load Links From File");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Select file of links to read");
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					loadLinks(fc.getSelectedFile().getAbsolutePath()); //get the absolute path to selected file
				}
			}

		});
	}
	
	private JComponent topPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		//destination, load, path

		panel.add(load);
		panel.add(destination);

		//unnecessary, for aesthetics
		for(int i = 0; i < 6; i++) panel.add(Box.createRigidArea(new Dimension(15,0)));

		JButton help = new JButton("Help");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, helpMessage());
			}
		});

		panel.add(help);

		return panel;
	}
	
	private JComponent bottomPanel() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		bottomPanel.add(downloadButton);
		bottomPanel.add(numThreads);

		//unnecessary, for spacing
		for(int i = 0; i < 11; i++) bottomPanel.add(Box.createRigidArea(new Dimension(15,0)));

		crawl = new JCheckBox();

		checkLabel = new JLabel("Exhaustive Crawl?:");
		checkLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) { crawl.setSelected(!crawl.isSelected()); } // label is part of checkbox
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		});
		bottomPanel.add(checkLabel);
		bottomPanel.add(crawl);
		
		return bottomPanel;
	}

	public WebFrame() { //initializes frame
		super("Web Spider");
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		addTable();
		initializeDownloadButton();
		initializeNumThreads();
		initializeDestinationButton();
		initializeLoadButton();
		addControlPanel();
	}

	private class Launcher implements Runnable { //inner class that handles all threads

		public Launcher(int limit) { //sets how many threads run at a time
			semaphore = new Semaphore(limit);
		}
		
		@Override
		public void run() {
			if(destinationDirectory != null) WebWorker.setDirectory(destinationDirectory); //check to see if destination is set
			else {
				JOptionPane.showMessageDialog(null, "Please enter a destination to store your results.");
				downloadButton.setText(WSC.DOWNLOAD_BUTTON_DEFAULT_TEXT);
				return;
			}
			
			if(crawl.isSelected()) runExhaustive();
			else runRegular();
		}

		private void runRegular() {
			array = new WebWorker[table.getRowCount()]; //initialize array of workers

			for(int i = 0; i < table.getRowCount(); i++) {
				if(table.getValueAt(i,0) == null || table.getValueAt(i, 0).equals("")) continue;

				WebWorker worker = new WebWorker((String)table.getValueAt(i, 0), i, WebFrame.this); //create new thread

				worker.start(); //start new thread
				array[i] = worker; //store thread to keep track of it
				if(Thread.currentThread().isInterrupted()) break; //if interrupted stop making new threads
			}

			for(int i = 0; i < array.length; i++) {
				try {
					if(array[i] != null) array[i].join(); //wait til all threads are finished
				} catch (InterruptedException ignored) {
					//do nothing
				}
			}

			finishDownload();
		}

		private void runExhaustive() {

			int maxThreads = Integer.parseInt(numThreads.getText());
			array = new WebWorker[table.getRowCount()*maxThreads]; //initialize array of workers

			ExhaustiveWebWorker.resetStatics();

			for(int i = 0; i < table.getRowCount(); i++) {
				if(table.getValueAt(i, 0) == null || table.getValueAt(i, 0).equals("")) continue;
				
				for(int j = 0; j < maxThreads; j++) {
					WebWorker worker = new ExhaustiveWebWorker((String)table.getValueAt(i, 0), i, WebFrame.this); //create exhaustive thread

					worker.start();
					array[i+j] = worker;
					if(Thread.currentThread().isInterrupted()) break;
				}

				for(int k = i; k < i + maxThreads; k++) { //join the threads of that exhaustive search
					try {
						if(array[k] != null) array[k].join();
					} catch (InterruptedException ignored) {
						//do nothing
					}
				}
			}

			finishDownload();
		}
		
		private void finishDownload() {
			downloadButton.setText(WSC.DOWNLOAD_BUTTON_DEFAULT_TEXT);
			crawl.setEnabled(true);
		}
	}


	private static void createAndShowGUI() { //create gui
		WebFrame frame = new WebFrame();
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	} 

	public static void main(String[] args) { //main that creates gui
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() { 
				createAndShowGUI(); 
			} 
		}); 
	}

}

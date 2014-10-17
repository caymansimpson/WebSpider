package crawler; //written by Cayman Simpson ©

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class WebWorker extends Thread {
	protected String urlString;
	protected int row;
	protected WebFrame parent;
	protected static String directory;

	public WebWorker(String url, int rowNumber, WebFrame parent) { //set up instances
		urlString = url;
		row = rowNumber;
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			parent.semaphore.acquire(); //ask if we can go

			displayMessage("Working...");

			//try all these types of strings
			download(Utilities.format(urlString)); //download url contents

			parent.semaphore.release(); //let another thread run

		} catch (InterruptedException e) {
			//stopped before it started (could get permit from semaphore)
			displayMessage("");
		}
	}

	public static void setDirectory(String filename) {
		directory = filename + "/" + WSC.DEFAULT_DIRECTORY;
		File dir = new File(directory);

		// if the directory does not exist, create it
		if (!dir.exists()) {
			try { dir.mkdir(); }
			catch(SecurityException se){ JOptionPane.showMessageDialog(null, "Could not create directory there because of Security Permissions. Try Again."); }        
		}
	}

	//prints to file what the parser outputs
	synchronized protected void writeToFile(String content, String URL) {
		try {
			PrintWriter p = new PrintWriter(directory + "/" + URL.replaceAll("http://","").replaceAll(".com","") + ".txt");
			p.print(content);
			p.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void displayMessage(String message) {
		parent.table.setValueAt(message, row, 1);
	}

	private void download(String URL) {
		InputStream input = null;
		String line = null;
		try {
			long start = System.currentTimeMillis();

			URL url = new URL(URL);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(WSC.CONNECTION_TIMEOUT);
			connection.connect();
			input = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));

			line = br.readLine();
			String content = "";

			long bytes = 0;
			while (line != null) {
				bytes += line.length();
				content += line + "\n";

				if(isInterrupted()) { //check for interruption to make speedy response
					displayMessage("Error: Thread Interrupted");
					if (input != null) input.close();
					if(br != null) br.close();
					writeToFile(content, URL);
					return; 
				}

				line = br.readLine();
			}

			writeToFile(content, URL);

			// Successful download if we get here; construct and put table value
			long time = System.currentTimeMillis() - start;
			String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			String value = currentTime + " " + time/1000 + " sec " + bytes + " bytes";
			if(bytes != 0) displayMessage(value);
			else displayMessage("Error: Malformed URL");

		}
		catch(MalformedURLException ignored) {
			displayMessage("Malformed URL: Could not read: " + URL);
		}
		catch(IOException ignored) {
			displayMessage("Error: Parsing Problem"); //io exception, connection timeout
		}
		// finally cluase to close input stream in any case
		finally {
			try{
				if (input != null) input.close(); //save resources
			}
			catch(IOException ignored) {}
		}
	}
}

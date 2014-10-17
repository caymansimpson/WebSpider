package crawler; //written by Cayman Simpson ©

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

public class ExhaustiveWebWorker extends WebWorker {

	private static int count;
	private static PriorityBlockingQueue<String> pq;
	private long byte_count;
	private static HashSet<String> seen;


	public ExhaustiveWebWorker(String url, int rowNumber, WebFrame parent) { //set up instances
		super(url, rowNumber, parent);
		seen = new HashSet<String>();
	}

	public static void resetStatics() {
		pq = new PriorityBlockingQueue<String>(100);
		seen = new HashSet<String>(100);
		count = 0; 
	}

	@Override
	synchronized protected void writeToFile(String content, String URL) { //TODO: write to directory
		String[] array = URL.replaceAll("http://","").split("/");//TODO: fix
		System.out.println("\nBEGINNING URL: " + URL + "\t==(SPLIT)==>\t" + Arrays.toString(array));
		String path = directory + "/";
		for(int i = 0; i < array.length; i++) {
			path += array[i] + "/";
			if(array.length == 1) path = path.replace("www.", ""); //so that you can create a directory on top of that

			if(i == array.length - 1) {
				try {
					System.out.println("Writing to file: " + path);
					PrintWriter p = new PrintWriter(path.substring(0, path.length() - 1));
					p.print(content);
					p.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			} else {

				File dir = new File(path);
				if(!dir.exists()) {
					try {
						dir.mkdir();
					}
					catch(SecurityException se){ JOptionPane.showMessageDialog(null, "Could not create directory there because of Security Permissions. Try Again."); } 
				}
			}
		}
	}


	@Override
	public void run() {
		try {
			parent.semaphore.acquire(); //ask if we can go

			long start = System.currentTimeMillis();
			//TODO: put so that only read first page once
			pq.put(Utilities.format(urlString)); //haven't seen the string yet

			while(!pq.isEmpty()) {
				System.out.println(pq.toString());
				if(isInterrupted()) break;
				downloadExhaustive();
			}

			// Successful finished exhaustive download if we get here
			long time = System.currentTimeMillis() - start;
			String value = "Read " + count + " websites // " + time/1000 + " sec // " + byte_count + " bytes";
			if(byte_count != 0) displayMessage(value);
			else displayMessage("Error: Malformed URL");

			parent.semaphore.release(); //let another thread run

		} catch (InterruptedException e) {
			//stopped before it started (couldnt get permit from semaphore)
			displayMessage("");
		}
	}

	private String getBase(String URL) {
		int index = 0;
		for(int i = 0; i < WSC.extensions.length; i++) {
			int temp = URL.indexOf(WSC.extensions[i]);
			if(temp != -1) {
				index = temp + WSC.extensions[i].length();
				break;
			}
		}
		//look at everything before extension
		return "http://" + URL.substring(0, index).replaceAll("http:","").replace("http://", "").replace("//", "");
	}

	/* ----------------------------- search(String pageContent, String URL) -------------------------------
	 * Searches all the content of the page for links and then adds them to the priority blocking queue
	 * and also the  list of seen links
	 * */
	private void search(String content, String URL) {
		String base = getBase(URL);

		Pattern pattern = Pattern.compile("href=\"(.+?)\"");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String link = matcher.group(1);

			if(Utilities.isValidLink(link, base) && !seen.contains(link.toLowerCase())) {
				//if starts with /, add it to base
				String str = new String(link); //for memory issues
				System.out.println("\t\t" + base + str);

				if(link.charAt(0) == '/') pq.put(base + str);
				else pq.put(str);

				seen.add(str.toLowerCase());	
			}
		}
	}

	/* ----------------------------- incrementCount() -------------------------------
	 * A synchronized method that increments the count of number of pages read
	 * */
	synchronized private int incrementCount() {
		count = count + 1;
		return count;
	}

	/* ----------------------------- incrementByteCount() -------------------------------
	 * A synchronized method that increments the bytecount by a number
	 * */
	synchronized private long incrementByteCount(long num) {
		byte_count = byte_count + num;
		return byte_count;
	}


	//returns true if url was good, false if url was bad, calls itself and downloads exhaustively
	private void downloadExhaustive() {
		if(pq.isEmpty()) return;
		String URL = "";

		InputStream input = null;
		String line = null;
		try {
			URL = pq.take();
			System.out.println("++++++++++++++" + URL);
			URL url = new URL(URL);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(WSC.CONNECTION_TIMEOUT);

			connection.connect();
			input = connection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(input));

			displayMessage("Working on site #" + incrementCount() + "...");

			line = br.readLine();
			String content = "";

			while (line != null) {
				incrementByteCount(line.length());
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

			search(content, URL);
			writeToFile(content, URL);

			content = null;
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {
			//show nothing
			//displayMessage("Could not read URL: " + URL);
			System.out.println("Could not read URL: " + URL);
		}
		catch(IOException ignored) {
			//show nothing
			//System.out.println("Could not read URL: " + URL);
			//displayMessage("Error: Parsing Problem"); //io exception, connection timeout
		} catch (InterruptedException e) {

			displayMessage("Thread interrupted");
		}

		finally {
			try{
				if (input != null) input.close(); //save resources
			}
			catch(IOException ignored) {}
		}
	}

}

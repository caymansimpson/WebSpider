package crawler;

public class WSC { 
	
	/* ------------------------------- WSC: Web Spider Constants -----------------------*/
	
	public static int DEFAULT_TABLE_SIZE = 20;
	public static String[] TABLE_HEADERS = {"URL", "Status"};
	public static int TABLE_SCROLLPANE_WIDTH = 600;
	public static int TABLE_SCROLLPANE_HEIGHT = 300;
	
	public static String DOWNLOAD_BUTTON_STOP_TEXT = "  Stop  ";
	public static String DOWNLOAD_BUTTON_DEFAULT_TEXT = "Download";
	public static String[] extensions = {".com", ".org", ".net", ".int", ".edu", ".gov", ".mil", ".arpa"};

	public static int CONNECTION_TIMEOUT = 5000;
	
	public static String DEFAULT_DIRECTORY = "WebData";
	
	
	public static String HELP = "<html><body><p style='width: 225px;'><p>Welcome to Web Spider!</p>" +
			"<p></p>"+
			"<p style='width: 225px;'>This java application lets you download any URL" +
			" you want and save it to a local directory. First, you can enter as many links" +
			" as you want into the table above. You can also give a text file with the links" +
			" already stored. Autoparsing is not yet available, so the links will have to be" +
			" separated by line breaks. Then, select a directory to store your text file" + 
			", select the number of threads you'd like to help your download (8-10 is optimal) " +
			"and click \'Download\'! If you'd like, you can click \"Exhaustive Crawl\"" +
			" to exahustively crawl your website until you stop the program. "+
			" You can press \"Stop\" to stop the operation at any time you'd like. The" +
			" program will store your downloaded websites in a text file of your chosen directory " +
			" with the name \"Web Data.txt\". If you have problems, contact cayman@stanford.edu</p>" + 
			"<p></p>"+
			"<p>Have fun and don't do anything illegal!</p></body></html>";
	
}

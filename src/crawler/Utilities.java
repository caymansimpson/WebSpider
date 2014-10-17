package crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Utilities {
	/* ------------------------------------------------ Utilities -----------------------------------------------------
	 * 
	 * A bunch of useful functions that are here instead of their home class to declutter my code.
	 * */

	public static boolean isNumeric(String str) {  
		try  {  
			Integer.parseInt(str);
			return true;  
		}  
		catch(NumberFormatException error) { return false; }  
	}

	public static String format(String url) {
		String toReturn = "";
		if(!url.contains("http://")) {
			if(!url.contains("www.")) {
				toReturn = "www." + toReturn;
			}
			toReturn = "http://" + toReturn;
		}
		toReturn = toReturn + url;
		return toReturn;
	}

	public static boolean isValidLink(String link, String base) {
		if(link.contains(":") && !link.contains("http:")) return false; //mailto: link
		if((link.contains("www") || link.contains("http:")) && !link.contains(base)) return false; //link to other site
		if(!link.contains(".html") && !link.contains(".jsp") && !link.contains(".asp") && link.contains(".")) return false; //has some other extension than .html or on server
		if(link.equals("/")) return false;
		if(link.contains("#")) return false;
		if(link.equals("")) return false;
		return true;
	}
}

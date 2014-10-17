package crawler; //written by Cayman Simpson ©

import java.util.HashMap;

public class StringParser {
	private static String BEGIN_NAME = "<h1 class=\"fn recipe_title\">";
	private static String END_NAME = "</h1></span>";
	private static String BEGIN_RATING = "rating<div><div style=\"font-size:36px; font-weight: bold;\">";
	private static String END_RATING = "</div><div class=\"average\"";
	private static String BEGIN_AMOUNT = "<span class=\"ingredient\"><span class=\"amount\">";
	private static String END_AMOUNT = "</span> <span class=\"name\"><a href=";
	private static String BEGIN_INGREDIENT = ">";
	private static String END_INGREDIENT = "</a></span></span>";

	public static String parse(String content, String URL) {
		//manual parsing for drinksmixer.com, Derin, this is where all the html goes through so you can parse here
		if(true)return content;
		if(!content.contains("rating<div><div style=\"font-size:36px; font-weight: bold;\">") || !content.contains("<h1 class=\"fn recipe_title\">")) {
			return "";
		}
		
		String drinkName = "";
		String rating = "NA";
		HashMap<String, String> ingredients = new HashMap<String, String>();

		int index = content.indexOf(BEGIN_NAME) + BEGIN_NAME.length();
		int endIndex = content.indexOf(END_NAME, index);

		drinkName = content.substring(index, endIndex);
		
		content = content.substring(endIndex); //so we don't have to search through entire string
		
		if(content.contains(BEGIN_RATING)) { //if there's a rating, take it
			index = content.indexOf(BEGIN_RATING) + BEGIN_RATING.length();
			endIndex = content.indexOf(END_RATING, index);
			rating = content.substring(index, endIndex);
		}
		
		index = content.indexOf(BEGIN_AMOUNT);
		while(index != -1) {
			index += BEGIN_AMOUNT.length();

			String amount = "";
			String ingredient = "";
			
			endIndex = content.indexOf(END_AMOUNT, index);
			
			amount = content.substring(index, endIndex);
			
			content = content.substring(endIndex + END_AMOUNT.length());
			
			index = content.indexOf(BEGIN_INGREDIENT) + BEGIN_INGREDIENT.length();
			endIndex = content.indexOf(END_INGREDIENT, index);
			ingredient = content.substring(index, endIndex);
			
			ingredients.put(ingredient, amount);
			
			content = content.substring(endIndex);
			index = content.indexOf(BEGIN_AMOUNT); //recycle
		}

		return "Drink: " + drinkName + "\nRating: " + rating + "\nIngredients: " + mapToString(ingredients);
	}
	
	
	public static String mapToString(HashMap<String, String> map) {
		String str = "";
		
		for(String entry : map.keySet()) {
			str += entry + "," + map.get(entry) + "||";
		}
		
		return str.substring(0, str.length() - 2);
	}
}

//example html for parsing:
//<h1 class="fn recipe_title">The Kicker recipe</h1></span>
//rating<div><div style="font-size:36px; font-weight: bold;">10.0</div><div class="average"
//<span class="ingredient"><span class="amount">1 part</span> <span class="name"><a href="/desc192.html">banana liqueur</a></span></span><br>
//<span class="ingredient"><span class="amount">1 part</span> <span class="name"><a href="/desc292.html">Kahlua&reg; coffee liqueur</a></span></span><br>
//<span class="ingredient"><span class="amount">1 part</span> <span class="name"><a href="/desc48.html">milk</a></span></span>
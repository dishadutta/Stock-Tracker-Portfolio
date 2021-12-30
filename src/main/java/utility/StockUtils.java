package main.java.utility;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StockUtils {

	public StockUtils() {}
	
	private static List<String> stockList;

	final static String STOCK_LIST_FILE = System.getProperty("user.dir") + File.separator + "src" + 
											File.separator + "main" + File.separator + "resources" + 
											File.separator + "nasdaqlisted.csv";
	
	private static void initStockList() {
		if (stockList == null) {
			stockList = getStockListFromFile(STOCK_LIST_FILE);
		}
	}
	
	private static List<String> getStockListFromFile(String filePath) {
		List<String> data = new ArrayList<String>();
		try (FileReader fileReader = new FileReader(filePath); 
			BufferedReader reader = new BufferedReader(fileReader)){
			String stockName = null;
			while ((stockName = reader.readLine()) != null) {
				data.add(stockName);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return data;
	}
	
	public static ObservableList<String> getMatchedQuery(String query) {
		if (query.isEmpty()) {
			return null;
		}
		initStockList();
		if (stockList != null) {
			List<String> matchedResult = stockList.stream().filter(it -> checkContainCaseInsensitive(it, query)).collect(Collectors.toList());
			return FXCollections.observableArrayList(matchedResult);
		}
		else return FXCollections.observableArrayList("Unexpected error happed!");
	}

	public static boolean checkContainCaseInsensitive(String s1, String s2) {
		return Pattern.compile(Pattern.quote(s2), Pattern.CASE_INSENSITIVE).matcher(s1).find();
	}

	public static double calculateStockValue(yahoofinance.Stock stock, int amount) {
		if (stock == null) {
			return 0;
		}
		return stock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue()*amount;
	}
}

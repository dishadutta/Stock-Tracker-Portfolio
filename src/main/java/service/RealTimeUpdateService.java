package main.java.service;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import main.java.model.Stock;
import main.java.utility.Utils;

import java.io.IOException;

public class RealTimeUpdateService extends ScheduledService<ObservableList<Stock>> {
	String[] stockSymbols;
	
	public RealTimeUpdateService(String[] stockSymbols) {
		this.stockSymbols = stockSymbols;
	}
	
	@Override
	protected Task<ObservableList<Stock>> createTask() {
		return new Task<ObservableList<Stock>>() {
			@Override
			protected ObservableList<Stock> call() throws IOException {
				 return Utils.getMultipleStockData(stockSymbols);
			}
		};
	}
}

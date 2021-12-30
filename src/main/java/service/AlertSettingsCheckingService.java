package main.java.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.UserStock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
public class AlertSettingsCheckingService extends ScheduledService<ObservableList<UserStock>> {
	
	private UserStockManager<UserStock> usManager;
	private Integer userID;

	public AlertSettingsCheckingService(Integer userID, UserStockManager<UserStock> usManager) {
		this.userID = userID;
		this.usManager = usManager;
	}

	private BigDecimal calculatePreviousCombinedValueThreshold(yahoofinance.Stock stock) {
		List<Stock> stocks = usManager.findStocks(userID, stock.getSymbol());
		if (stocks != null && stocks.size() > 0) {
			BigDecimal total = new BigDecimal(0);
			for (Stock s : stocks) {
				total = total.add(s.getPrice().multiply(BigDecimal.valueOf(s.getAmount())));
			}
			return total;
		}
		return new BigDecimal(-1);
	}

	private BigDecimal calculateCurrentCombinedValueThreshold(yahoofinance.Stock stock) {
		List<Stock> stocks = usManager.findStocks(userID, stock.getSymbol());
		BigDecimal total = new BigDecimal(-1);
		if (stocks != null && stocks.size() > 0) {
			int totalAmount = 0;
			for (Stock s : stocks) {
				totalAmount += s.getAmount();
			}
			total = stock.getQuote().getPrice().multiply(BigDecimal.valueOf(totalAmount));
		}
		return total;
	}
	
	@Override
	protected Task<ObservableList<UserStock>> createTask() {
		return new Task<ObservableList<UserStock>>() {
			@Override
			protected ObservableList<UserStock> call() throws IOException {
				List<UserStock> userStocks = usManager.findWithAlertSettingsOn(userID);
				if (userStocks == null) {
					return null;
				}
				BigDecimal defaultThreshold = new BigDecimal(-1);
				List<UserStock> removeList = new ArrayList<UserStock>();
				for (UserStock us : userStocks) {
					boolean isValueThresholdCrossed = false;
					boolean isCombinedValueThresholdCrossed = false;
					boolean isNetProfitThresholdCrossed = false;
					Stock stock = us.getStock();
					yahoofinance.Stock yahooStock = YahooFinance.get(stock.getStockCode());
					BigDecimal curPrice = yahooStock.getQuote().getPrice();
					if (us.getValueThreshold().compareTo(defaultThreshold) > 0) {
						isValueThresholdCrossed = isThresholdCrossed(us.getValueThreshold(), us.getStock().getPrice(), curPrice);
					}

					BigDecimal previousCombinedValue = calculatePreviousCombinedValueThreshold(yahooStock);
					BigDecimal curCombinedValue = calculateCurrentCombinedValueThreshold(yahooStock);
					if (us.getCombinedValueThreshold().compareTo(defaultThreshold) > 0) {
						isCombinedValueThresholdCrossed = isThresholdCrossed(us.getCombinedValueThreshold(), previousCombinedValue, curCombinedValue);
					}

					if (us.getNetProfitThreshold().compareTo(defaultThreshold) > 0) {
						isNetProfitThresholdCrossed = isNetThresholdCrossed(us.getNetProfitThreshold(), previousCombinedValue, curCombinedValue);
					}

					us.setCurrentValueThreshold(curPrice);

					if (isValueThresholdCrossed || isCombinedValueThresholdCrossed || isNetProfitThresholdCrossed) {
						if (isValueThresholdCrossed) {
							us.setValueThreshold(defaultThreshold);
						}
						if (isCombinedValueThresholdCrossed) {
							us.setCombinedValueThreshold(defaultThreshold);
						}
						if (isNetProfitThresholdCrossed) {
							us.setNetProfitThreshold(defaultThreshold);
						}
						usManager.update(us);
					} else {
						removeList.add(us);
					}
				}
				userStocks.removeAll(removeList);
				return FXCollections.observableArrayList(userStocks);
			}
		};
	}

	private boolean isThresholdCrossed(BigDecimal threshold, BigDecimal previous, BigDecimal curPrice) {
		// At the time user set threshold, the value of threshold is lesser than the stock price
		// So, we check if the current price went down below threshold or not
		curPrice = new BigDecimal(1000);
		if (threshold.compareTo(previous) < 0) {
			if (threshold.compareTo(curPrice) > 0) {
				return true;
			}
		} else {
			if (threshold.compareTo(curPrice) < 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isNetThresholdCrossed(BigDecimal threshold, BigDecimal previousPrice, BigDecimal curPrice) {
		BigDecimal difference = curPrice.subtract(previousPrice);
		if (difference.compareTo(BigDecimal.ZERO) > 0) {
			if (threshold.compareTo(difference) < 0) {
				return true;
			}
		} else {
			BigDecimal minusOne = new BigDecimal(-1);
			if (threshold.compareTo(difference.multiply(minusOne)) < 0) {
				return true;
			}
		}
		return false;
	}
}

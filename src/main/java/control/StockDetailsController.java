package main.java.control;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.common.CommonDefine.Interval;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.UserStock;
import main.java.model.UserStockId;
import main.java.utility.AlertFactory;
import main.java.utility.HoveredThresholdNode;
import main.java.utility.Utils;
import yahoofinance.histquotes.HistoricalQuote;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class StockDetailsController extends BaseController implements Initializable {
	
	@FXML private Label companyLB;
	@FXML private Label stockCodeAndTimeLB;
	@FXML private Label currentPriceLB;
	@FXML private Label priceChangeLB;
	@FXML private AnchorPane lineChartAP;
	@FXML private LineChart<String, Number> stockLineChart;
	@FXML private HBox parentHB;
	JFXTextField quantityTF;
	int quantity;
	
	RealTimeUpdateService service;

	@FXML private Label stockCodeLB;
	@FXML private Label buyPriceLB;
	@FXML private JFXComboBox<Object> quantityCB;
	@FXML JFXTextField currentBalanceTF;
	@FXML JFXTextField subTotalTF;
	@FXML JFXTextField remainBalanceTF;
	@FXML JFXButton buyStockButton;
	@FXML private Label dayLow;
	@FXML private Label dayHigh;
	@FXML private Label volume;
	@FXML private Label marketCapValue;
	@FXML private Label peRatio;
	@FXML private Label eps;
	@FXML private Label oneWeekLB;
	@FXML private Label oneMonthLB;
	@FXML private Label threeMonthLB;
	@FXML private Label sixMonthLB;
	@FXML private Label oneYearLB;
	
	private Label selectedLB;
	
	private yahoofinance.Stock yahooStock;
	private Interval interval;

	public StockDetailsController() {
		yahooStock = null;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		service = new RealTimeUpdateService();
		service.setPeriod(Duration.minutes(2));
		quantityCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				if (newValue != null) {
					try {
						quantity = Integer.parseInt(newValue.toString());
						estimateBuyingCost(quantity);
					} catch (NumberFormatException e) {
						parentHB.getChildren().remove(quantityCB);
						quantityTF = new JFXTextField();
						quantityTF.setPrefHeight(27.0);
						quantityTF.setPrefWidth(63);
						parentHB.getChildren().add(2, quantityTF);
						
						quantityTF.textProperty().addListener(new ChangeListener<String>() {

							@Override
							public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
								if (!newValue.matches("\\d*")) {
									quantityTF.setText(newValue.replaceAll("[^\\d]", ""));
								} else if (!quantityTF.getText().isEmpty()) {
									quantity = Integer.parseInt(quantityTF.getText());
									estimateBuyingCost(quantity);
								}
							}
						});
					}
				}
			}
		});

		interval = Interval.ONE_MONTH;
		oneWeekLB.setOnMouseClicked(event -> {
			interval = Interval.ONE_WEEK;
			handleOptionSelected(oneWeekLB);
		});
		oneMonthLB.setOnMouseClicked(event -> {
			interval = Interval.ONE_MONTH;
			handleOptionSelected(oneMonthLB);
		});
		threeMonthLB.setOnMouseClicked(event -> {
			interval = Interval.THREE_MONTH;
			handleOptionSelected(threeMonthLB);
		});
		sixMonthLB.setOnMouseClicked(event -> {
			interval = Interval.SIX_MONTH;
			handleOptionSelected(sixMonthLB);
		});
		oneYearLB.setOnMouseClicked(event -> {
			interval = Interval.ONE_YEAR;
			handleOptionSelected(oneYearLB);
		});
	}

	private void estimateBuyingCost(int quantity) {
		System.out.println("Selected item: " + quantity);
		double price = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue()*quantity;
		double remainingBalance = user.getAccount().getBalance() - price;
		buyStockButton.setDisable(remainingBalance < 0);
		subTotalTF.setText("- $" + Utils.formatCurrencyDouble(price));
		remainBalanceTF.setText("$" + Utils.formatCurrencyDouble(remainingBalance));
	}

	private void handleOptionSelected(Label lb) {
		if (lb != selectedLB) {
			setSelectedStyle(lb);
			removeSelectedStyle(selectedLB);
			drawLineChart(interval);
			selectedLB = lb;
		}
	}
	
	private void setSelectedStyle(Label lb) {
		if (lb != null) {
			lb.setUnderline(true);
			lb.setTextFill(Color.RED);
		}
	}
	
	private void removeSelectedStyle(Label lb) {
		if (lb != null) {
			lb.setUnderline(false);
			lb.setTextFill(Color.BLACK);
		}
	}

	public void setStock(yahoofinance.Stock stock) {
		this.yahooStock = stock;
		// Real-time update starts when user select a Stock
		service.setStockCode(stock);
		if (stock == null && service != null) {
			service.cancel();
		} else {
			service.start();
		}
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				System.out.println("Update new stock changes...");
				yahooStock = (yahoofinance.Stock) event.getSource().getValue();
				// Update values in GUIs
				updateStockData();
			}
		});
	}
	
	public yahoofinance.Stock getYahooStock() {
		return yahooStock;
	}
	
	private void initCompanyName() {
		companyLB.setText(yahooStock.getName());
	}
	
	private void initStockCodeAndTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm a");
		Date date = new Date();
		StringBuilder sb = new StringBuilder("");
		sb.append(yahooStock.getSymbol()).append(" - ").append(dateFormat.format(date));
		String finalDisplay = sb.toString();
		stockCodeAndTimeLB.setText(finalDisplay);
	}
	
	private void initCurrentPrice() {
		currentPriceLB.setText(yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).toString());
	}
	
	private void initPriceChange() {
		double priceChangeInMoney = yahooStock.getQuote().getChange().setScale(2, RoundingMode.CEILING).doubleValue();
		double priceChangeInPercent = yahooStock.getQuote().getChangeInPercent().setScale(2, RoundingMode.CEILING).doubleValue();//toString().substring(1); // Remove sign (- or +)
		
		StringBuilder sb = new StringBuilder("");
		sb.append(priceChangeInMoney).append(" (").append(priceChangeInPercent).append("%)");
		String finalDisplay = sb.toString(); //yahooStock.getSymbol() + " - " + dateFormat.format(date);

		priceChangeLB.setText(finalDisplay);

		BigDecimal priceChange = yahooStock.getQuote().getChange();
		if (priceChange.compareTo(BigDecimal.ZERO) == 0) {
			priceChangeLB.setTextFill(Color.BLACK);
		} else if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
			priceChangeLB.setTextFill(Color.GREEN);
		} else { // Price decreased
			priceChangeLB.setTextFill(Color.RED);
		}
	}
	
	private void initDayLow() {
		dayLow.setText(Utils.formatCurrencyNumber(yahooStock.getQuote().getDayLow()));
	}
	
	private void initDayHigh() {
		dayHigh.setText(Utils.formatCurrencyNumber(yahooStock.getQuote().getDayHigh()));
	}
	
	private void initVolume() {
		volume.setText(Utils.formatCurrencyNumber(new BigDecimal(yahooStock.getQuote().getVolume())));
	}
	
	private void initMarketCap() {
		marketCapValue.setText(Utils.formatCurrencyNumber(yahooStock.getStats().getMarketCap()));
	}
	
	private void initPriceEarnRatio() {
		peRatio.setText(Utils.formatCurrencyNumber(yahooStock.getStats().getPe()));
	}
	
	private void initEarnPerShare() {
		eps.setText(Utils.formatCurrencyNumber(yahooStock.getStats().getEps()));
	}
	
	/**
	 * Update stock data displayed in page
	 */
	public void updateStockData() {
		initCompanyName();
		initStockCodeAndTime();
		initCurrentPrice();
		initPriceChange();
		initDayLow();
		initDayHigh();
		initVolume();
		initMarketCap();
		initPriceEarnRatio();
		initEarnPerShare();
		handleOptionSelected(oneMonthLB);

		stockCodeLB.setText(yahooStock.getSymbol());
		buyPriceLB.setText(yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).toString());
		ObservableList<Object> options = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, "10+");
		quantityCB.setItems(options);
		currentBalanceTF.setText("$" + Utils.formatCurrencyDouble(user.getAccount().getBalance()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void drawLineChart(Interval interval) {
		if (yahooStock != null) {
			List<HistoricalQuote> historyQuotes = null;
			// Get stock history within one year
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();
			SimpleDateFormat dfm;
			
			switch(interval) {
				case ONE_WEEK: // Nearest week
					from.add(Calendar.DAY_OF_MONTH, -7);
					dfm = new SimpleDateFormat("EEE");
					break;
				case ONE_MONTH: // Nearest month
					from.add(Calendar.MONTH, -1);
					dfm = new SimpleDateFormat("EEE, MMM dd");
					break;
				case THREE_MONTH: // Nearest 3 months
					from.add(Calendar.MONTH, -3);
					dfm = new SimpleDateFormat("EEE, MMM dd");
					break;
				case SIX_MONTH: // Nearest 6 months
					from.add(Calendar.MONTH, -6);
					dfm = new SimpleDateFormat("EEE, MMM dd");
					break;
				case ONE_YEAR: //Nearest year
					from.add(Calendar.YEAR, -1);
					dfm = new SimpleDateFormat("MMM dd, yyyy");
					break;
				default:
					return;
			}
			System.out.println("From: " + from);
			System.out.println("From: " + to);
			try {
				historyQuotes = yahooStock.getHistory(from, to, yahoofinance.histquotes.Interval.DAILY);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			XYChart.Series series = new XYChart.Series();
			series.setName("Stock Price");

			double lowerBound = Double.MAX_VALUE;
			double upperBound = Double.MIN_VALUE;

			for (int index = historyQuotes.size() - 1; index >= 0; index --) {
				HistoricalQuote quote = historyQuotes.get(index);
				String date = dfm.format(quote.getDate().getTime());
				double price = quote.getOpen().setScale(2, RoundingMode.CEILING).doubleValue();

				if (price != 0 && lowerBound > price) {
					lowerBound = price;
				}
				if (upperBound < price) {
					upperBound = price;
				}

				XYChart.Data<String, Number> data = new XYChart.Data<String, Number>(date, price);
				data.setNode(new HoveredThresholdNode(date, price));
				series.getData().add(data);
			}

			CategoryAxis xAxis = new CategoryAxis();
			lowerBound = Math.floor(lowerBound);
			upperBound = Math.floor(upperBound);
			
			double range = upperBound - lowerBound;
			int tickCount = 10;
			double unroundedTickSize = range/(tickCount-1);
			double x = Math.ceil(Math.log10(unroundedTickSize)-1);
			double pow10x = Math.pow(10, x);
			double roundedTickRange = Math.ceil(unroundedTickSize / pow10x) * pow10x;
			double padding = Math.ceil(lowerBound / 15);
			roundedTickRange += Math.ceil(lowerBound/20);
			lowerBound = roundedTickRange * Math.ceil(lowerBound/roundedTickRange) - padding;
			upperBound = roundedTickRange * Math.ceil(1 + upperBound/roundedTickRange) + padding;
			NumberAxis yAxis = new NumberAxis(lowerBound, upperBound, roundedTickRange);
			
			VBox parent = (VBox)stockLineChart.getParent();
			parent.getChildren().remove(stockLineChart);
			stockLineChart = new LineChart<String, Number>(xAxis, yAxis);
			stockLineChart.setTitle("Yahoo Finance - Stock Tracker");
			stockLineChart.setCursor(Cursor.CROSSHAIR);
	        stockLineChart.getData().add(series);
	        parent.getChildren().add(stockLineChart);
		}
	}

	@FXML private void buyStock(ActionEvent e) {
		if ((quantityCB != null && quantityCB.getSelectionModel().isEmpty()) || (quantityTF != null && quantityTF.getText().isEmpty())) {
			Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.NOT_SELECT_STOCK_AMOUNT_SMS);
			alert.showAndWait();
		} else {
			double curBal= user.getAccount().getBalance();
			double price = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue();
			double payment = quantity*price;
			double newBalance = curBal - payment;
			if (newBalance > 0) {
				Stock boughtStock = extractStock();
				Transaction t = boughtStock.getTransaction();
				t.setPayment(payment*-1);
				t.setBalance(newBalance);
				stockManager.add(boughtStock);
				user.getAccount().setBalance(newBalance);
				userManager.update(user);
				UserStockId userStockId = new UserStockId(boughtStock.getId(), user.getId());
				List<UserStock> userStocks = userStockManager.findUserStock(user.getId(), boughtStock.getStockCode());
				UserStock userStock = new UserStock(userStockId, boughtStock, user, CommonDefine.OWNED_STOCK);
				if (userStocks != null && !userStocks.isEmpty()) {
					UserStock us = userStocks.get(0);
					userStock.setValueThreshold(us.getValueThreshold());
					userStock.setCombinedValueThreshold(us.getCombinedValueThreshold());
					userStock.setNetProfitThreshold(us.getNetProfitThreshold());
				}
				userStockManager.add(userStock);
			} else {

				Alert alert = AlertFactory.generateAlert(AlertType.WARNING, CommonDefine.NOT_ENOUGH_BALANCE_TO_BUY_SMS);
				alert.showAndWait();
			}
			setupAfterBuyingStock();
		}
	}

	private void setupAfterBuyingStock() {
		Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.TRANSACTION_SUCCESSFUL_SMS);
		alert.showAndWait();
		if (quantityCB != null && quantityTF == null) {
			quantityCB.getSelectionModel().clearSelection();
		} else {
			parentHB.getChildren().remove(quantityTF);
			parentHB.getChildren().add(2, quantityCB);
			quantityCB.getSelectionModel().clearSelection();
		}
		subTotalTF.setText("");
		remainBalanceTF.setText("");
		currentBalanceTF.setText("$" + Utils.formatCurrencyDouble(user.getAccount().getBalance()));
		quantity = 0;
	}
	
	private Stock extractStock() {
		String stockName = yahooStock.getName();
		String stockCode = yahooStock.getSymbol();
		BigDecimal price = yahooStock.getQuote().getPrice();
		BigDecimal previousPrice = yahooStock.getQuote().getPreviousClose();
		
		Transaction transaction = new Transaction(user.getAccount(), new Date());
		Stock stock = new Stock(transaction, stockName, stockCode, quantity, CommonDefine.OWNED_STOCK, price, previousPrice);
		transaction.setStock(stock);
		return stock;
	}
	
	private static class RealTimeUpdateService extends ScheduledService<yahoofinance.Stock> {
		private yahoofinance.Stock stock;
		
		public void setStockCode(yahoofinance.Stock stock) {
			this.stock = stock;
		}
		
		@Override
		protected Task<yahoofinance.Stock> createTask() {
			return new Task<yahoofinance.Stock>() {
				@Override
				protected yahoofinance.Stock call() throws IOException {
					System.out.println("Start getting data stock: " + stock.getSymbol() + " ...");
					return yahoofinance.YahooFinance.get(stock.getSymbol());
				}
			};
		}
		
	}
}

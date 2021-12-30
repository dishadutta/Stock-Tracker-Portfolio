package main.java.control;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.TransactionWrapper;
import main.java.model.UserStock;
import main.java.utility.*;
import main.java.utility.AlertFactory;
import yahoofinance.YahooFinance;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;

public class PortfolioController extends BaseController implements Initializable, IController {
	@FXML private AnchorPane mainAP;
	@FXML private TabPane mainTP;
	@FXML private Pagination portfolioPagination;
	@FXML private Pagination transactionHistoryPagination;
	@FXML private Pagination summaryTransactionPagination;
	@FXML private JFXButton sellStockButton;
	@FXML private JFXButton exportButton;
	
	final static int rowsPerPage = 10;
	TableView<TransactionWrapper> portfolioTable;
	List<TransactionWrapper> portfolioTransactions;

	TableView<TransactionWrapper> historyTable;
	List<TransactionWrapper> historyTransactions;

	TableView<TransactionWrapper> summaryTable;
	List<TransactionWrapper> summaryTransactions;

	List<TransactionWrapper> performingTransactions = new ArrayList<TransactionWrapper>();
	
	public PortfolioController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Clear any selected stock when user open Portfolio
		performingTransactions.clear();

		mainTP.getSelectionModel().selectedItemProperty().addListener(listener -> {
			int selectedIndex = mainTP.getSelectionModel().getSelectedIndex();
			double preferWidth;
			switch(selectedIndex) {
				case 0:
					preferWidth = 760;
					portfolioPagination.setPrefWidth(preferWidth);
					break;
				case 1:
					preferWidth = 950;
					transactionHistoryPagination.setPrefWidth(preferWidth);
					break;
				case 2:
					preferWidth = 780;
					summaryTransactionPagination.setPrefWidth(preferWidth);
					break;
				default:
					preferWidth = 950;
					break;
			}
			mainAP.getScene().getWindow().setWidth(preferWidth);
			mainAP.setPrefWidth(preferWidth);
			mainTP.setPrefWidth(preferWidth);
		});
	}
	
	/**
	 * Initialize portfolio
	 */
	public void initPortfolio() {
		if (user != null) {
			portfolioTransactions = transactionManager.findTransactions(user.getId(), CommonDefine.OWNED_STOCK);
			portfolioPagination.setPageCount(portfolioTransactions.size()/rowsPerPage + 1);
			portfolioTable = createPortfolioTable();
			portfolioPagination.setPageFactory(this::createPortfolioPage);
			sellStockButton.setOnAction(event -> {
				if (performingTransactions != null && performingTransactions.size() > 0) {
					Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, CommonDefine.SELL_STOCK_SMS);
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						sellMutipleStocks(performingTransactions);
						refreshTableView(performingTransactions);
						performingTransactions.clear();
					} else {
					}
				} else {
					Alert alert = AlertFactory.generateAlert(AlertType.WARNING, CommonDefine.NOT_SELECT_ANY_STOCK_SMS);
					alert.showAndWait();
				}
			});
			portfolioTable.setOnMouseClicked(eventHandler -> {
				if (2 == eventHandler.getClickCount()) {
					switchScreen(Screen.SELL_STOCK, " Stock", ResourceLocator.SELL_STOCK_VIEW);
				}
			});
		}
	}

	public void initTransactionHistory() {
		if (user != null) {
			historyTransactions = transactionManager.findTransactions(user.getId(), CommonDefine.TRANSACTION_STOCK);
			transactionHistoryPagination.setPageCount(historyTransactions.size()/rowsPerPage + 1);
			historyTable = createHistoryTable();
			transactionHistoryPagination.setPageFactory(this::createTransactionPage);
		}

		exportButton.setDisable(historyTransactions == null || historyTransactions.size() == 0);
		
		exportButton.setOnAction(event -> {
			DirectoryChooser dc = new DirectoryChooser();
			File selectedDir = dc.showDialog(mainAP.getScene().getWindow());
			boolean exportSuccessful = ExportUtils.writeToExcel(historyTransactions, selectedDir.getAbsolutePath());
			if (exportSuccessful) {
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, "Exported successfully!");
				alert.showAndWait();
			} else {
				Alert alert = AlertFactory.generateAlert(AlertType.ERROR, "Exported failed! Try again.");
				alert.showAndWait();
			}
		});
	}

	public void initTransactionSummary() {
		summaryTransactions = transactionManager.findSummaryTransactions(user.getId());
		setCurrentValue();
		summaryTransactionPagination.setPageCount(summaryTransactions.size()/rowsPerPage + 1);
		summaryTable = createSummaryTransactionTable();
		summaryTransactionPagination.setPageFactory(this::createSummaryPage);
	}

	private void setCurrentValue() {
		for (TransactionWrapper tw : summaryTransactions) {
			try {
				yahoofinance.Stock stock = YahooFinance.get(tw.getStockCode());
				double value = StockUtils.calculateStockValue(stock, tw.getStock().getAmount());
				tw.setTotalValue(value);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sellMutipleStocks(List<TransactionWrapper> performingTransactions) {
		if (null == performingTransactions || performingTransactions.size() <= 0) {
			return;
		}

		double curBalance = user.getAccount().getBalance();
		for (TransactionWrapper tran : performingTransactions) {
			try {
				curBalance += sellSingleStock(tran, curBalance);
			} catch (IOException e) {
				System.err.println("Transaction failed. Rollback.");
				e.printStackTrace();
			}
		}
		user.getAccount().setBalance(curBalance);
		userManager.update(user);
	}

	public double sellSingleStock(TransactionWrapper tran, double curBalance, int...soldAmount) throws IOException {
		double earnedAmount = 0;


		Stock stock = tran.getStock();
		Stock soldStock = new Stock(stock); // Clone data
		
		yahoofinance.Stock yahooStock = yahoofinance.YahooFinance.get(stock.getStockCode());

		if (soldAmount == null || soldAmount.length <= 0 || soldAmount[0] == stock.getAmount()) {
			earnedAmount = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue() * stock.getAmount();
		} else {
			earnedAmount = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue() * soldAmount[0];
			soldStock.setAmount(soldAmount[0]);
			Stock remainingStock = new Stock(stock); // Clone data
			remainingStock.setAmount(stock.getAmount() - soldAmount[0]);
			Transaction remainingTransaction = new Transaction(user.getAccount(), new Date());

			remainingTransaction.setTransactionDate(tran.getTransaction().getTransactionDate());

			remainingStock.setTransaction(remainingTransaction);
			remainingTransaction.setStock(remainingStock);
			stockManager.add(remainingStock);
			userStockManager.add(user, remainingStock, CommonDefine.REMAINING_STOCK);
		}

		UserStock us = userStockManager.findUserStock(user.getId(), stock.getId());
		if (us.getStockType() == CommonDefine.REMAINING_STOCK) {
			userStockManager.remove(us);
		} else {
			us.setStockType(CommonDefine.TRANSACTION_STOCK);
			userStockManager.update(us);
		}

		Transaction soldTransaction = new Transaction(user.getAccount(), new Date());

		soldTransaction.setPayment(earnedAmount);

		soldTransaction.setBalance(curBalance + earnedAmount);
		soldStock.setPrice(yahooStock.getQuote().getPrice());
		soldStock.setTransaction(soldTransaction);
		soldTransaction.setStock(soldStock);
		stockManager.add(soldStock);
		userStockManager.add(user, soldStock, CommonDefine.TRANSACTION_STOCK);
		return earnedAmount;
	}

	private void refreshTableView(List<TransactionWrapper> performingTransactions) {
		for (TransactionWrapper t : performingTransactions) {
			if (portfolioTable.getItems().contains(t)) { // Remove sold stock (transaction)
				portfolioTable.getItems().remove(t);
			}
		}
		initTransactionHistory();
		initTransactionSummary();
	}
	
	@SuppressWarnings("unchecked")
	private TableView<TransactionWrapper> createPortfolioTable() {

		TableView<TransactionWrapper> table = new TableView<>();
		table.setEditable(true);
		
		TableColumn<TransactionWrapper, String> transDateCol = new TableColumn<>("Date");
		transDateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionDate()));
		transDateCol.setPrefWidth(90);
		
		TableColumn<TransactionWrapper, String> transTimeCol = new TableColumn<>("Time");
		transTimeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionTime()));
		transTimeCol.setPrefWidth(90);

		TableColumn<TransactionWrapper, String> stockCodeCol = new TableColumn<>("Stock Code");
		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCode()));
		stockCodeCol.setPrefWidth(80);

		TableColumn<TransactionWrapper, String> stockNameCol = new TableColumn<>("Company");
		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCompany()));
		stockNameCol.setPrefWidth(250);

		TableColumn<TransactionWrapper, String> stockPriceCol = new TableColumn<>("Bought Price");
		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPrice()));
		stockPriceCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> amountCol = new TableColumn<>("Quantity");
		amountCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAmount()));
		amountCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, Boolean> sellStockCol = new TableColumn<>();
		sellStockCol.setGraphic(new CheckBox());
		
		sellStockCol.setCellValueFactory(new PropertyValueFactory<TransactionWrapper, Boolean>("selected"));
		// Add event handler when users select a check box on table
		sellStockCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
		    @Override
		    public ObservableValue<Boolean> call(Integer index) {
		        TransactionWrapper item = table.getItems().get(index);
		        // Add/Remove item from the selected list
		        if (true == item.getSelected()) {
		            performingTransactions.add(item);
		        } else {
		        	performingTransactions.remove(item);
		        }
		        return item.selectedProperty();
		    }
		}));
		sellStockCol.setEditable(true);

		table.getColumns().addAll(sellStockCol, transDateCol,  transTimeCol, stockCodeCol, stockNameCol, stockPriceCol, amountCol);
		return table;
	}
	
	@SuppressWarnings("unchecked")
	private TableView<TransactionWrapper> createHistoryTable() {

		TableView<TransactionWrapper> table = new TableView<>();
		table.setEditable(true);
		
		TableColumn<TransactionWrapper, String> transDateCol = new TableColumn<>("Date");
		transDateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionDate()));
		transDateCol.setPrefWidth(90);
		
		TableColumn<TransactionWrapper, String> transTimeCol = new TableColumn<>("Time");
		transTimeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionTime()));
		transTimeCol.setPrefWidth(90);

		TableColumn<TransactionWrapper, String> stockCodeCol = new TableColumn<>("Stock Code");
		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCode()));
		stockCodeCol.setPrefWidth(80);

		TableColumn<TransactionWrapper, String> stockNameCol = new TableColumn<>("Company");
		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCompany()));
		stockNameCol.setPrefWidth(250);

		TableColumn<TransactionWrapper, String> stockPriceCol = new TableColumn<>("Price");
		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPrice()));
		stockPriceCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> amountCol = new TableColumn<>("Quantity");
		amountCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAmount()));
		amountCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> paymentCol = new TableColumn<>("Payment");
		paymentCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionPayment()));
		paymentCol.setPrefWidth(100);
		styleTableCell(paymentCol);

		TableColumn<TransactionWrapper, String> balanceCol = new TableColumn<>("Balance");
		balanceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBalance()));
		balanceCol.setPrefWidth(100);
		
		table.getColumns().addAll(transDateCol,  transTimeCol, stockCodeCol, stockNameCol, stockPriceCol, amountCol, paymentCol, balanceCol);
		return table;
	}
	
	@SuppressWarnings("unchecked")
	private TableView<TransactionWrapper> createSummaryTransactionTable() {

		TableView<TransactionWrapper> table = new TableView<>();
		table.setEditable(true);

		TableColumn<TransactionWrapper, String> stockCodeCol = new TableColumn<>("Stock Symbol");
		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCode()));
		stockCodeCol.setPrefWidth(80);

		TableColumn<TransactionWrapper, String> stockNameCol = new TableColumn<>("Company");
		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCompany()));
		stockNameCol.setPrefWidth(250);

		TableColumn<TransactionWrapper, String> amountCol = new TableColumn<>("Quantity");
		amountCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAmount()));
		amountCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> totalPrice = new TableColumn<>("Total Bought Price");
		totalPrice.setCellValueFactory(param -> new SimpleStringProperty("$" + param.getValue().getTransactionPayment()));
		totalPrice.setPrefWidth(160);
		
		TableColumn<TransactionWrapper, String> curTotalPrice = new TableColumn<>("Current Stock Value");
		curTotalPrice.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTotalValue()));
		curTotalPrice.setPrefWidth(160);
		
		table.getColumns().addAll(stockCodeCol, stockNameCol, amountCol, totalPrice, curTotalPrice);
		return table;
	}
	
	private Node createPortfolioPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, portfolioTransactions.size());
        portfolioTable.setItems(FXCollections.observableArrayList(portfolioTransactions.subList(fromIndex, toIndex)));
        return new BorderPane(portfolioTable);
    }
	
	private Node createTransactionPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, historyTransactions.size());
        historyTable.setItems(FXCollections.observableArrayList(historyTransactions.subList(fromIndex, toIndex)));
        return new BorderPane(historyTable);
    }
	
	private Node createSummaryPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, summaryTransactions.size());
        summaryTable.setItems(FXCollections.observableArrayList(summaryTransactions.subList(fromIndex, toIndex)));
        return new BorderPane(summaryTable);
    }

	@Override public void makeNewStage(Screen target, String stageTitle, String url) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
		try {
			root = (Parent)loader.load();
		} catch (IOException e) {
        	System.err.println("Could not load url: " + url);
        	e.printStackTrace();
        	return;
        }
		TransactionWrapper tw = portfolioTable.getSelectionModel().getSelectedItem();
		switch(target) {
			case SELL_STOCK:
				SellStockController controller = loader.<SellStockController>getController();
				controller.setUser(user);
				controller.init(tw);
				break;
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
				stockController.setUser(user);
				// Find selected stock in Java API
				// TODO: Think about better way to find the stock given stock code
				yahoofinance.Stock yahooStock = null;
				try {
					yahooStock = YahooFinance.get(tw.getStockCode(), false);
				} catch (IOException e) {
					System.err.println("Stock code is invalid: " + tw.getStockCode());
					e.printStackTrace();
					return;
				}
				stockController.setStock(yahooStock);
				stockController.updateStockData();
				break;
			default:
				return;
		}
		Stage newStage = StageFactory.generateStage(stageTitle);
		newStage.setScene(new Scene(root));
		newStage.show();
	}
	
	@Override
	public void switchScreen(Screen target, String title, String url) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
		try {
			root = (Parent)loader.load();
		} catch (IOException e) {
        	System.err.println("Could not load url: " + url);
        	e.printStackTrace();
        	return;
        }
		TransactionWrapper tw = portfolioTable.getSelectionModel().getSelectedItem();
		switch(target) {
			case SELL_STOCK:
				SellStockController controller = loader.<SellStockController>getController();
				controller.setUser(user);
				controller.init(tw);
				break;
			default:
				return; // Move to undefined target. Should throw some exception?
		}
		Stage curStage = (Stage)mainAP.getScene().getWindow();
		curStage.setScene(new Scene(root));
		curStage.show();
	}
}

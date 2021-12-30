package main.java.control;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.User;
import main.java.model.UserStock;
import main.java.notification.EmailNotification;
import main.java.notification.PhoneNotification;
import main.java.service.AlertSettingsCheckingService;
import main.java.service.RealTimeUpdateService;
import main.java.utility.*;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class HomeController extends BaseController implements Initializable, Observer, IController {
	@FXML private StackPane homeSP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, String> priceCol;
	@FXML private TreeTableColumn<Stock, String> lastPriceCol;
	@FXML private TreeTableColumn<Stock, String> priceChangeCol;
	@FXML private TreeTableColumn<Stock, String> percentChangeCol;
	
	@FXML private JFXTextField searchTF;
	@FXML private StackPane sp;
	@FXML private JFXSpinner spinner;
	@FXML private ImageView addStock;
	
	private Stock stock; // The selected stock for alert
	private ObservableList<Stock> stocks;
	
	private ArrayList<String> stockSymbolsArrayList;
	private String[] stockSymbolsArray;
	
	private boolean isFirstLogin = false;
	
	RealTimeUpdateService stockUpdateService;
	AlertSettingsCheckingService alertSettingsService;
	
	PhoneNotification phoneNotif = new PhoneNotification();
	EmailNotification emailNotif = new EmailNotification();
	
	public ArrayList<String> getStocks() {
		return this.stockSymbolsArrayList;
	}

	private void initializeStockList() {

		List<UserStock> interestedStockList = userStockManager.findInterestedStockList(user.getId());

		if (interestedStockList == null || interestedStockList.size() <= 0) {
			isFirstLogin = true;
			stockSymbolsArrayList = new ArrayList<>(Arrays.asList(CommonDefine.DEFAULT_INTERESTED_STOCKS));
		} else {
			isFirstLogin = false;
			stockSymbolsArrayList = new ArrayList<String>();
			for(UserStock us : interestedStockList) {
				stockSymbolsArrayList.add(us.getStock().getStockCode());
			}
		}
	}

	public void populateData(String stockSymbol) {
		synchronizeStockList();
		String[] list = {stockSymbol};
		try {
			Stock newStock = (Stock) Utils.getMultipleStockData(list).get(0);

			stocks.add(0, newStock);

			stockManager.add(newStock);
			userStockManager.add(user, newStock, CommonDefine.INTERESTED_STOCK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
		stockTableView.setRoot(root);
		System.out.println("Finish adding!");
	}
	
	public void synchronizeStockList() {
		stockSymbolsArray = stockSymbolsArrayList.toArray(new String[0]);
	}

	@Override public void update() {
		if (stockUpdateService != null) {
			stockUpdateService.setPeriod(Duration.minutes(user.getStockUpdateTime()));
			stockUpdateService.restart();
		}
		if (alertSettingsService != null) {
			alertSettingsService.setPeriod(Duration.minutes(user.getAlertTime()));
			alertSettingsService.restart();
		}
	}
	
	@Override 
	public void setUser(User user) {
		this.user = user;
		initializeStockList();
		synchronizeStockList();
		startScheduleService();
	}

	private void startScheduleService() {
		startStockUpdateService();
		startAlertSettingsService();
	}

	private void startStockUpdateService() {
		stockUpdateService = new RealTimeUpdateService(stockSymbolsArray);
		stockUpdateService.setPeriod(Duration.minutes(user.getStockUpdateTime()));
		stockUpdateService.start();
		stockUpdateService.setOnSucceeded(event -> {
			System.out.println("Continue update...");
			spinner.setVisible(true);
			stocks = stockUpdateService.getValue();
			TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
			stockTableView.setRoot(root);
			spinner.setVisible(false);
			if (isFirstLogin) {
				System.out.println("This is the first time login!");
				for (Stock s : stocks) {
					stockManager.add(s);
					userStockManager.add(user, s, CommonDefine.INTERESTED_STOCK);
				}
			} else {
				System.out.println("This is not the first time login!");
			}
			System.out.println("Finish updating!");
		});
	}
	
	private void startAlertSettingsService() {
		alertSettingsService = new AlertSettingsCheckingService(user.getId(), userStockManager);
		alertSettingsService.setPeriod(Duration.minutes(user.getAlertTime()));
		alertSettingsService.start();
		alertSettingsService.setOnSucceeded(event -> {
			System.out.println("Checking alert settings...");
			ObservableList<UserStock> userStocks = alertSettingsService.getValue();
			notifyAlertToUser(userStocks);
		});
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addStock.setOnMouseClicked(event -> {
			makeNewStage(Screen.ADD_STOCK, "Add new stock", ResourceLocator.ADD_STOCK_VIEW);
		});
		// Initialize GUI
		stocks = FXCollections.observableArrayList();
		TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
		stockTableView.setRoot(root);
		stockTableView.setShowRoot(false);
		stockTableView.setEditable(true);
		// Set Cell Value Factory
		setCellValueStockCode();
		setCellValueCompanyName();
		setCellValueStockPrice();
		setCellFactoryLastPrice();
		setCellFactoryPriceChange();
		setCellFactoryPercentageChange();
		// Initialize context menu when user click right mouse on a row
		stockTableView.setRowFactory(ttv -> {
		    ContextMenu contextMenu = new ContextMenu();
		    MenuItem alertSettingsItem = new MenuItem("Alert Settings");
		    // ...
		    MenuItem removeItem = new MenuItem("Remove Stock");
		    // ...
		    contextMenu.getItems().addAll(alertSettingsItem, removeItem);
		    TreeTableRow<Stock> row = new TreeTableRow<Stock>() {
		        @Override
		        public void updateItem(Stock item, boolean empty) {
		            super.updateItem(item, empty);
		            if (empty) {
		                setContextMenu(null);
		            } else {
		                // configure context menu with appropriate menu items, 
		                // depending on value of item
		                setContextMenu(contextMenu);
		            }
		        }
		    };
		    alertSettingsItem.setOnAction(evt -> {
		        stock = row.getItem();
		        makeNewStage(Screen.ALERT_SETTINGS, CommonDefine.ALERT_SETTINGS_TITLE, ResourceLocator.ALERT_SETTINGS_VIEW);
		    });
		    removeItem.setOnAction(evt -> {
		    	String stockCode = row.getItem().getStockCode();
	    		Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, CommonDefine.REMOVE_STOCK_SMS);
	    		Optional<ButtonType> result = alert.showAndWait();
	    		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
	    			return;
	    		}
	    		stockSymbolsArrayList.remove(stockCode);
	    		synchronizeStockList();
	    		UserStock us = userStockManager.findInterestedStock(user.getId(), stockCode);
	    		Stock removedStock = us.getStock();
	    		userStockManager.remove(us);
	    		stockManager.remove(removedStock);
		    	TreeItem<Stock> treeItem = row.getTreeItem();
		    	treeItem.getParent().getChildren().remove(treeItem);
		    	stockTableView.getSelectionModel().clearSelection();
		    });
		    return row ;
		});
		searchTF.textProperty().addListener((o,oldVal,newVal)->{
			stockTableView.setPredicate(stock -> filterCriteria(stock, newVal));
		});

		stockTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2 && stockTableView.getCurrentItemsCount() > 0) {
					makeNewStage(Screen.STOCK_DETAILS, CommonDefine.STOCK_DETAILS_TITLE, ResourceLocator.STOCK_DETAILS_VIEW);
				}
			}
			
		});
	}

	@Override public void makeNewStage(Screen target, String stageTitle, String url) {
		Stage newStage = StageFactory.generateStage(stageTitle);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
		try {
			root = (Parent)loader.load();
		} catch (IOException e) {
        	System.err.println("Could not load url: " + url);
        	e.printStackTrace();
        	return;
        }
		switch(target) {
			case ALERT_SETTINGS:
				AlertSettingsController alertSettingsController = loader.<AlertSettingsController>getController();
				alertSettingsController.setUser(user);
				alertSettingsController.initAlertSettings(stock);
				break;
			case PORFOLIO:
				PortfolioController portfolioController = loader.<PortfolioController>getController();
				portfolioController.setUser(user);
				portfolioController.initPortfolio();
				portfolioController.initTransactionHistory();
				portfolioController.initTransactionSummary();
				break;
			case SETTINGS:
				SettingsController settingsController = loader.<SettingsController>getController();
				settingsController.setUser(user);
				settingsController.initUserInfo();
				settingsController.register(this);
				break;
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
				stockController.setUser(user);
				TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
				yahoofinance.Stock yahooStock = null;
				try {
					yahooStock = YahooFinance.get(item.getValue().getStockCode(), false);
				} catch (IOException e) {
					System.err.println("Stock code is invalid: " + item.getValue().getStockCode());
					e.printStackTrace();
					return;
				}
				stockController.setStock(yahooStock);
				stockController.updateStockData();
				newStage.setOnCloseRequest(eventHandler -> {
					stockController.setStock(null);
				});
				break;
			case ADD_STOCK:
				AddStockController addStockContrpller = loader.<AddStockController>getController();
				addStockContrpller.setHomeController(this);
				break;
			default:
				return;
		}
		newStage.setScene(new Scene(root));
		newStage.show();
	}

	@FXML private void openAccountSettings(ActionEvent event) {
		makeNewStage(Screen.SETTINGS, CommonDefine.USER_SETTINGS_TITLE, ResourceLocator.SETTINGS_VIEW);
	}
	
	@FXML private void openPorfolio(ActionEvent event) {
		makeNewStage(Screen.PORFOLIO, CommonDefine.PORTFOLIO_TITLE, ResourceLocator.PORTFOLIO_VIEW);
	}
	
	@FXML private void exit(ActionEvent event) {
		Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, "Do you really want to exit?");
		Optional<ButtonType> selection = alert.showAndWait();
		if (selection.isPresent() && selection.get().equals(ButtonType.OK)) {
			switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, ResourceLocator.LOGIN_VIEW);
			stockUpdateService.cancel();
			alertSettingsService.cancel();
		}
	}

	private boolean filterCriteria(TreeItem<Stock> stock, String value) {
		return stock.getValue().getStockCode().toLowerCase().contains(value.toLowerCase()) ||
				stock.getValue().getStockName().toLowerCase().contains(value.toLowerCase());
	}
	
	private void setCellValueStockCode() {
		stockCodeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockCode()));
	}
	
	private void setCellValueCompanyName() {
		companyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockName()));
	}
	
	private void setCellValueStockPrice() {
		priceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceString()));
	}
	
	private void setCellFactoryLastPrice() {
		lastPriceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPreviousPriceString()));
	}
	
	private void setCellFactoryPriceChange() {
		priceChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangeString()));

		priceChangeCol.setCellFactory(param -> new TreeTableCell<Stock, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				if (empty) {
					setText(null);
					setGraphic(null);
				}
				else {
					super.updateItem(item, empty);
					item = item.replaceAll(",", "");
					StringBuilder bd = new StringBuilder();
					double priceChange = Double.valueOf(item);
					// Price went down
					if (priceChange < 0) {
						setTextFill(Color.RED);
						bd.append("- ").append(item.replace("-", ""));
					} else if (priceChange > 0) { // Price went up
						setTextFill(Color.GREEN);
						bd.append("+ ").append(item);
					} else {
						setTextFill(Color.BLACK);
						bd.append(item);
					}
					setText(bd.toString());
				}
			}
		});
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangePercentString()));
	}

	private void notifyAlertToUser(ObservableList<UserStock> userStocks) {
		if (userStocks != null && userStocks.size() > 0) {
			sendMessageToUser(userStocks);
		}
	}

	private void sendMessageToUser(ObservableList<UserStock> userStocks) {
		HashSet<String> stockSet = new HashSet<String>();
		for (UserStock us : userStocks) {
			stockSet.add(us.getStock().getStockCode());
		}
		
		StringBuilder builder = new StringBuilder("Here is the list of stocks which have thresholds crossed: ");
		Iterator<String> it = stockSet.iterator();
		int size = stockSet.size();
		int i = 1;
		while (it.hasNext()) {
			builder.append(it.next());
			if (i != size) {
				builder.append(", ");
			}
			i ++;
		}
		String sms = builder.toString();
		phoneNotif.notify(sms, user.getPhoneNumber());
		emailNotif.notify(sms, user.getEmail());
	}

	@Override public void switchScreen(Screen target, String title, String url) {
        Parent root = null;
		try {
			switch (target) {
				case LOGIN:
					root = new FXMLLoader(getClass().getResource(url)).load();
					break;
				default:
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	  	Stage curStage = (Stage)homeSP.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
	}
}

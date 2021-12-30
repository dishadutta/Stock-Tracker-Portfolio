package main.java.control;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import main.java.common.CommonDefine;
import main.java.utility.AlertFactory;
import main.java.utility.StockUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddStockController extends BaseController implements Initializable {
	@FXML private VBox parentNode;
	@FXML private JFXTextField searchStockTF;
	@FXML private JFXListView<String> stockListView;
	@FXML private JFXButton addStockBt;
	
	private HomeController homeController;
	
	public AddStockController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addStockBt.setDisable(true);
		searchStockTF.textProperty().addListener((observable, oldValue, newValue) -> {
			ObservableList<String> results = StockUtils.getMatchedQuery(newValue);
			if (results != null) {
				stockListView.setItems(results);
			} else {
				stockListView.getItems().clear();
			}
		});

		stockListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		stockListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				addStockBt.setDisable(newValue == null);
			}
		});

		addStockBt.setOnAction(event->{
			String stock = stockListView.getSelectionModel().getSelectedItem();
			int separatorIndex = stock.indexOf("\t");
			if (separatorIndex == -1) {
				separatorIndex = stock.indexOf(" ");
			}
			stock = stock.substring(0, separatorIndex);
			ArrayList<String> stockSymbols = homeController.getStocks();
			if (stockSymbols.contains(stock)) {
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.ALREADY_HAD_STOCK_SMS);
				alert.showAndWait();
			} else {
				System.out.println("Add Stock: " + stock);
				stockSymbols.add(0, stock);
				homeController.populateData(stock);
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.ADDED_STOCK_SUCCESSFULLY_SMS);
				alert.showAndWait();
				parentNode.getScene().getWindow().hide();
			}
		});
	}

	public HomeController getHomeController() {
		return homeController;
	}
	public void setHomeController(HomeController homeController) {
		this.homeController = homeController;
	}
}

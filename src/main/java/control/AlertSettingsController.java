/**
 * 
 */
package main.java.control;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.UserStock;
import main.java.model.UserStockId;
import main.java.utility.AlertFactory;
import main.java.utility.CurrencyFormatter;
import main.java.utility.Utils;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author doquocanh-macbook
 *
 */
public class AlertSettingsController extends BaseController implements Initializable {

	@FXML private JFXToggleButton valueAlert;
	@FXML private JFXToggleButton combinedValueAlert;
	@FXML private JFXToggleButton netProfitAlert;

	@FXML private JFXTextField valueThreshold;
	@FXML private JFXTextField combinedValueThreshold;
	@FXML private JFXTextField netProfitThreshold;

	@FXML private JFXButton saveAlertSettingsButton;

	boolean valueAlertSwitchOn = false;
	boolean combinedValueAlertSwitchOn = false;
	boolean netProfitAlertSwitchOn = false;
	
	final String DEFAULT_THRESHOLD = "$0.00";

	private Stock selectedStock;
	public AlertSettingsController() {
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		valueThreshold.setTextFormatter(new CurrencyFormatter());
		combinedValueThreshold.setTextFormatter(new CurrencyFormatter());
		netProfitThreshold.setTextFormatter(new CurrencyFormatter());

		saveAlertSettingsButton.setOnAction(eventHandler -> {
			BigDecimal valueTh = new BigDecimal(-1);
			BigDecimal combinedTh = new BigDecimal(-1);
			BigDecimal netProfitTh = new BigDecimal(-1);
			boolean isSettingsUpdated = false;

			System.out.println("User owned stock: " + selectedStock.getStockCode());
			if (!valueAlert.isDisabled() && !valueThreshold.getText().equals(DEFAULT_THRESHOLD)) {
				isSettingsUpdated = true;
				Double value = Utils.parseCurrencyDouble(valueThreshold.getText());
				if (value != null)
					valueTh = new BigDecimal(value);
			}
			
			if (!combinedValueAlert.isDisabled() && !combinedValueThreshold.getText().equals(DEFAULT_THRESHOLD)) {
				isSettingsUpdated = true;
				Double value = Utils.parseCurrencyDouble(combinedValueThreshold.getText());
				if (value != null)
					combinedTh = new BigDecimal(value);
			}
			
			if (!netProfitAlert.isDisabled() && !netProfitThreshold.getText().equals(DEFAULT_THRESHOLD)) {
				isSettingsUpdated = true;
				Double value = Utils.parseCurrencyDouble(netProfitThreshold.getText());
				if (value != null)
					netProfitTh = new BigDecimal(value);
			}
			
			if (isSettingsUpdated) {
				List<UserStock> userStocks = userStockManager.findUserStock(user.getId(), selectedStock.getStockCode());
				if (userStocks != null && !userStocks.isEmpty()) {
					for (UserStock us : userStocks) {
						if (valueTh.compareTo(BigDecimal.ZERO) > 0)
							us.setValueThreshold(valueTh);
						if (combinedTh.compareTo(BigDecimal.ZERO) > 0) 
							us.setCombinedValueThreshold(combinedTh);
						if (netProfitTh.compareTo(BigDecimal.ZERO) > 0)
							us.setNetProfitThreshold(netProfitTh);
						// Update to db
						userStockManager.update(us);
					}
				} else {
					System.out.println("User doesn't own stock: " + selectedStock.getStockCode());
					stockManager.add(selectedStock);
					UserStockId userStockId = new UserStockId(selectedStock.getId(), user.getId());
					UserStock userStock = new UserStock(userStockId, selectedStock, user, valueTh, combinedTh, netProfitTh);
					userStockManager.add(userStock);
				}
				// Display successful message to user
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.UPDATE_ALERT_SETTINGS_SMS);
				alert.showAndWait();
			}
		});
	}

	public void initAlertSettings(Stock stock) {
		selectedStock = stock;
		if(userStockManager.hasStock(user.getId(), selectedStock.getStockCode())) {
			combinedValueAlert.setDisable(false);
			netProfitAlert.setDisable(false);
		} else {
			combinedValueAlert.setDisable(true);
			netProfitAlert.setDisable(true);
		}
		valueThreshold.setEditable(valueAlertSwitchOn);
		combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
		netProfitThreshold.setEditable(netProfitAlertSwitchOn);

		valueAlert.setOnAction(eventHandler -> {
			if (valueAlertSwitchOn) {
				valueThreshold.setText(DEFAULT_THRESHOLD);
			}
			valueAlertSwitchOn = !valueAlertSwitchOn;
			valueThreshold.setEditable(valueAlertSwitchOn);
		});

		combinedValueAlert.setOnAction(eventHandler -> {
			if (combinedValueAlertSwitchOn) {
				combinedValueThreshold.setText(DEFAULT_THRESHOLD);
			}
			combinedValueAlertSwitchOn = !combinedValueAlertSwitchOn;
			combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
		});

		netProfitAlert.setOnAction(eventHandler -> {
			if (netProfitAlertSwitchOn) {
				netProfitThreshold.setText(DEFAULT_THRESHOLD);
			}
			netProfitAlertSwitchOn = !netProfitAlertSwitchOn;
			netProfitThreshold.setEditable(netProfitAlertSwitchOn);
		});
	}
}

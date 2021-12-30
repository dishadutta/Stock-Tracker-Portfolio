package main.java.control;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import main.java.utility.CurrencyFormatter;
import main.java.utility.PhoneNumberFormatter;
import main.java.utility.Utils;
import main.java.utility.ValidationUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingsController extends BaseController implements Initializable, Subject {

	@FXML private JFXTextField firstNameTF;
	@FXML private JFXTextField lastNameTF;
	@FXML private JFXTextField emailTF;
	@FXML private JFXTextField phoneNumberTF;
	@FXML private PasswordField currentPasswordPF;
	@FXML private PasswordField newPasswordPF;
	@FXML private PasswordField confirmPasswordPF;
	@FXML private Text firstNameError;
	@FXML private Text lastNameError;
	@FXML private Text emailError;
	@FXML private Text currentPasswordError;
	@FXML private Text passwordError;
	@FXML private Text confirmPasswordError;
	@FXML private Text phoneNumberError;
	@FXML private JFXTextField currentBalanceTF;
	@FXML private TextField newBalanceTF;
	@FXML private Label accountName;
	@FXML private Text successfulMessage;
	@FXML private JFXComboBox<Integer> alertCheckingTime;
	@FXML private JFXComboBox<Integer> stockUpdateTime;
	
	private ArrayList<Observer> observers = new ArrayList<>();
	
	public SettingsController() {}

	public void initUserInfo() {
		accountName.setText(user.getAccount().getAccountName());
		currentBalanceTF.setText("$" + Utils.formatCurrencyDouble(user.getAccount().getBalance()));
		firstNameTF.setText(user.getFirstName());
		lastNameTF.setText(user.getLastName());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		newBalanceTF.setTextFormatter(new CurrencyFormatter());
		phoneNumberTF.setTextFormatter(new TextFormatter<>(PhoneNumberFormatter::addPhoneNumberMask));
		successfulMessage.setVisible(false);
		ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 5, 10, 20, 30, 60, 120);
		alertCheckingTime.setItems(options);
		stockUpdateTime.setItems(options);
	}
	
	@FXML private void saveChanges(ActionEvent e) {
		boolean anyChange = false;
		if (firstNameTF.getText().equals("")) {
		} else {
			if (ValidationUtil.validateFirstName(firstNameTF, firstNameError)) {
				user.setFirstName(firstNameTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (lastNameTF.getText().equals("")) {
		} else {
			if (anyChange && ValidationUtil.validateLastName(lastNameTF, lastNameError)) {
				user.setLastName(lastNameTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (emailTF.getText().equals("")) {
		} else {
			if (anyChange && ValidationUtil.validateEmail(emailTF, emailError)) {
				user.setEmail(emailTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (phoneNumberTF.getText().equals("###-###-####")) {
		}
		else {
			if (anyChange && ValidationUtil.validatePhoneNumber(phoneNumberTF, phoneNumberError)) {
				user.setPhoneNumber(phoneNumberTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (currentPasswordPF.getText().equals("") && newPasswordPF.getText().equals("") && confirmPasswordPF.getText().equals("")) {
		} else {
			if (ValidationUtil.validateCurrentPassword(user.getPassword(), currentPasswordPF, currentPasswordError)
			        && ValidationUtil.validateOriginalPassword(newPasswordPF, passwordError)
			        && ValidationUtil.validateConfirmedPassword(newPasswordPF, confirmPasswordPF, passwordError)) {
				user.setHashedPassword(newPasswordPF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}

		if (newBalanceTF.getText().equals("$0.00")) {
		} else {
			try {
				double newBalance = Utils.parseCurrencyDouble(newBalanceTF.getText());
				user.getAccount().setBalance(newBalance);
				anyChange = true;
			} catch (NumberFormatException ex) {
				System.err.println("Invalid balance: " + newBalanceTF.getText());
				anyChange = false;
			}
		}
		if (!alertCheckingTime.getSelectionModel().isEmpty()) {
			user.setAlertTime(alertCheckingTime.getSelectionModel().getSelectedItem());
			anyChange = true;
		}

		if (!stockUpdateTime.getSelectionModel().isEmpty()) {
			user.setStockUpdateTime(stockUpdateTime.getSelectionModel().getSelectedItem());
			anyChange = true;
		}

		if (anyChange) {
			userManager.update(user);
			notifyObservers();
			System.out.println("Update succesfully...");
			updateSettingFields();
			successfulMessage.setVisible(true);
			Task<Void> sleeper = new Task<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} 	finally {
						successfulMessage.setVisible(false);
					}
					return null;
				}
			};
			new Thread(sleeper).start();
		}
	}
	
	private void updateSettingFields() {
		passwordError.setVisible(false);
		emailError.setVisible(false);
		firstNameError.setVisible(false);
		lastNameError.setVisible(false);
		phoneNumberError.setVisible(false);
		currentPasswordError.setVisible(false);
		currentPasswordPF.clear();
		newPasswordPF.clear();
		confirmPasswordPF.clear();
		currentBalanceTF.setText("$" + Utils.formatCurrencyDouble(user.getAccount().getBalance()));
		alertCheckingTime.getSelectionModel().clearSelection();
		stockUpdateTime.getSelectionModel().clearSelection();
	}


	@Override
	public void register(Observer o) {
		if (observers.indexOf(o) == -1) {
			observers.add(o);
		}
	}

	@Override
	public void remove(Observer o) {
		if (observers.indexOf(o) != -1) {
			observers.remove(o);
		}
	}

	@Override
	public void notifyObservers() {
		for (Observer o : observers) {
			o.update();
		}
	}
}

package main.java.control;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.common.CommonDefine;
import main.java.model.User;
import main.java.utility.AlertFactory;
import main.java.utility.ResourceLocator;
import main.java.utility.Screen;
import main.java.utility.ValidationUtil;

import java.io.IOException;
import java.sql.Date;
import java.util.Optional;

public class ResetPasswordController extends BaseController implements IController {
	@FXML private AnchorPane resetPasswordAP;
	@FXML private JFXTextField emailTF;
	@FXML private JFXDatePicker dobDP;
	@FXML private JFXPasswordField passwordPF;
	@FXML private JFXPasswordField confirmPasswordPF;
	@FXML private Text errorT;
	
	public ResetPasswordController() {}
	
	@FXML protected void back(MouseEvent e) {
		switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, ResourceLocator.LOGIN_VIEW);
	}
	
	@FXML protected void reset(MouseEvent e) {
		System.out.println("Reset password.");
		
		if (!ValidationUtil.validateDoB(dobDP, errorT)) {
			return;
		}
		
		User user = userManager.verifyResetPassword(emailTF.getText(), Date.valueOf(dobDP.getValue()));
		if (user == null) { // Can't find user matched given information
			ValidationUtil.displayErrorMessage(errorT, CommonDefine.INCORRECT_INFORMATION_ERR);
			return;
		}
		
		if (ValidationUtil.validateOriginalPassword(passwordPF, errorT) &&
			ValidationUtil.validateConfirmedPassword(passwordPF, confirmPasswordPF, errorT)) {
			user.setHashedPassword(passwordPF.getText());
			boolean updateSuccessful = userManager.update(user);

			if (updateSuccessful) {
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.RESET_PASSWORD_SUCCESSFULLY_SMS);
				Optional<ButtonType> option = alert.showAndWait();
				// Switch to Login when user select OK
				if (option.isPresent() && option.get().equals(ButtonType.OK)) {
					switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, ResourceLocator.LOGIN_VIEW);
				}
			}
		}
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
    	Stage curStage = (Stage)resetPasswordAP.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
	}

	@Override
	public void makeNewStage(Screen target, String title, String url) {}
}

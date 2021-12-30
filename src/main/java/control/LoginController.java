package main.java.control;

import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.common.CommonDefine;
import main.java.dao.UserManager;
import main.java.utility.ResourceLocator;
import main.java.utility.Screen;
import main.java.utility.SecurityUtils;
import main.java.utility.Utils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
 
public class LoginController extends BaseController implements Initializable, IController {
    @FXML private Text loginError;
	@FXML private TextField usernameTF;
    @FXML private PasswordField passwordPF;
    
    @FXML private JFXCheckBox rememberMeCB;
    @FXML private AnchorPane loginAP;
    
    @FXML protected void login(ActionEvent e) throws IOException {
    	// Check if given username and password matched with records in database
		if (user == null) {
    		String hashedPW = SecurityUtils.hash(passwordPF.getText());
    		user = userManager.findByUsernameOrEmail(usernameTF.getText(), hashedPW);
		}
		
        if (user != null) {
        	System.out.println("Login successfully!");
        	switchScreen(Screen.HOME, "Stock Tracker", ResourceLocator.HOME_VIEW);
        	loginError.setVisible(false);
        	if (rememberMeCB.isSelected()) {
            	Utils.writeFile(usernameTF.getText());
        	} else {
        		Utils.writeFile("");
        	}
        } else {
        	loginError.setText("Incorrect username or password. Try again.");
        	loginError.setVisible(true);
        }
    }
    
    @FXML private void createNewAccount(MouseEvent e) {
    	System.out.println("Create new account.");
    	switchScreen(Screen.REGISTER, CommonDefine.REGISTRATION_TITLE, ResourceLocator.USER_REGISTRATION_VIEW);
    }
    
    @FXML private void resetPassword(MouseEvent e) {
    	System.out.println("Reset password.");
    	switchScreen(Screen.RESET_PASSWORD, CommonDefine.RESET_PASSWORD_TITLE, ResourceLocator.RESET_PASSWORD_VIEW);
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setupRememberMe();
	}

	public void setupRememberMe() {
		String identity = Utils.readFile(); // Could be either username or email
		if (identity != null && !identity.equals("")) {
			userManager = new UserManager<>();
			user = userManager.findByUsernameOrEmail(identity);
			if (user != null) {
				usernameTF.setText(identity);
				passwordPF.setText(user.getPassword());
			}
			rememberMeCB.setSelected(true);
		}
	}

	@Override public void switchScreen(Screen target, String title, String url) {
        Parent root = null;
		try {
			switch (target) {
				case HOME:
					FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
					root = loader.load(); // Loading before get controller
					HomeController homeController = loader.<HomeController>getController();
					homeController.setUser(user);
					break;
				case REGISTER:
					FXMLLoader ld = new FXMLLoader(getClass().getResource(url));
					root = ld.load();
					RegistrationController controller = ld.<RegistrationController>getController();
					controller.setPhoneNumberFormatter();
					break;
				case RESET_PASSWORD:
					root = new FXMLLoader(getClass().getResource(url)).load();
					break;
				default:
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	  	Stage curStage = (Stage)loginAP.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
    }

	@Override
	public void makeNewStage(Screen target, String title, String url) {}
}

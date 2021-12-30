package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.java.utility.ResourceLocator;


public class MainApp extends Application {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource(ResourceLocator.LOGIN_VIEW));
    	Parent root = (Parent)loader.load();
        stage.setTitle("Stock Tracker");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.getIcons().add(new Image(ResourceLocator.MSU_ICON));
        stage.show();
    }
}
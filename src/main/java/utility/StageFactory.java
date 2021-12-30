package main.java.utility;

import javafx.stage.Stage;

public class StageFactory {

	private StageFactory() {}
	
	public static Stage generateStage(String title) {
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setResizable(false);
		return stage;
	}
 
}

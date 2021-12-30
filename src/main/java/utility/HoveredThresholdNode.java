package main.java.utility;

import com.jfoenix.controls.JFXTextField;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class HoveredThresholdNode extends StackPane {
	public HoveredThresholdNode(String date, double value) {
		setPrefSize(5, 5);

		final JFXTextField tf = createDataThresholdTF(date, value);
		tf.setTranslateY(-30);
		tf.setAlignment(Pos.CENTER);
		tf.setPrefWidth(tf.getText().length()*10);
		setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				getChildren().setAll(tf);
				setCursor(Cursor.NONE);
				toFront();
			}
		});
		setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				getChildren().clear();
				setCursor(Cursor.CROSSHAIR);
			}
		});
	}
	
	private JFXTextField createDataThresholdTF(String date, double value) {
		final JFXTextField tf = new JFXTextField(date + ": " + String.valueOf(value));
		tf.getStyleClass().addAll("default-color1", "chart-line-symbol", "chart-series-line");
		tf.setStyle("-fx-font: Verdana; -fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: green");
		tf.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		return tf;
	}
}

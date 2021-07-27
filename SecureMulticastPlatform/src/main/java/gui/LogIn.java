package gui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LogIn {
	
	public void changeToLoginScene() {
		try {
			Stage loginStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene scene = new Scene(root,480,630);
			loginStage.setScene(scene);
			loginStage.setResizable(false);
			loginStage.setTitle("Login");
			loginStage.show();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			loginStage.setX((primScreenBounds.getWidth() - loginStage.getWidth()) / 2);
			loginStage.setY((primScreenBounds.getHeight() - loginStage.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}

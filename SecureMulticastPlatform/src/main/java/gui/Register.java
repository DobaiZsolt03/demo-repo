package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.CriptographicHelperClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class Register extends Application{

	private Stage registerStage = new Stage();
	
	public void loadStage(Stage registerStage) {
		try {
			
			Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
			Scene scene = new Scene(root,800,800);
			registerStage.setScene(scene);
			registerStage.setResizable(false);
			registerStage.setTitle("Register");
			registerStage.show();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			registerStage.setX((primScreenBounds.getWidth() - registerStage.getWidth()) / 2);
			registerStage.setY((primScreenBounds.getHeight() - registerStage.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
		@Override
		public void start(Stage primaryStage) {
			CriptographicHelperClass CHC = new CriptographicHelperClass();
			 String helloSERVERPublicKey = CHC.initialHelloHandshake();
			 
			 if(helloSERVERPublicKey!=null) {	
				File file = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\spubkey.txt"));
				
					if(file.exists()) {
						try {
							if(CHC.getServerPublicKey().equals(helloSERVERPublicKey)) {
								loadStage(primaryStage);
							}
							else {
								Alert alert = new Alert(AlertType.ERROR);
								alert.setTitle("Error");
								alert.setHeaderText("Unexpected error...");
								alert.setContentText("Something went wrong while contacting the server...");
								alert.showAndWait();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					else {
						CHC.saveServerPublicKey(helloSERVERPublicKey);
						loadStage(primaryStage);
					}
				}
				 else {
					 loadStage(primaryStage);
				 }
			 }

		public static void main(String[] args) {
			launch(args);
		}
		
		public void changeToRegisterScene() {
			loadStage(registerStage);
		}
}

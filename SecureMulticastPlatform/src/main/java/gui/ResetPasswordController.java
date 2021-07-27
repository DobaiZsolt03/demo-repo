package gui;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.CriptographicHelperClass;
import other.HANDSHAKE_PROTOCOL;
import other.ResetPasswordHelper;

public class ResetPasswordController{
	public Button continueButton;
	public Button registerInsteadButton;
	public Label informationLabel;
	public TextField usernameField;
	private static final int ATTEMPTS_ALLOWED = 5;
	public int attempts = ATTEMPTS_ALLOWED;
	private CriptographicHelperClass CHC = new CriptographicHelperClass();
	
	public void goToResetPasswordScene() {
		try {
			Stage stage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("ResetPassword.fxml"));
			Scene scene = new Scene(root,493,452);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setTitle("Reset your password");
			stage.show();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
			stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void goToAnswerQuestionsScene(String serverAnswer) throws IOException {
		
		String[] messageSplitter = new String[4];
		
		messageSplitter = serverAnswer.split("@",4);
		
		String question1 = messageSplitter[0];
		String question2 = messageSplitter[1];
		String question3 = messageSplitter[2];
		String retriesLeft = messageSplitter[3];
		
		Stage answerQuestionsStage = new Stage();	
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("AnswerSecurityQuestions.fxml"));
		Parent root = loader.load();
		
		AnswerSecurityQuestionsController controller = loader.getController();
		controller.getName(usernameField.getText());
		controller.setQuestion1(question1);
		controller.setQuestion2(question2);
		controller.setQuestion3(question3);
		controller.setRetriesLeft(retriesLeft);
		
		Scene scene = new Scene(root,700,752);
		answerQuestionsStage.setScene(scene);
		answerQuestionsStage.setResizable(false);
		answerQuestionsStage.setTitle("Answer some questions");
		answerQuestionsStage.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		answerQuestionsStage.setX((primScreenBounds.getWidth() - answerQuestionsStage.getWidth()) / 2);
		answerQuestionsStage.setY((primScreenBounds.getHeight() - answerQuestionsStage.getHeight()) / 2);
	}
	
	public void continueButtonClicked() {
		if(--attempts>0) {
			
			String serverAnswer = null;
			try {
				serverAnswer = CHC.sendUsername(CHC.getServerPublicKey(), usernameField.getText(),"UMESS");
			} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
					| IOException e1) {
				e1.printStackTrace();
			}
				
				if(serverAnswer!=null) {
						if(serverAnswer.contains("@")) {
							
							Stage stage = (Stage) continueButton.getScene().getWindow();
							stage.close();
							
							try {
								goToAnswerQuestionsScene(serverAnswer);
							} catch (IOException e) {
								e.printStackTrace();
							}
							
						}
						
						else if(serverAnswer.equals("No more resets left!")) {
							informationLabel.setText("You cannot reset your password anymore!");
						}
						
						else if(serverAnswer.equals("Username not found!")) {
							informationLabel.setText("No such username was found!");
						}
						
						else {
							informationLabel.setText("Something went wrong... Please try again!");
						}
				}
				
				else {
					informationLabel.setText("Server is unavailable... Try again later!");
				}
		}
		else
			informationLabel.setText("Attempts exhausted... Please try again later!");
	}
		
	
	public void registerInsteadButtonClicked() {
		Stage stage = (Stage) registerInsteadButton.getScene().getWindow();
		stage.close();
		
		Register register = new Register();
		register.changeToRegisterScene();
	}
	
}

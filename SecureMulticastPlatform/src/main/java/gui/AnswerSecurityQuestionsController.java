package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.CriptographicHelperClass;
import other.HANDSHAKE_PROTOCOL;
import other.ResetPasswordHelper;

public class AnswerSecurityQuestionsController implements Initializable{
	public Button answerButton;
	public Label usernameLabel;
	
	public ComboBox<String> questionBox1;
	public ComboBox<String> questionBox2;
	public ComboBox<String> questionBox3;
	
	public TextField answerField1;
	public TextField answerField2;
	public TextField answerField3;
	public Label nrRetriesLabel;
	public Label informationLabel;
	private static final int ATTEMPTS_ALLOWED = 5;
	public int attempts = ATTEMPTS_ALLOWED;
	
	public void getName(String username) {
		usernameLabel.setText(username+"!");
	}
	
	public void setQuestion1(String question1) {
		questionBox1.setValue(question1);
		questionBox1.getItems().add(question1);
	}
	
	public void setQuestion2(String question2) {
		questionBox2.setValue(question2);
		questionBox2.getItems().add(question2);
	}
	
	public void setQuestion3(String question3) {
		questionBox3.setValue(question3);
		questionBox3.getItems().add(question3);
	}
	
	public void setRetriesLeft(String retriesLeft) {
		nrRetriesLabel.setText(retriesLeft);
	}
	
	public void answerButtonClicked() {
		if(answerField1.getText().isBlank() || answerField2.getText().isBlank() || answerField3.getText().isBlank()) {
			informationLabel.setText("Answer all three questions!");
		}
		
		else if(--attempts>0){
				String username = usernameLabel.getText().substring(0, usernameLabel.getText().length()-1);
				ResetPasswordHelper RP = new ResetPasswordHelper();
				CriptographicHelperClass CHC = new CriptographicHelperClass();
				
				String serverAnswer = null;
				try {
					serverAnswer = RP.sendAnsweredQuestions(CHC.getServerPublicKey(), username,
							answerField1.getText().toUpperCase(),
							answerField2.getText().toUpperCase(),
							answerField3.getText().toUpperCase(),
							nrRetriesLabel.getText());
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
						| InvalidKeySpecException | IOException e1) {
					e1.printStackTrace();
				}
				
				if(serverAnswer!=null) {
								
								if(serverAnswer.equals("Questions answered correctly!")) {
									Stage stage = (Stage) answerButton.getScene().getWindow();
									stage.close();
									
									Stage finalizeResetPassword = new Stage();	
									
									FXMLLoader loader = new FXMLLoader();
									loader.setLocation(getClass().getResource("finalizeResetPassword.fxml"));
									Parent root = null;
									try {
										root = loader.load();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
									FinalizeResetPasswordController controller = loader.getController();
									controller.setName(username);
									
									Scene scene = new Scene(root,427,400);
									finalizeResetPassword.setScene(scene);
									finalizeResetPassword.setResizable(false);
									finalizeResetPassword.setTitle("Reset your password");
									finalizeResetPassword.show();
									Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
									finalizeResetPassword.setX((primScreenBounds.getWidth() - finalizeResetPassword.getWidth()) / 2);
									finalizeResetPassword.setY((primScreenBounds.getHeight() - finalizeResetPassword.getHeight()) / 2);
									
								}
								
								else if(serverAnswer.equals("Incorrect question answers!")) {
									informationLabel.setText("Incorrect answer(s)! Try again!");
								}
								
								else if(serverAnswer.equals("Username not found!")) {
									informationLabel.setText("Username not found!");
								}
								
								else {
									informationLabel.setText("Something went wrong... Please try again!");
								}
						}
						
						else {
							informationLabel.setText("Attempts exhausted! Please try again later!");
						}
					}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
	
}

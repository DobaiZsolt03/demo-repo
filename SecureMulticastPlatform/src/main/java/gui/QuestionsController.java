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

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import other.CriptographicHelperClass;
import other.HANDSHAKE_PROTOCOL;
import other.ResetPasswordHelper;

public class QuestionsController implements Initializable{
	
	public ComboBox<String> questionBox1;
	public ComboBox<String> questionBox2;
	public ComboBox<String> questionBox3;
	
	public TextField answerField1;
	public TextField answerField2;
	public TextField answerField3;
	
	public Button finishButton;
	public Label informationLabel;
	public Label usernameLabel;
	private ResetPasswordHelper RP;
	private CriptographicHelperClass CHC = new CriptographicHelperClass();
	
	public void finishButtonClicked() {
		if(questionBox1.getValue()==null || questionBox2.getValue()==null ||
				questionBox3.getValue()==null || answerField1.getText().isBlank() ||
				answerField2.getText().isBlank() || answerField3.getText().isBlank()) {
			
			informationLabel.setText("Please complete every field!");
			
		}
		
		else if(!answerField1.getText().matches("[a-zA-Z0-9 ]*") || !answerField2.getText().matches("[a-zA-Z0-9 ]*")
				|| !answerField3.getText().matches("[a-zA-Z0-9 ]*")){
			
			informationLabel.setText("Special characters not allowed! (except space)");
		}
		
		else {
			String username = usernameLabel.getText();
			username = username.substring(0, username.length() - 1);
			
			String serverAnswer=null;
			try {
				RP = new ResetPasswordHelper();
				serverAnswer = RP.sendEncryptedQuestionsData(CHC.getServerPublicKey(), username, questionBox1.getValue(),
						answerField1.getText().toUpperCase(), questionBox2.getValue(), 
						answerField2.getText().toUpperCase(),questionBox3.getValue(), 
						answerField3.getText().toUpperCase());
				
			} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
					| InvalidAlgorithmParameterException | IOException e) {
				e.printStackTrace();
			}
			
			if(serverAnswer!=null) {
						if(serverAnswer.equals("Q and A saved!")) {
							Stage stage = (Stage) finishButton.getScene().getWindow();
							stage.close();
							
							LogIn login = new LogIn();
							login.changeToLoginScene();
						}
						
						else {
							informationLabel.setText("Something went wrong... Please try again!");
						}
				}
				else {
					informationLabel.setText("Server seems to be offline... Try again later!");
				}
			}
		}
	
	public void getName(String username) {
		usernameLabel.setText(username+"!");
	}
	
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		questionBox1.getItems().addAll(
			    "Who was your childhood hero?","What is the name of the town where you were born?",
			    "What is the name of your first pet?","What was your first car?",
			    "What elementary school did you attend?");
		
		questionBox2.getItems().addAll("What did you want to be when you grew up?",
				"Where was your best family vacation as a kid?","What Is your favorite book?",
				"What is your favorite food?");
		
		questionBox3.getItems().addAll("What was the last name of your third grade teacher?",
				"What is your oldest sibling's middle name?", "What year did you graduate from High School?");
		
	}
}

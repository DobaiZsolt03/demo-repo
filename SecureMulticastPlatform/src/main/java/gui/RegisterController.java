package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import client.TestingSendMoreClient;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.ByteAndHexConversions;
import other.CriptographicHelperClass;
import other.HANDSHAKE_PROTOCOL;
import other.RSA_ALGORITHM;
import other.SchroederAuthenticationProtocol;



public class RegisterController implements Initializable{
	
	public ImageView dragon;
	public Button loginButton;
	public Button registerButton;
	public TextField firstnameField;
	public TextField lastnameField;
	public TextField usernameField;
	public TextField passwordField;
	public TextField repeatPasswordField;
	public CheckBox agreeCheckBox;
	public Label informationLabel;
	private static final int ATTEMPTS_ALLOWED = 5;
	public int attempts = ATTEMPTS_ALLOWED;	
	
	public boolean passwordIsValid(String password) {
		
	    char character;
	    boolean uppercaseFound= false;
	    boolean lowercaseFound = false;
	    boolean numberFound = false;
	    
	    for(int i=0;i < password.length();i++) {
	        character = password.charAt(i);
	        if( Character.isDigit(character)) {
	        	numberFound = true;
	        }
	        else if (Character.isUpperCase(character)) {
	        	uppercaseFound = true;
	        } else if (Character.isLowerCase(character)) {
	        	lowercaseFound = true;
	        }
	        
	        if(uppercaseFound && lowercaseFound && numberFound)
	            return true;
	    }
	    
	    return false;
	}
	
	public boolean specialCharactersFound(String input) {
		boolean isSpecialCharacter = false;
		Pattern pattern = Pattern.compile("\\W", Pattern.CASE_INSENSITIVE);
		Matcher match = pattern.matcher(input);
		boolean found = match.find();

		if (found) {
			isSpecialCharacter = true;
		}
		
		return isSpecialCharacter;
		
	}
	
	private void goToLoginScreen() {
		Stage stage = (Stage) loginButton.getScene().getWindow();
		stage.close();
		
		LogIn login = new LogIn();
		login.changeToLoginScene();
	}
	
	private void goToQuestionsScreen() throws IOException {
		Stage stage = (Stage) registerButton.getScene().getWindow();
		stage.close();
		
		Stage questionStage = new Stage();
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("Questions.fxml"));
		Parent root = loader.load();
		
		QuestionsController controller = loader.getController();
		controller.getName(usernameField.getText());
		
		Scene scene = new Scene(root,714,757);
		questionStage.setScene(scene);
		questionStage.setResizable(false);
		questionStage.setTitle("Questions");
		questionStage.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		questionStage.setX((primScreenBounds.getWidth() - questionStage.getWidth()) / 2);
		questionStage.setY((primScreenBounds.getHeight() - questionStage.getHeight()) / 2);
		
	}
	
	public void logInButtonClicked() {
			goToLoginScreen();
	}
	
	public void registerButtonClicked() throws SQLException {
		
		if(firstnameField.getText().isBlank() || lastnameField.getText().isBlank()
				|| usernameField.getText().isBlank() || passwordField.getText().isBlank()
				|| repeatPasswordField.getText().isBlank()) {
			
			informationLabel.setText("All boxes must be filled!");
		}
		
		else if(usernameField.getText().length() <6 ) {
			
			informationLabel.setText("Username is too short!");
		}
		
		else if(passwordField.getText().length()<8 || !passwordIsValid(passwordField.getText())) {
			
			informationLabel.setText("Password is too weak!");
		}
		
		else if(!repeatPasswordField.getText().equals(passwordField.getText())) {
			
			informationLabel.setText("Passwords don't match!");
		}
		
		else if(firstnameField.getText().length()>20 || lastnameField.getText().length()>20 ||
				usernameField.getText().length()>20 || passwordField.getText().length()>20) {
			
			informationLabel.setText("Too many characters in one of the fields!");
		}
		
		else if(specialCharactersFound(firstnameField.getText()) || specialCharactersFound(lastnameField.getText()) ||
				specialCharactersFound(usernameField.getText()) || specialCharactersFound(passwordField.getText())) {
			
			informationLabel.setText("No special characters or space allowed!");
		}
		
		else if(!agreeCheckBox.isSelected()) {
			
			informationLabel.setText("You must accept our conditions!");
		}
		
		
		else if(--attempts>0){
			
				HANDSHAKE_PROTOCOL HP = new HANDSHAKE_PROTOCOL();
				CriptographicHelperClass CHC = new CriptographicHelperClass();
				SchroederAuthenticationProtocol SCHAP = new SchroederAuthenticationProtocol();
				
						try {
							String serverAnswer;
							serverAnswer = HP.sendEncryptedRegistrationData(CHC.getServerPublicKey(), firstnameField.getText(), 
									lastnameField.getText(), usernameField.getText(), passwordField.getText());
							
							if(serverAnswer!=null) {
								if(serverAnswer.equals("Username taken!")) {
									informationLabel.setText("Username is not available!");
								}
								
								else if(serverAnswer.equals("Registration successful!")) {
									
									
									String finalAnswer;
									CHC.generateAndSaveUserKeyPair(usernameField.getText());
									finalAnswer = SCHAP.sendUserPublicKey(CHC.getServerPublicKey(),
											usernameField.getText(),CHC.getUserPublicKey(usernameField.getText()));
									
									if(finalAnswer.equals("Public key saved!")) {
										try {
											goToQuestionsScreen();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									
									else {
										informationLabel.setText("Unexpected error occured.. Account saved!");
									}
									
								}
								
								else{
									informationLabel.setText("Something went wrong... Please try again later!");
								}
							}
							
							else {
								informationLabel.setText("Server seems to be offline... Try again later!");
							}
							
						} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
								| InvalidKeySpecException | IOException e) {
							e.printStackTrace();
						}
			}
			
			else {
				informationLabel.setText("Attempts exhausted! Try again later!");
			}
			
		}
		

	public void initialize(URL arg0, ResourceBundle arg1) {
			 Image image = new Image("/images/twindragon.png");
		     dragon.setImage(image);
	}
}

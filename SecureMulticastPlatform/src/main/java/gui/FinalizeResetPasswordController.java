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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import other.CriptographicHelperClass;
import other.ResetPasswordHelper;

public class FinalizeResetPasswordController {
public Button resetPasswordButton;
public Label informationLabel;
public TextField passwordField;
public TextField confirmPasswordField;
public Label usernameLabel;


	public void setName(String username) {
		usernameLabel.setText(username);
	}
	
	public void resetPasswordButtonClicked() {
		RegisterController RC = new RegisterController();
		if(!RC.passwordIsValid(passwordField.getText()) || passwordField.getText().length()<8) {
			informationLabel.setText("Password is too weak!");
		}
		
		else if(RC.specialCharactersFound(passwordField.getText())) {
			informationLabel.setText("No special characters allowed!");
		}
		
		else if(passwordField.getText().equals(confirmPasswordField.getText())) {
			CriptographicHelperClass CHC = new CriptographicHelperClass();
			ResetPasswordHelper RP = new ResetPasswordHelper();
			String serverAnswer=null;
			try {
				serverAnswer = RP.sendNewPassword(CHC.getServerPublicKey(), usernameLabel.getText(),passwordField.getText());
			} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
					| IOException e) {
				e.printStackTrace();
			}
			
			if(serverAnswer!=null) {
							
							if(serverAnswer.equals("Password succesfully changed!")) {
								Stage stage = (Stage) resetPasswordButton.getScene().getWindow();
								stage.close();
								
								LogIn login = new LogIn();
								login.changeToLoginScene();
								
							}
							
							else {
								informationLabel.setText("Something went wrong... Please try again later!");
							}
			}
			
			else {
				informationLabel.setText("Server is unavailable... Try again later!");
			}
		}
		
		else
			informationLabel.setText("Passwords must match!");
	}
}

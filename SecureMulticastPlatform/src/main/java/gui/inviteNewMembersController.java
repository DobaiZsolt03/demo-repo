package gui;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.ForumRequests;

public class inviteNewMembersController {
	public Label informationLabel;
	public Button cancelButton;
	public Button inviteButton;
	public TextField inviteUserField;
	
	public void cancelButtonClicked() {
		Stage closeStage = (Stage)cancelButton.getScene().getWindow();
		closeStage.close();
	}
	
	public void inviteButtonClicked() {
		if(inviteUserField.getText().isBlank()) {
			informationLabel.setText("Please provide a valid user!");
		}
		
		else {
			ZContext ctx = new ZContext();
			AES_ALGORITHM AES = new AES_ALGORITHM();
			IvParameterSpec IV = AES.generateIv();
			ForumRequests FR = new ForumRequests();
			Stage currentStage = (Stage) informationLabel.getScene().getWindow();
			
			try {
				String forumName = currentStage.getTitle().toString();
				String serverAnswer = FR.sendForumInvite(ctx, IV, forumName, inviteUserField.getText());
				String[] messageSplitter = serverAnswer.split("#",3);
				
				if(!messageSplitter[0].equals(ForumController.nonceClient)) {
					informationLabel.setText("Something went wrong... try again later!");
				}
				
				else if(messageSplitter[1].equals("Error!")) {
					informationLabel.setText("That username does not exist!");
				}
				
				else if(messageSplitter[1].equals("Request accepted!")){
					informationLabel.setText("Invitation sent!");
				}
				
				else if(messageSplitter[1].equals("User is already a member!")) {
					informationLabel.setText("User already joined or is pending!");
				}
				
				else {
					informationLabel.setText("Unexpected error...");
				}
				
			} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
					| InvalidAlgorithmParameterException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}

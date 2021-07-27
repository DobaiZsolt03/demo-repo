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

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.ForumRequests;

public class giveAnswerController {

	public Button giveAnswerButton;
	public Label informationLabel;
	public TextField subjectField;
	public TextArea questionField;
	public TextArea yourAnswerField;
	public Label hiddenValue;
	
	private CurrentForumController currentForumController;
	
	public void goToGiveAnswerScene(Parent root, String forumTitle, Stage windowStage, String question, String subject, String questionNumber) throws IOException {
		
		Scene scene = new Scene(root,600,615);
		windowStage.initModality(Modality.APPLICATION_MODAL);
		windowStage.setScene(scene);
		windowStage.setResizable(false);
		windowStage.setTitle(forumTitle);
		questionField.setText(questionNumber+".) "+question);
		subjectField.setText(subject);
		hiddenValue.setText(questionNumber);
		windowStage.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		windowStage.setX((primScreenBounds.getWidth() - windowStage.getWidth()) / 2);
		windowStage.setY((primScreenBounds.getHeight() - windowStage.getHeight()) / 2);
		
	}
	
	public void init(CurrentForumController currentForumController) {
		this.currentForumController = currentForumController;
	}
	
	public void giveAnswerButtonClicked() {
		
		if(yourAnswerField.getText().isBlank()) {
			informationLabel.setText("Please provide an answer!");
		}
		
		else if(yourAnswerField.getText().length()<20) {
			informationLabel.setText("Your answer is too short, try to provide more detail!");
		}
		
		else if(yourAnswerField.getText().length()>1000) {
			informationLabel.setText("Input is too large! Please stick to 1000 max characters!");
		}
		
		else {
			ForumRequests FR = new ForumRequests();
			ZContext ctx = new ZContext();
			AES_ALGORITHM AES = new AES_ALGORITHM();
			IvParameterSpec IV = AES.generateIv();
			Stage stage = (Stage) giveAnswerButton.getScene().getWindow();
			try {
				String serverAnswer = FR.sendForumAnswer(ctx, IV, ForumController.sessionUser, 
						stage.getTitle().toString(), hiddenValue.getText().toString(), yourAnswerField.getText());
				
				if(serverAnswer==null) {
					informationLabel.setText("No response from server...");
				}
				
				else if(serverAnswer.equals("")) {
					informationLabel.setText("Something went wrong, please try again later!");
				}
				
				else {
					String[] messageSplitter = serverAnswer.split("#",3);
					
					if(!messageSplitter[0].equals(ForumController.nonceClient)) {
						informationLabel.setText("Error! Try again later...");
					}
					
					else if(messageSplitter[1].equals("Answer saved!")) {
						stage.close();
					}
					
					else {
						informationLabel.setText("Unexpected error!");
					}
				}
		
				
			} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
					| IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}

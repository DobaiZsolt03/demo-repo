package gui;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
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
import other.CriptographicHelperClass;
import other.ForumRequests;
import other.MessageAuthenticationCode;

public class ComposeForumQuestionController {
	public Button submitQuestionButton;
	public Label informationLabel;
	public TextField subjectField;
	public TextArea questionField;
	private CurrentForumController currentForumController;
	
	public void goToComposeQuestionScene(Parent root, String forumTitle, Stage windowStage) throws IOException {
		
		Scene scene = new Scene(root,840,731);
		windowStage.initModality(Modality.APPLICATION_MODAL);
		windowStage.setScene(scene);
		windowStage.setResizable(false);
		windowStage.setTitle(forumTitle);
		windowStage.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		windowStage.setX((primScreenBounds.getWidth() - windowStage.getWidth()) / 2);
		windowStage.setY((primScreenBounds.getHeight() - windowStage.getHeight()) / 2);
		
	}
	
	public void init(CurrentForumController currentForumController) {
		this.currentForumController = currentForumController;
	}
	
	public void submitQuestionButtonClicked() {
		if(subjectField.getText().isBlank()) {
			informationLabel.setText("You must provide a subject!");
		}
		
		else if(questionField.getText().isBlank()) {
			informationLabel.setText("Provide you question!");
		}
		
		else if(questionField.getText().length()<20) {
			informationLabel.setText("Your question is too short, try to provide more detail!");
		}
		
		else if(questionField.getText().length()>1000) {
			informationLabel.setText("Input is too large! Please stick to 1000 max characters!");
		}
		
		else {
			
			ForumRequests FR = new ForumRequests();
			Stage stage = (Stage) submitQuestionButton.getScene().getWindow();
			ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
			try {
				ArrayList<String> questions = FRUC.fetchForumQuestions(ForumController.sessionUser, stage.getTitle().toString(), 
						subjectField.getText(), questionField.getText());
				if(questions.isEmpty()) {
					informationLabel.setText("Unexpected error! Try again later!");
				}
				
				else {
					ArrayList<String> currentQuestion = new ArrayList<String>();
					int counter=0;
					for(int i=0;i<questions.size();i++) {
						counter++;
						currentQuestion.add(questions.get(i));
						
						if(counter==3) {
							counter=0;
							String IVSPEC = currentQuestion.get(2);
							String enc_message = currentQuestion.get(1);
							String questionNumber = currentQuestion.get(0);
							
							CriptographicHelperClass CHC = new CriptographicHelperClass();
							MessageAuthenticationCode MAC = new MessageAuthenticationCode();
							SecretKey finalForumKey = CHC.initializeSessionKey
									(CHC.getForumSimmetricKey(stage.getTitle().toString()));
							IvParameterSpec IVsPec = CHC.initializeIV(IVSPEC);
							String dec_message = FR.decryptForumMessage(enc_message, finalForumKey, IVsPec);
							String[] messageSplitter = dec_message.split("#",4);
							String username = messageSplitter[0];
							String subject = messageSplitter[1];
							String question = messageSplitter[2];
							String signedMAC = messageSplitter[3];
							
							String datablock = username+""+subject+""+question;
							if(MAC.isSignatureMatching(datablock, signedMAC, finalForumKey)) {
								if(i==2) {
									currentForumController.setForumQuestions(questionNumber, username, question, subject,"yes");
									currentQuestion.clear();
									
								}
								else {
									currentForumController.setForumQuestions(questionNumber, username, question, subject,"no");
									currentQuestion.clear();
								}
							}
						}
						
					}
					
					stage.close();
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

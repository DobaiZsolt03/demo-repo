package gui;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.zeromq.ZContext;

import client.ClientForumRequests;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.ForumRequests;
import other.MessageAuthenticationCode;

public class CreateForumController implements Initializable{
	
	public Button createForumButton;
	public ImageView dragon;
	public TextField forumNameField;
	public Label informationLabel;
	public CheckBox agreeCheckBox;
	public Button goBackButton;
	public TextArea describeForumField;
	private static final int ATTEMPTS_ALLOWED = 5;
	public int attempts = ATTEMPTS_ALLOWED;	
	public MessageAuthenticationCode MAC = new MessageAuthenticationCode();
	
	public void navigateToCreateForum() {
		try {
			Stage forum = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("createForum.fxml"));
			Scene scene = new Scene(root,480,757);
			forum.setScene(scene);
			forum.setResizable(false);
			forum.setTitle("Create a new Forum");
			forum.show();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			forum.setX((primScreenBounds.getWidth() - forum.getWidth()) / 2);
			forum.setY((primScreenBounds.getHeight() - forum.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createForumButtonClicked() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		if(forumNameField.getText().isBlank() || describeForumField.getText().isBlank()) {
			
			informationLabel.setText("You must fill out every field!");
		}
		
		else if(forumNameField.getText().length() > 35) {
			
			informationLabel.setText("Forum name is too long!");
		}
		
		else if(describeForumField.getText().length() > 116) {
			
			informationLabel.setText("Stick to a shorter description!");
		}
		
		else if(!agreeCheckBox.isSelected()) {
			
			informationLabel.setText("Please accept the condition!");
		}
		
		else if(--attempts>0){
			
			ForumRequests FR = new ForumRequests();
			String serverAnswer = FR.sendCreateForumREQ(forumNameField.getText(), describeForumField.getText());
			
			String[] messageSplitter = null;
			String nonceClient = null;
			String serverMessage = null;
			
			if(serverAnswer==null ) {
				
				informationLabel.setText("No response from server...");
			}
			
			
			else if(serverAnswer.equals("")){
				
				informationLabel.setText("Something went wrong...");
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				nonceClient = messageSplitter[0];
				serverMessage = messageSplitter[1];
				
				if(!nonceClient.equals(ForumController.nonceClient)) {
					
					informationLabel.setText("Error! Try again later!");
				}
				
				else if(serverMessage.equals("Rejected signature!")) {
					
					informationLabel.setText("Something went wrong... try again later!");
				}
				
				else if(serverMessage.equals("Error!")) {
					
					informationLabel.setText("Unexpected error...");
				}
				
				else if(serverMessage.equals("Forum name not available!")) {
					
					informationLabel.setText("That forum name is taken!");
				}
				
				else if(serverMessage.equals("Forum created!")) {
					
					FXMLLoader loader = new FXMLLoader(getClass().getResource("CurrentForum.fxml"));
		            Parent root;
		            
					try {
						
						root = loader.load();
						CurrentForumController CFC = loader.getController();
						CFC.setForumTitle(forumNameField.getText());
						
						ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
						ArrayList<String> forumMembersList = FRUC.fetchForumMembersList(forumNameField.getText(), ForumController.sessionUser);
						
						if(forumMembersList!=null) {
							for(int i=0;i<forumMembersList.size();i++) {
								String[] forumSplitter = forumMembersList.get(i).split("#",4);
								
								CFC.setForumMembers(forumSplitter[1], forumSplitter[2]);
							}
						}
						
						CFC.setPendingUsers(null,"Moderator");
					
						Stage closeStage = (Stage)createForumButton.getScene().getWindow();
						closeStage.close();
			            
			            Stage stage = new Stage();
			            stage.setScene(new Scene(root));
			            stage.setTitle(forumNameField.getText());
			            stage.setMaximized(true);
			            stage.show();
		            
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				else {
					
					informationLabel.setText("Unexpected error...");
				}
			}
		}
			
	}
	
	public void goBackButtonClicked() throws IOException {
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("joinOrCreateForum.fxml"));
		Parent root = loader.load();
		
		joinOrCreateForumController controller = loader.getController();
		
		ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
		ArrayList<String> notJoinedForumsList = FRUC.fetchNotJoinedForumsList();
		
		if(notJoinedForumsList==null) {
			controller.transferForum(null, null, null, null);
		}
		
		else {
			
			for(int i=0;i<notJoinedForumsList.size();i++) {
				String[] messageSplitter = notJoinedForumsList.get(i).split("#",6);
				controller.transferForum(messageSplitter[1], messageSplitter[2], messageSplitter[3], messageSplitter[4]);
			}
		}
		
		Stage closeStage = (Stage)goBackButton.getScene().getWindow();
		closeStage.close();
        
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Join or create Forum");
        stage.setResizable(false);
        stage.show();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Image image = new Image("/images/twindragon.png");
	    dragon.setImage(image);
		
	}
}

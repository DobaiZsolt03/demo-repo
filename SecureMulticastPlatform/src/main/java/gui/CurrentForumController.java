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
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.CriptographicHelperClass;
import other.ForumRequests;
import other.MessageAuthenticationCode;

public class CurrentForumController implements Initializable{
	public Button postAQuestionButton;
	public Label forumTitleLabel;
	public Button addNewMemberButton;
	public Button goBackButton;
	public VBox forumMembersListVBOX;
	public VBox pendingUsersVBOX;
	public VBox membersVBOX;
	public VBox middleVBOX;
	
	public void navigateToCurrentForum() {
		try {
			Stage forum = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("CurrentForum.fxml"));
			Scene scene = new Scene(root,1467,788);
			forum.setScene(scene);
			forum.setMaximized(true);
			forum.setTitle("Your forum");
			forum.show();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			forum.setX((primScreenBounds.getWidth() - forum.getWidth()) / 2);
			forum.setY((primScreenBounds.getHeight() - forum.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setForumTitle(String title) {
		
		forumTitleLabel.setText(title);	
    }
	
	public void setForumMembers(String forumMemberName, String forumMemberPosition) {
		
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.TOP_LEFT);
		
		
		Label forumMemberNameLabel = new Label(forumMemberName);
		forumMemberNameLabel.setWrapText(true);
		forumMemberNameLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
		forumMemberNameLabel.setTextFill(Color.WHITE);
		forumMemberNameLabel.setPadding(new Insets(10, 10, 10, 10));
		HBox.setMargin(forumMemberNameLabel, new Insets(0,0,0,20));
		
		Label forumMemberPositionLabel = new Label("("+forumMemberPosition+")");
		forumMemberPositionLabel.setWrapText(true);
		forumMemberPositionLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
		if(forumMemberPosition.equals("Moderator")) {
			forumMemberPositionLabel.setStyle("-fx-text-fill: linear-gradient(#FFBF00,#D3212D);");
			
		}
		else {
			forumMemberPositionLabel.setStyle("-fx-text-fill: #ff8c00;");
			
		}
		forumMemberPositionLabel.setPadding(new Insets(10, 10, 10, 10));
		HBox.setMargin(forumMemberPositionLabel, new Insets(0,0,0,0));
		
		hbox.getChildren().addAll(forumMemberNameLabel, forumMemberPositionLabel);
		forumMembersListVBOX.getChildren().add(hbox);
	}
	
	public void setPendingUsers(String pendingUsername, String Memberstatus) {
		
		if(Memberstatus.equals("Moderator")) {
		if(pendingUsername!=null) {
			Label pendingForumUser = new Label(pendingUsername);
			pendingForumUser.setWrapText(true);
			pendingForumUser.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
			pendingForumUser.setTextFill(Color.WHITE);
			VBox.setMargin(pendingForumUser, new Insets(10,0,5,0));
			pendingUsersVBOX.getChildren().add(pendingForumUser);
			
				HBox HboxforButtons = new HBox();
				HboxforButtons.setAlignment(Pos.TOP_CENTER);
				
				Button acceptButton = new Button("Accept");
				acceptButton.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
				acceptButton.setFont(Font.font("System", FontWeight.BOLD, 18));
				acceptButton.setPrefSize(90, 35);
				acceptButton.setMinSize(90, 35);
				acceptButton.setMaxSize(90, 35);
				acceptButton.setTextFill(Color.WHITE);
				HBox.setMargin(acceptButton, new Insets(0,10,0,0));
				
				Button declineButton = new Button("Decline");
				declineButton.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
				declineButton.setFont(Font.font("System", FontWeight.BOLD, 18));
				declineButton.setPrefSize(90, 35);
				declineButton.setMinSize(90, 35);
				declineButton.setMaxSize(90, 35);
				declineButton.setTextFill(Color.WHITE);
				
				HboxforButtons.getChildren().addAll(acceptButton, declineButton);
				pendingUsersVBOX.getChildren().add(HboxforButtons);
				
				acceptButton.setOnAction(e -> {
					
					ZContext ctx = new ZContext();
					AES_ALGORITHM AES = new AES_ALGORITHM();
					IvParameterSpec IV = AES.generateIv();
					ForumRequests FR = new ForumRequests();
					
						String serverAnswer;
						try {
							serverAnswer = FR.sendJoinForumDecision(ctx, IV, pendingUsername,
									forumTitleLabel.getText(), acceptButton.getText());
							
							String[] messageSplitter = serverAnswer.split("#",3);
							
							if(messageSplitter[0].equals(ForumController.nonceClient) 
									&& messageSplitter[1].equals("Operation Successful!")) {
									
									ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
									ArrayList<String> forumMembersList = FRUC.fetchForumMembersList(forumTitleLabel.getText(), ForumController.sessionUser);
									
									if(forumMembersList!=null) {
										forumMembersListVBOX.getChildren().clear();
										for(int i=0;i<forumMembersList.size();i++) {
											messageSplitter = forumMembersList.get(i).split("#",4);
											
											setForumMembers(messageSplitter[1], messageSplitter[2]);
										}
									}
									
									
									ArrayList<String> pendingForumREQList = FRUC.fetchPendingForumREQ(forumTitleLabel.getText(), ForumController.sessionUser);
									pendingUsersVBOX.getChildren().clear();
									
									if(pendingForumREQList!=null) {
										for(int i=0;i<pendingForumREQList.size();i++) {
											messageSplitter = pendingForumREQList.get(i).split("#",3);
											
											setPendingUsers(messageSplitter[1],"Moderator");
										}
									}
									
									else {
										
										setPendingUsers(null,"Moderator");
									}
							}
							
						} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
								| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
								| InvalidAlgorithmParameterException | IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
				});
				
				declineButton.setOnAction(e -> {
					
					ZContext ctx = new ZContext();
					AES_ALGORITHM AES = new AES_ALGORITHM();
					IvParameterSpec IV = AES.generateIv();
					ForumRequests FR = new ForumRequests();
					
						String serverAnswer;
						try {
							serverAnswer = FR.sendJoinForumDecision(ctx, IV, pendingUsername,
									forumTitleLabel.getText(), declineButton.getText());
							
							String[] messageSplitter = serverAnswer.split("#",3);
							
							if(messageSplitter[0].equals(ForumController.nonceClient) 
									&& messageSplitter[1].equals("Operation Successful!")) {
									
									ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
									
									ArrayList<String> pendingForumREQList = FRUC.fetchPendingForumREQ(forumTitleLabel.getText(), ForumController.sessionUser);
									pendingUsersVBOX.getChildren().clear();
									
									if(pendingForumREQList!=null) {
										
										for(int i=0;i<pendingForumREQList.size();i++) {
											messageSplitter = pendingForumREQList.get(i).split("#",3);
											
											setPendingUsers(messageSplitter[1],"Moderator");
										}
									}
									
									else {
										setPendingUsers(null,"Moderator");
									}
							}
							
						} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
								| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
								| InvalidAlgorithmParameterException | IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
				});
			}
		
			else {
				Label pendingForumUser = new Label("No new request to join!");
				pendingForumUser.setWrapText(true);
				pendingForumUser.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
				pendingForumUser.setTextFill(Color.WHITE);
				pendingForumUser.setPadding(new Insets(10, 10, 10, 10));
				
				pendingUsersVBOX.getChildren().add(pendingForumUser);
			}
		}
		
		else {
			
			Label pendingForumUser = new Label("You are not eligible to accept forum requests! Only moderators can add new members!");
			pendingForumUser.setWrapText(true);
			pendingForumUser.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
			pendingForumUser.setTextFill(Color.WHITE);
			membersVBOX.getChildren().remove(1);
			pendingForumUser.setPadding(new Insets(10, 10, 10, 10));
			
			pendingUsersVBOX.getChildren().add(pendingForumUser);
		}
	}
	
	public void setForumQuestions(String questionNumber, String username, String question, String subject, String islistupdated) throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		
		if(islistupdated.equals("yes")) {
			middleVBOX.getChildren().clear();
		}
		
		Label questionNumberLabel = new Label("Question "+questionNumber+":");
		questionNumberLabel.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		questionNumberLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
		questionNumberLabel.setTextFill(Color.WHITE);
		questionNumberLabel.setPrefSize(600, 35);
		questionNumberLabel.setMinSize(600, 35);
		questionNumberLabel.setMaxSize(600, 35);
		questionNumberLabel.setPadding(new Insets(5, 5, 5, 5));
		VBox.setMargin(questionNumberLabel, new Insets(2,2,2,2));
		
		Label questionContributor = new Label(username+" Asks:");
		questionContributor.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		questionContributor.setFont(Font.font("System", FontWeight.BOLD, 18));
		questionContributor.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
		questionContributor.setTextFill(Color.WHITE);
		questionContributor.setPrefSize(700, 35);
		questionContributor.setMinSize(700, 35);
		questionContributor.setMaxSize(700, 35);
		questionContributor.setPadding(new Insets(5, 5, 5, 5));
		VBox.setMargin(questionContributor, new Insets(2,2,2,2));
		
		Label questionSubject = new Label(subject);
		questionSubject.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		questionSubject.setFont(Font.font("System", FontWeight.BOLD, 18));
		questionSubject.setStyle("-fx-background-color: #43302e;");
		questionSubject.setTextFill(Color.WHITE);
		questionSubject.setPrefSize(800, 35);
		questionSubject.setMinSize(800, 35);
		questionSubject.setMaxSize(800, 35);
		questionSubject.setPadding(new Insets(5, 5, 5, 5));
		VBox.setMargin(questionSubject, new Insets(2,2,2,2));
		
		TextArea questionDetails = new TextArea(question);
		questionDetails.setFont(Font.font("System", FontWeight.BOLD, 18));
		questionDetails.setPrefSize(1210, 150);
		questionDetails.setMinSize(1210, 150);
		questionDetails.setMaxSize(1210, 150);
		questionDetails.setEditable(false);
		questionDetails.setWrapText(true);
		VBox.setMargin(questionDetails, new Insets(2,2,2,2));
		
		HBox HboxforButtons = new HBox();
		HboxforButtons.setAlignment(Pos.TOP_LEFT);
		
		Button deleteQuestion = new Button("Delete Question");
		deleteQuestion.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		deleteQuestion.setFont(Font.font("System", FontWeight.BOLD, 18));
		deleteQuestion.setStyle("-fx-background-color: linear-gradient(#d62121, #c72c2c);");
		deleteQuestion.setPrefSize(150, 40);
		deleteQuestion.setMinSize(150, 40);
		deleteQuestion.setMaxSize(150, 40);
		deleteQuestion.setTextFill(Color.WHITE);
		deleteQuestion.setPadding(new Insets(5, 5, 5, 5));
		HBox.setMargin(deleteQuestion, new Insets(0,0,0,725));
		
		Button viewComments = new Button("View Comments");
		viewComments.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		viewComments.setFont(Font.font("System", FontWeight.BOLD, 18));
		viewComments.setStyle("-fx-background-color: linear-gradient(#021b79, #0575e6);");
		viewComments.setPrefSize(150, 40);
		viewComments.setMinSize(150, 40);
		viewComments.setMaxSize(150, 40);
		viewComments.setTextFill(Color.WHITE);
		viewComments.setPadding(new Insets(5, 5, 5, 5));
		HBox.setMargin(viewComments, new Insets(0,0,0,20));
		
		Button giveAnswer = new Button("Give An Answer");
		giveAnswer.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		giveAnswer.setFont(Font.font("System", FontWeight.BOLD, 18));
		giveAnswer.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
		giveAnswer.setPrefSize(150, 40);
		giveAnswer.setMinSize(150, 40);
		giveAnswer.setMaxSize(150, 40);
		giveAnswer.setTextFill(Color.WHITE);
		giveAnswer.setPadding(new Insets(5, 5, 5, 5));
		HBox.setMargin(giveAnswer, new Insets(0,0,0,20));
		
		if(username.equals(ForumController.sessionUser)) {
			HboxforButtons.getChildren().addAll(deleteQuestion, viewComments, giveAnswer);
		}
		
		else {
			HBox.setMargin(viewComments, new Insets(0,0,0,890));
			HboxforButtons.getChildren().addAll(viewComments, giveAnswer);
		}
		
		giveAnswer.setOnAction(e -> {
			try {
				
				Stage windowStage = new Stage();
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("giveAnswer.fxml"));
				Parent root = loader.load();
				
				giveAnswerController controller = loader.getController();
				controller.init(this);
				controller.goToGiveAnswerScene(root,forumTitleLabel.getText(),windowStage,question,subject,questionNumber);
				
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			
		});
		
		viewComments.setOnAction(e -> {
				
			try {
				Stage windowStage = new Stage();
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("viewAnswers.fxml"));
				Parent root = loader.load();
				viewAnswersController controller = loader.getController();
				controller.init(this);
				
				ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
				
					ArrayList<String> answers = FRUC.fetchForumAnswers(forumTitleLabel.getText().toString()
							,ForumController.sessionUser, questionNumber);
					
					
					
					ForumRequests FR = new ForumRequests();
					if(answers!=null) {
						ArrayList<String> currentAnswer = new ArrayList<String>();
						int counter=0;
						for(int i=0;i<answers.size();i++) {
							counter++;
							currentAnswer.add(answers.get(i));
							
							if(counter==3) {
								counter=0;
								String IVSPEC = currentAnswer.get(2);
								String enc_message = currentAnswer.get(1);
								String quesNumber = currentAnswer.get(0);
								
								CriptographicHelperClass CHC = new CriptographicHelperClass();
								MessageAuthenticationCode MAC = new MessageAuthenticationCode();
								SecretKey finalForumKey = CHC.initializeSessionKey
										(CHC.getForumSimmetricKey(forumTitleLabel.getText().toString()));
								IvParameterSpec IVsPec = CHC.initializeIV(IVSPEC);
								String dec_message = FR.decryptForumMessage(enc_message, finalForumKey, IVsPec);
								String[] messageSplitter = dec_message.split("#",3);
								
								String uname = messageSplitter[0];
								String answer = messageSplitter[1];
								String signedMAC = messageSplitter[2];
								
								String datablock = uname+""+answer;
								if(MAC.isSignatureMatching(datablock, signedMAC, finalForumKey)) {
										controller.setAnswers(uname, quesNumber, answer);
										currentAnswer.clear();
								}
							}
						}
						controller.goToViewAnswersScene(root,forumTitleLabel.getText(),windowStage,question,subject,questionNumber);
						
					}
					
					else {
						controller.goToViewAnswersScene(root,forumTitleLabel.getText(),windowStage,question,subject,questionNumber);
					}
					
			}catch(InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
					| InvalidAlgorithmParameterException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		
		middleVBOX.getChildren().addAll(questionNumberLabel, questionContributor, questionSubject, questionDetails, HboxforButtons);
	}
	
	public void addNewMemberButtonClicked() {
		try {
			Stage forum = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("inviteNewMembers.fxml"));
			Scene scene = new Scene(root,450,200);
			forum.setScene(scene);
			forum.setTitle(forumTitleLabel.getText());
			forum.setResizable(false);
			forum.initModality(Modality.APPLICATION_MODAL);
			forum.initOwner(addNewMemberButton.getScene().getWindow());
			forum.showAndWait();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			forum.setX((primScreenBounds.getWidth() - forum.getWidth()) / 2);
			forum.setY((primScreenBounds.getHeight() - forum.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void goBackButtonClicked() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("Forum.fxml"));
		Parent root = loader.load();
		
		ForumController controller = loader.getController();
		
		ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
		ArrayList<String> forumsList = FRUC.fetchForumsList();
		
		if(forumsList==null) {
			controller.transferNewestForumQuestions(null, null, null, null, null);
			controller.transferForum(null);
			
			Stage closeStage = (Stage)goBackButton.getScene().getWindow();
			closeStage.close();
	        
	        Stage stage = new Stage();
	        stage.setScene(new Scene(root));
	        stage.setTitle("Main Menu");
	        stage.setResizable(true);
	        stage.setMaximized(true);
	        stage.show();
		}
		
		else {
			for(int i=0;i<forumsList.size();i++) {
				controller.transferForum(forumsList.get(i));
				ArrayList<String> currentLatestForumQuestion = FRUC.
						fetchLatestForumQuestions(forumsList.get(i),
								ForumController.sessionUser);
				
				ForumRequests FR = new ForumRequests();
				if(currentLatestForumQuestion!=null) {
					ArrayList<String> currentLatestForumQues = new ArrayList<String>();
					int counter=0;
					for(int i1=0;i1<currentLatestForumQuestion.size();i1++) {
						counter++;
						currentLatestForumQues.add(currentLatestForumQuestion.get(i1));
						
						if(counter==3) {
							counter=0;
							String IVSPEC = currentLatestForumQues.get(2);
							String enc_message = currentLatestForumQues.get(1);
							String quesNumber = currentLatestForumQues.get(0);
							
							CriptographicHelperClass CHC = new CriptographicHelperClass();
							MessageAuthenticationCode MAC = new MessageAuthenticationCode();
							SecretKey finalForumKey = CHC.initializeSessionKey
									(CHC.getForumSimmetricKey(forumsList.get(i).toString()));
							IvParameterSpec IVsPec = CHC.initializeIV(IVSPEC);
							String dec_message = FR.decryptForumMessage(enc_message, finalForumKey, IVsPec);
							String[] messageSplitter = dec_message.split("#",4);
							
							String uname = messageSplitter[0];
							String subject = messageSplitter[1];
							String question = messageSplitter[2];
							String signedMAC = messageSplitter[3];
							
							String datablock = uname+""+subject+""+question;
							if(MAC.isSignatureMatching(datablock, signedMAC, finalForumKey)) {
									controller.transferNewestForumQuestions(uname, forumsList.get(i), quesNumber, subject, question);
									currentLatestForumQues.clear();
							}
						}
					}
			}
		}
		
		ArrayList<String> invitedList = FRUC.fetchForumInvites(ForumController.sessionUser);
		
		if(invitedList==null) {
			controller.transferInvites(null);
		}
		
		else {
			
			for(int i=0;i<invitedList.size();i++) {
				controller.transferInvites(invitedList.get(i));
			}
		}
		
		Stage closeStage = (Stage)goBackButton.getScene().getWindow();
		closeStage.close();
        
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Main Menu");
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
		
	}
		
	}
	
	public void postAQuestionButtonClicked() {
		try {
			
			Stage windowStage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("ComposeForumQuestion.fxml"));
			Parent root = loader.load();
			
			ComposeForumQuestionController controller = loader.getController();
			controller.init(this);
			controller.goToComposeQuestionScene(root,forumTitleLabel.getText(),windowStage);
			
			} catch (IOException e) {
				e.printStackTrace();
			}
	}


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
}

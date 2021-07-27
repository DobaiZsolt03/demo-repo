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
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.CriptographicHelperClass;
import other.ForumRequests;
import other.MessageAuthenticationCode;

public class ForumController{
	public Button testStuff;
	public VBox middleVBOX;
	public Button postAQuestionButton;
	public VBox questionsBox;
	public Label usernameLabel;
	public Button joinForumButton;
	public VBox forumsListVBOX;
	public VBox invitesVBOX;
	public Label InvitesLabel;
	public Label invitesDescriptionLabel;
	public Label noForumsFoundLabel = new Label("You are not part of any forum yet... Join one!");
	
	public static String nonceClient;
	public static String nonceServer;
	public static String sessionKey;
	public static String sessionUser;
	
	public void navigateToForum() throws IOException{
		Stage forum = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("Forum.fxml"));
		Scene scene = new Scene(root,1467,788);
		forum.setScene(scene);
		forum.setMaximized(true);
		forum.setTitle("Main Menu");
		forum.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		forum.setX((primScreenBounds.getWidth() - forum.getWidth()) / 2);
		forum.setY((primScreenBounds.getHeight() - forum.getHeight()) / 2);
		forum.setOnCloseRequest(e -> System.out.println("Bye"));
	}
	
	public void transferForum(String forum) {
		
		if(forum == null) {
			noForumsFoundLabel.setWrapText(true);
			noForumsFoundLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
			noForumsFoundLabel.setTextFill(Color.WHITE);
			noForumsFoundLabel.setPadding(new Insets(10, 10, 10, 10));
			VBox.setMargin(noForumsFoundLabel, new Insets(10,0,0,20));
			
			forumsListVBOX.getChildren().add(noForumsFoundLabel);
		}
		
		else {
			
			Label forumName = new Label(forum);
			forumName.setWrapText(true);
			forumName.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
			forumName.setTextFill(Color.WHITE);
			forumName.setPadding(new Insets(10, 10, 10, 10));
			VBox.setMargin(forumName, new Insets(0,0,0,0));
			
			Button visitButton = new Button("Visit");
			visitButton.setWrapText(true);
			visitButton.setPrefSize(80, 45);
			visitButton.setMinSize(80, 45);
			visitButton.setMaxSize(80, 45);
			visitButton.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			visitButton.setFont(Font.font("System", FontWeight.BOLD, 18));
			visitButton.setTextFill(Color.WHITE);
			visitButton.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
			visitButton.setPadding(new Insets(10, 10, 10, 10));
			VBox.setMargin(visitButton, new Insets(0,0,35,0));
			
			visitButton.setOnAction(e -> {
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("CurrentForum.fxml"));
	            Parent root;
	            
				try {
					
					root = loader.load();
					CurrentForumController CFC = loader.getController();
					CFC.setForumTitle(forumName.getText());
					
					ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
					ArrayList<String> forumMembersList = FRUC.fetchForumMembersList(forum, ForumController.sessionUser);
					
					
					boolean isModerator = false;
					
					if(forumMembersList!=null) {
						for(int i=0;i<forumMembersList.size();i++) {
							String[] messageSplitter = forumMembersList.get(i).split("#",4);
							
							CFC.setForumMembers(messageSplitter[1], messageSplitter[2]);
							
							if(messageSplitter[1].equals(ForumController.sessionUser) 
									&& messageSplitter[2].equals("Moderator")) {
								
								isModerator = true;
							}
						}
					}
					
					
					
					ArrayList<String> pendingForumREQList = FRUC.fetchPendingForumREQ(forum, ForumController.sessionUser);
				
					
					if(isModerator) {
						if(pendingForumREQList!=null) {
							for(int i=0;i<pendingForumREQList.size();i++) {
								String[] messageSplitter = pendingForumREQList.get(i).split("#",3);
								
								CFC.setPendingUsers(messageSplitter[1],"Moderator");
							}
						}
						
						else {
							
							CFC.setPendingUsers(null,"Moderator");
						}
					}
					
					else {
						if(pendingForumREQList!=null) {
							for(int i=0;i<pendingForumREQList.size();i++) {
								String[] messageSplitter = pendingForumREQList.get(i).split("#",3);
								
								CFC.setPendingUsers(messageSplitter[1],"Member");
							}
						}
						
						else {
							
							CFC.setPendingUsers(null,"Member");
						}
					}
					
					try {
						ArrayList<String> questions= FRUC.fetchForumQuestionsModeratorMenu(forum,
								ForumController.sessionUser);
						
						ForumRequests FR = new ForumRequests();
						if(questions!=null) {
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
											(CHC.getForumSimmetricKey(forum));
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
											CFC.setForumQuestions(questionNumber, username, question, subject,"yes");
											currentQuestion.clear();
											
										}
										else {
											CFC.setForumQuestions(questionNumber, username, question, subject,"no");
											currentQuestion.clear();
										}
									}
								}
							}
						}
						
					} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
							| InvalidAlgorithmParameterException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					Stage closeStage = (Stage)visitButton.getScene().getWindow();
					closeStage.close();
		            
		            Stage stage = new Stage();
		            stage.setScene(new Scene(root));
		            stage.setTitle(forumName.getText());
		            stage.setMaximized(true);
		            stage.show();
	            
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			
			forumsListVBOX.getChildren().addAll(forumName,visitButton);
		}
    }
	
	public void transferInvites(String forumInvite) {
		
		if(forumInvite != null) {
			Label forumName = new Label(forumInvite);
			forumName.setWrapText(true);
			forumName.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 18));
			forumName.setTextFill(Color.WHITE);
			forumName.setPadding(new Insets(10, 10, 10, 10));
			VBox.setMargin(forumName, new Insets(0,0,0,0));
			
			HBox HboxforButtons = new HBox();
			HboxforButtons.setAlignment(Pos.TOP_CENTER);
			
			Button joinButton = new Button("Join");
			joinButton.setWrapText(true);
			joinButton.setPrefSize(80, 45);
			joinButton.setMinSize(80, 45);
			joinButton.setMaxSize(80, 45);
			joinButton.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			joinButton.setFont(Font.font("System", FontWeight.BOLD, 18));
			joinButton.setTextFill(Color.WHITE);
			joinButton.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
			joinButton.setPadding(new Insets(10, 10, 10, 10));
			HBox.setMargin(joinButton, new Insets(0,0,35,0));
			
			Button declineButton = new Button("Decline");
			declineButton.setWrapText(true);
			declineButton.setPrefSize(100, 45);
			declineButton.setMinSize(100, 45);
			declineButton.setMaxSize(100, 45);
			declineButton.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			declineButton.setFont(Font.font("System", FontWeight.BOLD, 18));
			declineButton.setTextFill(Color.WHITE);
			declineButton.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
			declineButton.setPadding(new Insets(10, 10, 10, 10));
			HBox.setMargin(declineButton, new Insets(0,0,35,35));
			
			invitesVBOX.getChildren().remove(InvitesLabel);
			invitesVBOX.getChildren().remove(invitesDescriptionLabel);
			HboxforButtons.getChildren().addAll(joinButton,declineButton);
			invitesVBOX.getChildren().addAll(forumName,HboxforButtons);
			
			declineButton.setOnAction(e -> {
				ZContext ctx = new ZContext();
				AES_ALGORITHM AES = new AES_ALGORITHM();
				IvParameterSpec IV = AES.generateIv();
				ForumRequests FR = new ForumRequests();
				
					String serverAnswer;
					try {
						serverAnswer = FR.sendJoinForumDecision(ctx, IV, ForumController.sessionUser,
								forumName.getText(), declineButton.getText());
						
						String[] messageSplitter = serverAnswer.split("#",3);
						
						if(messageSplitter[0].equals(ForumController.nonceClient) 
								&& messageSplitter[1].equals("Operation Successful!")) {
							invitesVBOX.getChildren().remove(forumName);
							invitesVBOX.getChildren().remove(HboxforButtons);
							
							if(invitesVBOX.getChildren().isEmpty()) {
								invitesVBOX.getChildren().add(InvitesLabel);
								invitesVBOX.getChildren().add(invitesDescriptionLabel);
							}
						}
						
					} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
							| InvalidAlgorithmParameterException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				
			});
			
			joinButton.setOnAction(e -> {
				ZContext ctx = new ZContext();
				AES_ALGORITHM AES = new AES_ALGORITHM();
				IvParameterSpec IV = AES.generateIv();
				ForumRequests FR = new ForumRequests();
				
					String serverAnswer;
					try {
						serverAnswer = FR.sendJoinForumDecision(ctx, IV, ForumController.sessionUser,
								forumName.getText(), "Accept");
						
						String[] messageSplitter = serverAnswer.split("#",3);
						
						if(messageSplitter[0].equals(ForumController.nonceClient) 
								&& messageSplitter[1].equals("Operation Successful!")) {
							
							invitesVBOX.getChildren().remove(forumName);
							invitesVBOX.getChildren().remove(HboxforButtons);
							
							if(invitesVBOX.getChildren().isEmpty()) {
								invitesVBOX.getChildren().add(InvitesLabel);
								invitesVBOX.getChildren().add(invitesDescriptionLabel);
							}
							forumsListVBOX.getChildren().remove(noForumsFoundLabel);
							transferForum(forumName.getText());
						}
						
					} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
							| InvalidAlgorithmParameterException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
			});
		}
    }
	
	public void transferNewestForumQuestions(String username, String forumName, String questionNumber, String subject, String question) {
		
			if(username!=null && forumName != null && questionNumber !=null && subject != null && question != null) {
				Label questionNumberLabel = new Label("Question "+questionNumber+":");
				questionNumberLabel.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
				questionNumberLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
				questionNumberLabel.setTextFill(Color.WHITE);
				questionNumberLabel.setPrefSize(500, 35);
				questionNumberLabel.setMinSize(500, 35);
				questionNumberLabel.setMaxSize(500, 35);
				questionNumberLabel.setPadding(new Insets(5, 5, 5, 5));
				VBox.setMargin(questionNumberLabel, new Insets(2,2,2,2));
				
				Label questionContributor = new Label(username+" Asks:");
				questionContributor.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
				questionContributor.setFont(Font.font("System", FontWeight.BOLD, 18));
				questionContributor.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
				questionContributor.setTextFill(Color.WHITE);
				questionContributor.setPrefSize(600, 35);
				questionContributor.setMinSize(600, 35);
				questionContributor.setMaxSize(600, 35);
				questionContributor.setPadding(new Insets(5, 5, 5, 5));
				VBox.setMargin(questionContributor, new Insets(2,2,2,2));
				
				Label questionSubject = new Label(subject);
				questionSubject.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
				questionSubject.setFont(Font.font("System", FontWeight.BOLD, 18));
				questionSubject.setStyle("-fx-background-color: #43302e;");
				questionSubject.setTextFill(Color.WHITE);
				questionSubject.setPrefSize(700, 35);
				questionSubject.setMinSize(700, 35);
				questionSubject.setMaxSize(700, 35);
				questionSubject.setPadding(new Insets(5, 5, 5, 5));
				VBox.setMargin(questionSubject, new Insets(2,2,2,2));
				
				TextArea questionDetails = new TextArea(question);
				questionDetails.setFont(Font.font("System", FontWeight.BOLD, 18));
				questionDetails.setPrefSize(1035, 150);
				questionDetails.setMinSize(1035, 150);
				questionDetails.setMaxSize(1035, 150);
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
				HBox.setMargin(deleteQuestion, new Insets(0,0,0,545));
				
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
					HBox.setMargin(viewComments, new Insets(0,0,0,715));
					HboxforButtons.getChildren().addAll(viewComments, giveAnswer);
				}
				
				giveAnswer.setOnAction(e -> {
					try {
						
						Stage windowStage = new Stage();
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("giveAnswer.fxml"));
						Parent root = loader.load();
						
						giveAnswerController controller = loader.getController();
						controller.goToGiveAnswerScene(root, forumName, windowStage, question, subject, questionNumber);
						
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
						
						ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
						
							ArrayList<String> answers = FRUC.fetchForumAnswers(forumName
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
												(CHC.getForumSimmetricKey(forumName));
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
								controller.goToViewAnswersScene(root,forumName,windowStage,question,subject,questionNumber);
								
							}
							
							else {
								controller.goToViewAnswersScene(root,forumName,windowStage,question,subject,questionNumber);
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
			
			
	}
	
	public void testStuffButtonClicked(){
		
	}
	
	public void joinForumButtonClicked() throws IOException {
		
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
		
		Stage closeStage = (Stage)joinForumButton.getScene().getWindow();
		closeStage.close();
        
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Join or create Forum");
        stage.setResizable(false);
        stage.show();
	}
}

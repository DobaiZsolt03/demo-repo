package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import client.ClientAuthentication;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.CriptographicHelperClass;
import other.ForumRequests;
import other.HANDSHAKE_PROTOCOL;
import other.MessageAuthenticationCode;
import other.SchroederAuthenticationProtocol;

public class LogInController implements Initializable{

	public Button registerButton;
	public Button loginButton;
	public TextField usernameField;
	public TextField passwordField;
	public CheckBox rememberCheckBox;
	public Label informationLabel;
	public ImageView dragon;
	public HANDSHAKE_PROTOCOL HP;
	public RegisterController RC = new RegisterController();
	private static final int ATTEMPTS_ALLOWED = 5;
	public int attempts = ATTEMPTS_ALLOWED;
	public Label forgotPasswordLabel;
	private CriptographicHelperClass CHC = new CriptographicHelperClass();
	
	public void registerButtonClicked() {
		Stage stage = (Stage) registerButton.getScene().getWindow();
		stage.close();
		
		Register register = new Register();
		register.changeToRegisterScene();
	}
	
	public void forgotPasswordLabelClicked() {
		Stage stage = (Stage) forgotPasswordLabel.getScene().getWindow();
		stage.close();
		
		ResetPasswordController reset = new ResetPasswordController();
		reset.goToResetPasswordScene();
	}
	
	public void rememberUser(String username, String decision) {
			
			try {
	            File rememberFile = new File("Users\\isRemembered.txt");
	            if (rememberFile.createNewFile())
	              System.out.println("File created: " + rememberFile.getName());
	            
	            FileWriter publicKeyWriter = new FileWriter(rememberFile);
	            publicKeyWriter.write(username);
	            publicKeyWriter.write("\n");
	            publicKeyWriter.write(decision);
	            publicKeyWriter.close();
	            
	          } catch (IOException e) {
	            System.out.println("An error occurred.");
	            e.printStackTrace();
	          }
	}
	
	public String showUser() throws IOException {
		
		File rememberFile = new File("Users\\isRemembered.txt");
		String toShow = null;
		
		if(rememberFile.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(rememberFile)); 
	    	String indexLine;
	    	String username = "";
	    	
	    	  while ((indexLine = br.readLine()) != null) {
	    		  if(!indexLine.equals("yes") && !indexLine.equals("no")) {
	    			 username = indexLine; 
	    		  }
	    		  else {
	    			  if(indexLine.equals("yes")) {
	    				  toShow = username;
	    			  }
	    			  
	    			  else if(indexLine.equals("no")){
	    				  toShow = null;
	    			  }
	    		  }
	    	  }
	    	  
	    	  br.close();
		}
    	  
    	  return toShow;
	}
	
	public void loginButtonClicked() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
		if(usernameField.getText().isBlank() || passwordField.getText().isBlank()){
			
			informationLabel.setText("Provide username and password!");
		}
		
		else if(RC.specialCharactersFound(usernameField.getText()) || 
				RC.specialCharactersFound(passwordField.getText())) {
			
			informationLabel.setText("No special characters allowed!");
		}
		
		else {
			
			if(--attempts>0) {
				
				String serverAnswer = null;
				try {
					serverAnswer = CHC.sendUsername(CHC.getServerPublicKey(), usernameField.getText(),"LOG");
				} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
						| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException | IOException e1) {
					e1.printStackTrace();
				}
				
				if(serverAnswer!=null) {
					
						if(serverAnswer.equals("Missmatch credentials!")) {
							informationLabel.setText("Invalid username or password combination!");
						}
						
						else if(serverAnswer.equals("Rejected signature!")) {
							informationLabel.setText("Something went wrong... please try again!");
						}
						
						else {
							HP = new HANDSHAKE_PROTOCOL();
							SchroederAuthenticationProtocol SCHARP = new SchroederAuthenticationProtocol();
							String finalServerAnswer = null;
							try {
								finalServerAnswer = HP.sendFinalLoginData(CHC.getServerPublicKey(), usernameField.getText()
										,passwordField.getText(), Integer.valueOf(serverAnswer));
							} catch (InvalidKeyException | NumberFormatException | InvalidKeySpecException
									| NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
									| BadPaddingException | InvalidAlgorithmParameterException | IOException e) {
								e.printStackTrace();
							}
							
							if(finalServerAnswer!=null) {
								if(finalServerAnswer.equals("Missmatch credentials!")) {
									informationLabel.setText("Invalid username or password combination! ");
								}
								
								else if(finalServerAnswer.equals("Rejected signature!")) {
									informationLabel.setText("Something went wrong... Please try again!");
								}
								
								else if(finalServerAnswer.equals("Credentials match!")){
									
									if(rememberCheckBox.isSelected()){
										rememberUser(usernameField.getText(), "yes");
										
									}
									else {
										rememberUser(usernameField.getText(), "no");
									}
									
									if(SCHARP.AuthenticationSuccessful(usernameField.getText(),
											ForumController.nonceClient, ForumController.nonceServer)) {
										
										FXMLLoader loader = new FXMLLoader();
										loader.setLocation(getClass().getResource("Forum.fxml"));
										Parent root = loader.load();
										
										ForumController controller = loader.getController();
										
										ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
										ArrayList<String> forumsList = FRUC.fetchForumsList();
										
										if(forumsList==null) {
											controller.transferNewestForumQuestions(null, null, null, null, null);
											controller.transferForum(null);
											
											Stage closeStage = (Stage)loginButton.getScene().getWindow();
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
																usernameField.getText());
												
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
										
										ArrayList<String> invitedList = FRUC.fetchForumInvites(usernameField.getText());
										
										if(invitedList==null) {
											controller.transferInvites(null);
										}
										
										else {
											
											for(int i=0;i<invitedList.size();i++) {
												controller.transferInvites(invitedList.get(i));
											}
										}
										
										Stage closeStage = (Stage)loginButton.getScene().getWindow();
										closeStage.close();
								        
								        Stage stage = new Stage();
								        stage.setScene(new Scene(root));
								        stage.setTitle("Main Menu");
								        stage.setResizable(true);
								        stage.setMaximized(true);
								        stage.show();
										
									}
								}
									
									else {
										informationLabel.setText("Authentication unsuccessful.. aborting..");
										attempts=0;
									}
								}
								
								else if(finalServerAnswer.equals("No public key found!")) {
									finalServerAnswer = SCHARP.sendUserPublicKey(CHC.getServerPublicKey(),
											usernameField.getText(),CHC.getUserPublicKey(usernameField.getText()));
									
									if(finalServerAnswer.equals("Public key saved!")) {
										if(SCHARP.AuthenticationSuccessful(usernameField.getText(),
												ForumController.nonceClient, ForumController.nonceServer)) {
											
											FXMLLoader loader = new FXMLLoader();
											loader.setLocation(getClass().getResource("Forum.fxml"));
											Parent root = loader.load();
											
											ForumController controller = loader.getController();
											
											ForumRequestUtilitiesClass FRUC = new ForumRequestUtilitiesClass();
											ArrayList<String> forumsList = FRUC.fetchForumsList();
											
											if(forumsList==null) {
												controller.transferForum(null);
											}
											
											else {
												
												for(int i=0;i<forumsList.size();i++) {
													String[] messageSplitter = forumsList.get(i).split("#",3);
													controller.transferForum(messageSplitter[1]);
												}
											}
											
											Stage closeStage = (Stage)loginButton.getScene().getWindow();
											closeStage.close();
									        
									        Stage stage = new Stage();
									        stage.setScene(new Scene(root));
									        stage.setTitle("Main Menu");
									        stage.setResizable(true);
									        stage.setMaximized(true);
									        stage.show();
										}
										
										else {
											informationLabel.setText("Authentication unsuccessful.. aborting..");
											attempts=0;
										}
									}
									
									else {
										informationLabel.setText("Unexpected error occured..");
									}
								}
								
								else {
									informationLabel.setText("Unexpected error... Try again later!");
								}
							}
							else {
								informationLabel.setText("No response from server! Try again later...");
							}
						}
				}
				
				else {
					informationLabel.setText("Server is unavailable... Try again later!");
				}
			}
			
			else{
				informationLabel.setText("Attempts exhausted! Try again later!");
			}
		}
	}
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		 Image image = new Image("/images/twindragon.png");
	     dragon.setImage(image);
	     
	     try {
	    	 
	    	 String username = showUser();
			if(username!=null) {
				 usernameField.setText(username);
				 rememberCheckBox.setSelected(true);
			 }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

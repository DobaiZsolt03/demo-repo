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
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.ForumRequests;

public class joinOrCreateForumController implements Initializable{
	
	public Button createForumButton;
	public Button goBackButton;
	public VBox middleVBOX;
	public Label informationLabel;
	
	public void navigateToJoinOrCreate() {
		try {
			Stage forum = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("joinOrCreateForum.fxml"));
			Scene scene = new Scene(root,987,711);
			forum.setScene(scene);
			forum.setTitle("JoinOrCreateForum");
			forum.show();
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			forum.setX((primScreenBounds.getWidth() - forum.getWidth()) / 2);
			forum.setY((primScreenBounds.getHeight() - forum.getHeight()) / 2);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createForumButtonClicked() {
		Stage stage = (Stage) goBackButton.getScene().getWindow();
		stage.close();
		
		CreateForumController cfc = new CreateForumController();
		
		cfc.navigateToCreateForum();
	}
	
	public void goBackButtonClicked() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		
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
				controller.transferForum(forumsList.get(i));
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
	
	public void transferForum(String forumName, String forumModerator, String forumDescription, String nbr_of_members) {
		
		if(forumName == null && forumModerator == null && forumDescription == null && nbr_of_members== null) {
			
			Label forumNameLabel = new Label("No new forums to join!");
			forumNameLabel.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 36));
			forumNameLabel.setTextFill(Color.WHITE);
			forumNameLabel.setPrefSize(400, 50);
			forumNameLabel.setMinSize(400, 50);
			forumNameLabel.setMaxSize(400, 50);
			VBox.setMargin(forumNameLabel, new Insets(230,0,0,300));
			
			middleVBOX.getChildren().add(forumNameLabel);
		}
		
		else {
			
			Label forumNameLabel = new Label("Forum name: "+forumName);
			forumNameLabel.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			forumNameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
			forumNameLabel.setTextFill(Color.WHITE);
			forumNameLabel.setPrefSize(400, 44);
			forumNameLabel.setMinSize(400, 44);
			forumNameLabel.setMaxSize(400, 44);
			forumNameLabel.setPadding(new Insets(5, 5, 5, 5));
			VBox.setMargin(forumNameLabel, new Insets(1,0,0,125));
			
			Label forumModeratorLabel = new Label("Forum owner: "+forumModerator);
			forumModeratorLabel.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			forumModeratorLabel.setStyle("-fx-background-color: #43302e;");
			forumModeratorLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
			forumModeratorLabel.setTextFill(Color.WHITE);
			forumModeratorLabel.setPrefSize(500, 44);
			forumModeratorLabel.setMinSize(500, 44);
			forumModeratorLabel.setMaxSize(500, 44);
			forumModeratorLabel.setPadding(new Insets(5, 5, 5, 5));
			VBox.setMargin(forumModeratorLabel, new Insets(1,0,0,125));
			
			Label nbr_of_ForumMembers = new Label("Total members: "+nbr_of_members);
			nbr_of_ForumMembers.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			nbr_of_ForumMembers.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
			nbr_of_ForumMembers.setFont(Font.font("System", FontWeight.BOLD, 18));
			nbr_of_ForumMembers.setTextFill(Color.WHITE);
			nbr_of_ForumMembers.setPrefSize(600, 44);
			nbr_of_ForumMembers.setMinSize(600, 44);
			nbr_of_ForumMembers.setMaxSize(600, 44);
			nbr_of_ForumMembers.setPadding(new Insets(5, 5, 5, 5));
			VBox.setMargin(nbr_of_ForumMembers, new Insets(1,0,0,125));
			
			TextArea descriptionText = new TextArea(forumDescription);
			descriptionText.setFont(Font.font("System", FontWeight.BOLD, 18));
			descriptionText.setPrefSize(700, 72);
			descriptionText.setMinSize(700, 72);
			descriptionText.setMaxSize(700, 72);
			descriptionText.setEditable(false);
			descriptionText.setWrapText(true);
			VBox.setMargin(descriptionText, new Insets(1,0,0,125));
			
			HBox HboxforButtons = new HBox();
			HboxforButtons.setAlignment(Pos.TOP_LEFT);
			
			Button joinForum = new Button("Join Forum");
			joinForum.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
			joinForum.setFont(Font.font("System", FontWeight.BOLD, 18));
			joinForum.setPrefSize(150, 40);
			joinForum.setMinSize(150, 40);
			joinForum.setMaxSize(150, 40);
			joinForum.setTextFill(Color.WHITE);
			joinForum.setPadding(new Insets(5, 5, 5, 5));
			HBox.setMargin(joinForum, new Insets(1,0,0,675));
			joinForum.setOnAction(e -> {
				String clickedForumName = forumNameLabel.getText();
				clickedForumName = clickedForumName.substring(12);
				
				ZContext ctx = new ZContext();
				AES_ALGORITHM AES = new AES_ALGORITHM();
				IvParameterSpec IV = AES.generateIv();
				ForumRequests FR = new ForumRequests();
				
				try {
					String serverAnswer = FR.sendJoinForumREQ(ctx, IV, forumName, ForumController.sessionUser);
					String[] messageSplitter = serverAnswer.split("#",3);
					
					if(!messageSplitter[0].equals(ForumController.nonceClient)) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Forum request failed..");
						alert.setHeaderText("Request to join "+clickedForumName+" failed!");
						alert.setContentText("Try again later...");
						alert.showAndWait();
					}
					
					else if(!messageSplitter[1].equals("Request accepted!")) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Forum request failed..");
						alert.setHeaderText("Request to join "+clickedForumName+" failed!");
						alert.setContentText("Try again later...");
						alert.showAndWait();
					}
					
					else {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Forum Request sent..");
						alert.setHeaderText("Request to join "+clickedForumName+" sent!");
						alert.setContentText("Moderator will send you an answer!");
						alert.showAndWait();
					}
					
				} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
						| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException | IOException e1) {
					e1.printStackTrace();
				}
			});
			
			HboxforButtons.getChildren().add(joinForum);
			
			middleVBOX.getChildren().add(forumNameLabel);
			middleVBOX.getChildren().add(forumModeratorLabel);
			middleVBOX.getChildren().add(nbr_of_ForumMembers);
			middleVBOX.getChildren().add(descriptionText);
			middleVBOX.getChildren().add(HboxforButtons);
		}
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
}

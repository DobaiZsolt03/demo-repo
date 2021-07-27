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

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import other.AES_ALGORITHM;
import other.ForumRequests;

public class viewAnswersController {
	public Button giveAnswerButton;
	public Label informationLabel;
	public TextField subjectField;
	public TextArea questionField;
	public TextArea yourAnswerField;
	public Label hiddenValue;
	public VBox answersVBOX;
	
	private CurrentForumController currentForumController;
	
	public void goToViewAnswersScene(Parent root, String forumTitle, Stage windowStage, String question, String subject, String questionNumber) throws IOException {
		
		Scene scene = new Scene(root,852,780);
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
	
	public void setAnswers(String username, String answerNumber, String answer) {
		
		Label questionContributor = new Label(username+" Says:");
		questionContributor.getStylesheets().add(ForumController.class.getResource("Questions.css").toExternalForm());
		questionContributor.setFont(Font.font("System", FontWeight.BOLD, 18));
		questionContributor.setStyle("-fx-background-color: linear-gradient(#233329,#63D471);");
		questionContributor.setTextFill(Color.WHITE);
		questionContributor.setPadding(new Insets(5, 5, 5, 5));
		VBox.setMargin(questionContributor, new Insets(2,2,2,2));
		
		TextArea questionDetails = new TextArea(answerNumber+") "+answer);
		questionDetails.setFont(Font.font("System", FontWeight.BOLD, 18));
		questionDetails.setPrefSize(700, 150);
		questionDetails.setMinSize(700, 150);
		questionDetails.setMaxSize(700, 150);
		questionDetails.setEditable(false);
		questionDetails.setWrapText(true);
		VBox.setMargin(questionDetails, new Insets(2,2,20,2));
		
		answersVBOX.getChildren().addAll(questionContributor, questionDetails);
	}
}

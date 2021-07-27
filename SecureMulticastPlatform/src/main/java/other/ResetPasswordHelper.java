package other;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import client.ClientLogin;
import server.WHelpDatabase;

public class ResetPasswordHelper {
	private RSA_ALGORITHM RSA = new RSA_ALGORITHM();
	private ByteAndHexConversions bhc = new ByteAndHexConversions();
	private AES_ALGORITHM AES = new AES_ALGORITHM();
	private WHelpDatabase UD = new WHelpDatabase();
	private MessageAuthenticationCode MAC = new MessageAuthenticationCode();
	private SALTEDHASH_ALGORITHM SHA = new SALTEDHASH_ALGORITHM();
	private String serverAnswer;
	private CriptographicHelperClass CHC = new CriptographicHelperClass();
	
	public String sendEncryptedQuestionsData(String serverPubKey, String username, String question1,
			String answer1, String question2, String answer2, String question3, String answer3) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		String hashedAnswer1 = bhc.encodeHexString(SHA.HASHChain(answer1, 999));
		String hashedAnswer2 = bhc.encodeHexString(SHA.HASHChain(answer2, 999));
		String hashedAnswer3 = bhc.encodeHexString(SHA.HASHChain(answer3, 999));
		String datablock = username+""+question1+""+hashedAnswer1+""+question2+""
				+hashedAnswer2+""+question3+""+hashedAnswer3;
		
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock, secretKey));
		String enc_secretKey = bhc.encodeHexString(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "QUES#"+username+"#"+question1+"#"+hashedAnswer1+"#"+question2+"#"
				+hashedAnswer2+"#"+question3+"#"+hashedAnswer3+"#"+signedMessage;
		
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = CHC.decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		
		return finalAnswer;
		
	}
	
	public void storeQuestionsData(String message, String IV, String secretKey, Socket server) throws SQLException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		String[] messageSplitter = message.split("#",9);
		
		String Username = messageSplitter[1];
		String question1 = messageSplitter[2];
		String answer1 = messageSplitter[3];
		String question2 = messageSplitter[4];
		String answer2 = messageSplitter[5];
		String question3 = messageSplitter[6];
		String answer3 = messageSplitter[7];
		String signedMAC = messageSplitter[8];
			
			String datablock = Username+""+question1+""+answer1+""+question2
			+""+answer2+""+question3+""+answer3;
			
			if(CHC.MACMatches(datablock, signedMAC, secretKey)) {
				UD.getConnection("Questions");
				
				SALTEDHASH_ALGORITHM SHA = new SALTEDHASH_ALGORITHM();
				byte[] salt = SHA.generateSalt();
				
				String saltedAnswer1 = bhc.encodeHexString(SHA.saltAndHASHPassword(answer1, salt));
				String saltedAnswer2 = bhc.encodeHexString(SHA.saltAndHASHPassword(answer2, salt));
				String saltedAnswer3 = bhc.encodeHexString(SHA.saltAndHASHPassword(answer3, salt));
				
				UD.addUserQuestions(Username, question1, saltedAnswer1,
						question2, saltedAnswer2, question3, saltedAnswer3,
						bhc.encodeHexString(salt), 3, "Questions");
				
				serverAnswer = CHC.encryptServerMessage("Q and A saved!", IV, secretKey);
				server.send(serverAnswer);
			}
			
			else {
				serverAnswer = CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
				server.send(serverAnswer);
			}
		}
	
	public void sendQuestions(String message,String IV,String secretKey,Socket server) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SQLException, InvalidAlgorithmParameterException {
		String[] messageSplitter = message.split("#",3);
		
		String Username = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		 
			if(CHC.MACMatches(Username, signedMAC, secretKey)) {
				 UD.getConnection("Questions");
				
				if(UD.userExists(Username, "Questions")) {
					
					if(UD.getResetsLeft("Questions", Username)==0) {
						serverAnswer = CHC.encryptServerMessage("No more resets left!", IV, secretKey);
						server.send(serverAnswer);
					}
					
					else {
						String[] questions = new String[3];
						questions = UD.getQuestions("Questions", Username);
						
						int retriesLeft = UD.getResetsLeft("Questions", Username);
						
						serverAnswer = CHC.encryptServerMessage(questions[0]+"@"+questions[1]+"@"+questions[2]+"@"+retriesLeft,
								IV, secretKey);
						
						server.send(serverAnswer);
					}
				}
				
				else {
					serverAnswer = CHC.encryptServerMessage("Username not found!", IV, secretKey);
					server.send(serverAnswer);
					
				}
			}
			
			else {
				serverAnswer = CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
				server.send(serverAnswer);
			}
	}
	
	
	public String sendAnsweredQuestions(String serverPubKey, String username, String answer1, String answer2,
			String answer3, String retriesLeft) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
		
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		int retries = Integer.valueOf(retriesLeft);
		int timesToHash = 4-retries;
		String hashedAnswer1 = bhc.encodeHexString(SHA.HASHChain(answer1, 999-timesToHash));
		String hashedAnswer2 = bhc.encodeHexString(SHA.HASHChain(answer2, 999-timesToHash));
		String hashedAnswer3 = bhc.encodeHexString(SHA.HASHChain(answer3, 999-timesToHash));
		String datablock = username+""+hashedAnswer1+""+hashedAnswer2+""+hashedAnswer3;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock, secretKey));
		String enc_secretKey = bhc.encodeHexString
				(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "AQUES#"+username+"#"+hashedAnswer1+"#"+hashedAnswer2+"#"+hashedAnswer3+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = CHC.decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		
		return finalAnswer;
	}
	
	public void sendAnsweredQuestionsResult(String message,String IV,String secretKey,Socket server) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, SQLException {
		String[] messageSplitter = message.split("#",6);
		
		String username = messageSplitter[1];
		String hashedAnswer1 = messageSplitter[2];
		String hashedAnswer2 = messageSplitter[3];
		String hashedAnswer3 = messageSplitter[4];
		String signedMAC = messageSplitter[5];
		String datablock = username+""+hashedAnswer1+""+hashedAnswer2+""+hashedAnswer3;
			
			if(CHC.MACMatches(datablock, signedMAC, secretKey)) {
				UD.getConnection("Questions");
				
				if(UD.userExists(username, "Questions")) {
					
					int retriesLeft = UD.getResetsLeft("Questions", username);
					int timesToHash = 4-retriesLeft;
					String answer1 = bhc.encodeHexString(SHA.ContinueHASHCHAIN(hashedAnswer1, timesToHash));
					String answer2 = bhc.encodeHexString(SHA.ContinueHASHCHAIN(hashedAnswer2, timesToHash));
					String answer3 = bhc.encodeHexString(SHA.ContinueHASHCHAIN(hashedAnswer3, timesToHash));
					byte[] answersSalt = bhc.decodeHexString(UD.getAnswersSalt("Questions", username));
					String final_answer1 = bhc.encodeHexString(SHA.saltAndHASHPassword(answer1, answersSalt));
					String final_answer2 = bhc.encodeHexString(SHA.saltAndHASHPassword(answer2, answersSalt));
					String final_answer3 = bhc.encodeHexString(SHA.saltAndHASHPassword(answer3, answersSalt));
					String[] answers = UD.getAnswers("Questions", username);
					
					if(final_answer1.equals(answers[0]) && final_answer2.equals(answers[1]) && 
							final_answer3.equals(answers[2])) {
						serverAnswer = CHC.encryptServerMessage("Questions answered correctly!",IV, secretKey);
						server.send(serverAnswer);
					}
					
					else {
						serverAnswer = CHC.encryptServerMessage("Incorrect question answers!",IV, secretKey);
						server.send(serverAnswer);
					}
				}
				
				else {
					serverAnswer = CHC.encryptServerMessage("Username not found!", IV, secretKey);
					server.send(serverAnswer);
				}
			}
			
			else {
				serverAnswer =  CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
				server.send(serverAnswer);
			}
	}
	
	public String sendNewPassword(String serverPubKey, String username, String password) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		String hashedPassword = bhc.encodeHexString(SHA.HASHChain(password, 999));
		String datablock = username+""+hashedPassword;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock, secretKey));
		String enc_secretKey = bhc.encodeHexString(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "RPASS#"+username+"#"+hashedPassword+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = CHC.decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		
		return finalAnswer;
	}
	
	public void storeNewPassword(String message,String IV,String secretKey,Socket server) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, SQLException {
		String[] messageSplitter = message.split("#",4);
		
		String username = messageSplitter[1];
		String hashedPassword = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		String datablock=username+""+hashedPassword;
			
			if(CHC.MACMatches(datablock, signedMAC, secretKey)) {
				UD.getConnection("Questions");
					int resets_left = UD.getResetsLeft("Questions", username);
					UD.updateResetsLeftNumber("Questions", username, resets_left);
					byte[] salt = bhc.decodeHexString(UD.getSalt("Users", username));
					String finalNewPassword = bhc.encodeHexString(SHA.saltAndHASHPassword(hashedPassword, salt));
					String old_password = UD.getPassword("Users", username);
					UD.updatePassword("Users", username, old_password, finalNewPassword);
						serverAnswer = CHC.encryptServerMessage("Password succesfully changed!",IV, secretKey);
						server.send(serverAnswer);
				}
				else {
					serverAnswer = CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
					server.send(serverAnswer);
				}
			}
	}

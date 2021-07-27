package other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.inject.spi.Message;

import client.ClientForumRequests;
import client.ClientLogin;
import gui.ForumController;
import server.WHelpDatabase;

public class ForumRequests {
	
	private RSA_ALGORITHM RSA = new RSA_ALGORITHM();
	private ByteAndHexConversions bhc = new ByteAndHexConversions();
	private AES_ALGORITHM AES = new AES_ALGORITHM();
	private WHelpDatabase UD = new WHelpDatabase();
	private MessageAuthenticationCode MAC = new MessageAuthenticationCode();
	private String serverAnswer;
	private CriptographicHelperClass CHC = new CriptographicHelperClass();
	private String signedMessage;
	private String finalMessage;
	private String datablock;
	
	private String encryptNonces(String nonceClient, String nonceServer) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		
		String message = "FORUMREQ#"+nonceClient+"#"+nonceServer;
		String enc_message = CHC.publicEncryptWithServerKey(message);
		
		return enc_message;
	}
	
	public String decryptForumMessage(String message, SecretKey secretKey, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		String decryptedMessage = AES.Decrypt(bhc.decodeHexString(message), secretKey, IV);
		
		return decryptedMessage;
	}
	
	public String encryptForumMessage(String message, SecretKey secretKey, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		String encryptedMessage = bhc.encodeHexString(AES.Encrypt(message, secretKey, IV));
		
		return encryptedMessage;
	}
	
	public String decryptNonces(String enc_nonces) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String privKey = RSA.getPrivateKey("RegisterLoginServer");
		byte[] dec_nonces = RSA.PrivateDecryption(bhc.decodeHexString(enc_nonces), bhc.decodeHexString(privKey));
		String final_nonces = new String(dec_nonces, StandardCharsets.UTF_8);
		
		return final_nonces;
	}
	
	public String sendCreateForumREQ(String forumName, String forumDesc) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		ZContext ctx = new ZContext();
		ClientForumRequests CFR = new ClientForumRequests(ctx);
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		IvParameterSpec IV = AES.generateIv();
		CHC.generateAndSaveForumSimmetricKey(forumName);
		
		String forumKey = CHC.getForumSimmetricKey(forumName);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = ForumController.sessionUser+""+forumName+""+forumDesc+""+forumKey;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "CREATEFORUM#"+ForumController.sessionUser+"#"+forumName+"#"+forumDesc+"#"
				+signedMessage+"#"+forumKey;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
		
	}
	
	public void sendCreateForumResult(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",6);
		String Username = messageSplitter[1];
		String forumName = messageSplitter[2];
		String forumDescription = messageSplitter[3];
		String signedMAC = messageSplitter[4];
		String forumKey = messageSplitter[5];
		
		datablock = Username+""+forumName+""+forumDescription+""+forumKey;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(Username, "Users")) {
				
				
				UD.getConnection(forumName);
				if(UD.forumCreationSuccessful(forumName, Username, forumDescription, forumKey)) {
					
					datablock = nonceClient+""+"Forum created!";
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+"#"+"Forum created!"+"#"+signedMessage;
					serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
					server.send(serverAnswer);
				}
				
				else {
					
					datablock = nonceClient+""+"Forum name not available!";
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+"#"+"Forum name not available!"+"#"+signedMessage;
					serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
					server.send(serverAnswer);
				}
			}
			
			else {
				
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendNotJoinedForumListREQ(ZContext ctx, IvParameterSpec IV) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = ForumController.sessionUser;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "GETFORUMLIST#"+ForumController.sessionUser+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	
	public void sendNotJoinedForumsList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",3);
		String Username = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		datablock = Username;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(Username, "Users")) {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				ArrayList<ArrayList<Object>> forumsList = UD.getAllNotJoinedForums(Username);
				
				String currentForum="";
				datablock = nonceClient;
				for(int i=0;i<forumsList.size();i++) {
					for(int j=0;j<4;j++) {
						currentForum = currentForum+"#"+forumsList.get(i).get(j);
						datablock = datablock+""+forumsList.get(i).get(j);
					}
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+currentForum+"#"+signedMessage;
					String enc_currentForum = encryptForumMessage(finalMessage,secretKey, IV);
					publisher.send(enc_currentForum);
					currentForum="";
					datablock = nonceClient;
				}
				publisher.send("DONE!");
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendJoinedForumsListREQ(ZContext ctx, IvParameterSpec IV) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = ForumController.sessionUser;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "GETJOINEDFORUMSLIST#"+ForumController.sessionUser+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendJoinedForumsList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",3);
		String Username = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		datablock = Username;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(Username, "Users")) {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				ArrayList<String> forumsList = UD.getAllJoinedForums(Username);
				
				String currentForum="";
				datablock = nonceClient;
				for(int i=0;i<forumsList.size();i++) {
					
					currentForum = currentForum+"#"+forumsList.get(i);
					datablock = datablock+""+forumsList.get(i);
					
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+currentForum+"#"+signedMessage;
					String enc_currentForum = encryptForumMessage(finalMessage,secretKey, IV);
					publisher.send(enc_currentForum);
					currentForum="";
					datablock = nonceClient;
				}
				publisher.send("DONE!");
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receiveNotJoinedForumsList(Socket subscriber, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> enc_ForumsList = new ArrayList<String>();
		String answer="";
		while(true) {
			answer = subscriber.recvStr();
			if(answer.equals("DONE!")) {
				break;
			}
			
			else {
				enc_ForumsList.add(answer);
			}
		}
		
		ArrayList<String> dec_ForumsList = new ArrayList<String>();
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		for(int i=0;i<enc_ForumsList.size();i++) {
			String[] messageSplitter = decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV).split("#",6);
			datablock = messageSplitter[0]+""+messageSplitter[1]+""+messageSplitter[2]+""+messageSplitter[3]+
					""+messageSplitter[4];
			if(MAC.isSignatureMatching(datablock, messageSplitter[5], sessionKey)) {
				dec_ForumsList.add(decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV));
			}
		}
		
		return dec_ForumsList;
	}
	
	public ArrayList<String> receiveJoinedForumsList(Socket subscriber, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> enc_ForumsList = new ArrayList<String>();
		String answer="";
		while(true) {
			answer = subscriber.recvStr();
			if(answer.equals("DONE!")) {
				break;
			}
			
			else {
				enc_ForumsList.add(answer);
			}
		}
		
		ArrayList<String> dec_ForumsList = new ArrayList<String>();
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		for(int i=0;i<enc_ForumsList.size();i++) {
			String[] messageSplitter = decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV).split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				dec_ForumsList.add(decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV));
			}
		}
		
		return dec_ForumsList;
	}
	
	public String sendForumMembersListREQ(ZContext ctx, IvParameterSpec IV, String forumName, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = username+""+forumName;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "GETFORUMMEMBERSLIST#"+username+"#"+forumName+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendForumMembersList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",4);
		String username = messageSplitter[1];
		String forumname = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = username+""+forumname;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(username, "Users")) {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				ArrayList<ArrayList<String>> forumMembersList = UD.getAllForumMembers(forumname);
				
				String currentForumMember="";
				datablock = nonceClient;
				for(int i=0;i<forumMembersList.size();i++) {
					
					for(int j=0;j<2;j++) {
						currentForumMember = currentForumMember+"#"+forumMembersList.get(i).get(j);
						datablock = datablock+""+forumMembersList.get(i).get(j);
					}
					
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+currentForumMember+"#"+signedMessage;
					String enc_currentForum = encryptForumMessage(finalMessage,secretKey, IV);
					publisher.send(enc_currentForum);
					currentForumMember="";
					datablock = nonceClient;
				}
				publisher.send("DONE!");
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receiveForumMembersList(Socket subscriber, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> enc_ForumsList = new ArrayList<String>();
		String answer="";
		while(true) {
			answer = subscriber.recvStr();
			if(answer.equals("DONE!")) {
				break;
			}
			
			else {
				enc_ForumsList.add(answer);
			}
		}
		
		ArrayList<String> dec_ForumsList = new ArrayList<String>();
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		for(int i=0;i<enc_ForumsList.size();i++) {
			String[] messageSplitter = decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV).split("#",4);
			datablock = messageSplitter[0]+""+messageSplitter[1]+""+messageSplitter[2];
			if(MAC.isSignatureMatching(datablock, messageSplitter[3], sessionKey)) {
				dec_ForumsList.add(decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV));
			}
		}
		
		return dec_ForumsList;
	}
	
	public String sendJoinForumREQ(ZContext ctx, IvParameterSpec IV, String forumName, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = username+""+forumName;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "JOINFORUMREQ#"+username+"#"+forumName+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendJoinForumAnswer(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",4);
		String username = messageSplitter[1];
		String forumname = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = username+""+forumname;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(username, "Users")) {
				UD.addPendingUserToForum(username, forumname);
				
				datablock = nonceClient+""+"Request accepted!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Request accepted!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendPendingForumREQ(ZContext ctx, IvParameterSpec IV, String forumName, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = username+""+forumName;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "PENDINGFORUMREQ#"+username+"#"+forumName+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendPendingForumREQList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",4);
		String username = messageSplitter[1];
		String forumname = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = username+""+forumname;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(username, "Users")) {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				ArrayList<String> pendingForumREQList = UD.getAllPendingForumUsers(username, forumname);
				
				String currentPendingUser="";
				datablock = nonceClient;
				for(int i=0;i<pendingForumREQList.size();i++) {
					
					currentPendingUser = currentPendingUser+"#"+pendingForumREQList.get(i);
					datablock = datablock+""+pendingForumREQList.get(i);
					
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+currentPendingUser+"#"+signedMessage;
					String enc_currentForum = encryptForumMessage(finalMessage,secretKey, IV);
					publisher.send(enc_currentForum);
					currentPendingUser="";
					datablock = nonceClient;
				}
				publisher.send("DONE!");
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receivePendingForumREQ(Socket subscriber, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> enc_ForumsList = new ArrayList<String>();
		String answer="";
		while(true) {
			answer = subscriber.recvStr();
			if(answer.equals("DONE!")) {
				break;
			}
			
			else {
				enc_ForumsList.add(answer);
			}
		}
		
		ArrayList<String> dec_ForumsList = new ArrayList<String>();
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		for(int i=0;i<enc_ForumsList.size();i++) {
			String[] messageSplitter = decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV).split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				dec_ForumsList.add(decryptForumMessage(enc_ForumsList.get(i), sessionKey, IV));
			}
		}
		
		return dec_ForumsList;
	}
	
	public String sendJoinForumDecision(ZContext ctx, IvParameterSpec IV, String userSelected, String forumName, String decision) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = userSelected+""+forumName+""+decision;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "JOINFORUMDECISION#"+userSelected+"#"+forumName+"#"+decision+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendJoinForumDecisionResult(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",5);
		String userSelected = messageSplitter[1];
		String forumName = messageSplitter[2];
		String Decision = messageSplitter[3];
		String signedMAC = messageSplitter[4];
		
		datablock = userSelected+""+forumName+""+Decision;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(userSelected, "Users")) {
				
				UD.updatePendingUserToForum(userSelected, forumName, Decision);
				datablock = nonceClient+""+"Operation Successful!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Operation Successful!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendForumInvite(ZContext ctx, IvParameterSpec IV, String forumName, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = username+""+forumName;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "FORUMINVITE#"+username+"#"+forumName+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendForumInviteAnswer(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",4);
		String username = messageSplitter[1];
		String forumname = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = username+""+forumname;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(username, "Users")) {
				if(UD.pendingUserExists(username, forumname) || UD.forumUserExists(username, forumname)) {
					datablock = nonceClient+""+"User is already a member!";
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+"#"+"User is already a member!"+"#"+signedMessage;
					serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
					server.send(serverAnswer);
				}
				
				else {
					UD.addInvitedUserToForum(username, forumname);
					datablock = nonceClient+""+"Request accepted!";
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+"#"+"Request accepted!"+"#"+signedMessage;
					serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
					server.send(serverAnswer);
				}
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendInvitedForumREQ(ZContext ctx, IvParameterSpec IV, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = username;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "INVITEDFORUMREQ#"+username+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void sendInvitedForumREQList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",3);
		String username = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		datablock = username+"";
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(username, "Users")) {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				ArrayList<String> invitedForumREQList = UD.getAllForumInvites(username);
				
				String currentForumInvite="";
				datablock = nonceClient;
				for(int i=0;i<invitedForumREQList.size();i++) {
					
					currentForumInvite = currentForumInvite+"#"+invitedForumREQList.get(i);
					datablock = datablock+""+invitedForumREQList.get(i);
					
					signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
					finalMessage = nonceClient+currentForumInvite+"#"+signedMessage;
					String enc_currentForum = encryptForumMessage(finalMessage,secretKey, IV);
					publisher.send(enc_currentForum);
					currentForumInvite="";
					datablock = nonceClient;
				}
				publisher.send("DONE!");
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receiveInvitedForumREQ(Socket subscriber, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> enc_ForumInviteList = new ArrayList<String>();
		String answer="";
		while(true) {
			answer = subscriber.recvStr();
			if(answer.equals("DONE!")) {
				break;
			}
			
			else {
				enc_ForumInviteList.add(answer);
			}
		}
		
		ArrayList<String> dec_ForumsInviteList = new ArrayList<String>();
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		for(int i=0;i<enc_ForumInviteList.size();i++) {
			String[] messageSplitter = decryptForumMessage(enc_ForumInviteList.get(i), sessionKey, IV).split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				dec_ForumsInviteList.add(decryptForumMessage(enc_ForumInviteList.get(i), sessionKey, IV));
			}
		}
		
		return dec_ForumsInviteList;
	}
	
	public String ResendForumKeyREQ(ZContext ctx, IvParameterSpec IV, String username, String forumName) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		ClientForumRequests CFR = new ClientForumRequests(ctx);
        
		SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
		
		String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
		String datablock = username+""+forumName;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "FORUMKEYREQ#"+username+"#"+forumName+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
		
		serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
			String[] messageSplitter = finalAnswer.split("#",3);
			datablock = messageSplitter[0]+""+messageSplitter[1];
			if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
				finalAnswer="";
			}
		}
		
		return finalAnswer;
	}
	
	public void resendForumKey(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException {
		String[] messageSplitter = message.split("#",4);
		String username = messageSplitter[1];
		String forumName = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = username+""+forumName;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(UD.userExists(username, "Users")) {
				
				String forumKey = UD.getForumKey(forumName);
				datablock = nonceClient+""+forumKey;
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock, secretKey));
				finalMessage = nonceClient+"#"+forumKey+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
			
			else {
				datablock = nonceClient+""+"Error!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Error!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
				server.send(serverAnswer);
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendForumQuestion(ZContext ctx, IvParameterSpec IV, String username, String forumName, String subject, String question) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String finalAnswer=null;
		ClientForumRequests CFR = new ClientForumRequests(ctx);
		boolean keyRetrieved = false;
		if(!CHC.forumKeyExists(forumName)) {
			String serverAnswer = ResendForumKeyREQ(ctx, IV, username, forumName);
			if(serverAnswer!=null) {
				if(!serverAnswer.equals("")) {
					String[] messageSplitter;
					messageSplitter = serverAnswer.split("#",3);
					if(messageSplitter[0].equals(ForumController.nonceClient)) {
						if(!messageSplitter[1].equals("Rejected signature!") || !messageSplitter[1].equals("Error!")) {
							CHC.saveForumKey(messageSplitter[1], forumName);
							keyRetrieved = true;
						}
					}
				}
			}
		}
		
		else {
			keyRetrieved = true;
		}
		
		if(keyRetrieved) {
			
			
			SecretKey finalForumKey = CHC.initializeSessionKey(CHC.getForumSimmetricKey(forumName));
			SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
			
			String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
			String commandDatablock = "FORUMQUESTION"+forumName;
			String signedCommandMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(commandDatablock,sessionKey));
			String IVSPEC = bhc.encodeHexString(IV.getIV());
			String questionDatablock = username+""+subject+""+question;
			String signedQuestionMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(questionDatablock,finalForumKey));
			String finalCommandMessage = "FORUMQUESTION#"+forumName+"#"+signedCommandMessage;
			String finalQuestionMessage = username+"#"+subject+"#"+question+"#"+signedQuestionMessage;
			String enc_finalCommandMessage = bhc.encodeHexString(AES.Encrypt(finalCommandMessage, sessionKey, IV));
			String enc_finalQuestionMessage = bhc.encodeHexString(AES.Encrypt(finalQuestionMessage, finalForumKey, IV));
			
			serverAnswer = CFR.attemptToSndQuestionToServer(encrypted_nonces, enc_finalCommandMessage, enc_finalQuestionMessage,IVSPEC);
			if(serverAnswer!=null) {
				finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
				String[] messageSplitter = finalAnswer.split("#",3);
				datablock = messageSplitter[0]+""+messageSplitter[1];
				if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
					finalAnswer="";
				}
			}
			
		}
		
		return finalAnswer;
	}
	
	public void sendForumQuestionResult(String message, String questionReceived, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException, IOException {
		String[] messageSplitter = message.split("#",3);
		String command = messageSplitter[0];
		String forumName = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		datablock = command+""+forumName;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(!CHC.forumMessageFileExists(forumName)){
				CHC.createForumFile(forumName);
			}
			
			try {
				String IVSPEC = bhc.encodeHexString(IV.getIV());
				CHC.writeToFileForumQuestion(forumName, questionReceived, IVSPEC);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			datablock = nonceClient+""+"Sending volumes of data!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
			server.send(serverAnswer);
			
			datablock = nonceClient;
			File forumFile = new File(forumName+"_messages.txt");
			BufferedReader reader = new BufferedReader(new FileReader(forumFile));
			String currentForumElement = reader.readLine();
			while(currentForumElement!=null) {
				if(currentForumElement.contains(".")) {
					
					reader.readLine();
					reader.readLine();
					currentForumElement = reader.readLine();
					
					if(currentForumElement==null) {
						publisher.send("DONE!");
					}
				}
				else {
					finalMessage = nonceClient+"#"+currentForumElement;
					publisher.send(finalMessage);
					currentForumElement = reader.readLine();
				}
				
				System.out.println(currentForumElement);
			}
			
			publisher.send("DONE!");
			
			reader.close();
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receiveForumQuestions(Socket subscriber) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> forumQuestionsList = new ArrayList<String>();
		String element="";
		while(true) {
			element = subscriber.recvStr();
			if(element.equals("DONE!")) {
				break;
			}
			
			else {
				forumQuestionsList.add(element);
			}
		}
		
		return forumQuestionsList;
	}
	
	public String sendForumQuestionsListREQ(ZContext ctx, IvParameterSpec IV, String username, String forumName) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String finalAnswer=null;
		ClientForumRequests CFR = new ClientForumRequests(ctx);
		boolean keyRetrieved = false;
		if(!CHC.forumKeyExists(forumName)) {
			String serverAnswer = ResendForumKeyREQ(ctx, IV, username, forumName);
			if(serverAnswer!=null) {
				if(!serverAnswer.equals("")) {
					String[] messageSplitter;
					messageSplitter = serverAnswer.split("#",3);
					if(messageSplitter[0].equals(ForumController.nonceClient)) {
						if(!messageSplitter[1].equals("Rejected signature!") || !messageSplitter[1].equals("Error!")) {
							CHC.saveForumKey(messageSplitter[1], forumName);
							keyRetrieved = true;
						}
					}
				}
			}
		}
		
		else {
			keyRetrieved = true;
		}
		
		if(keyRetrieved) {
			SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
			
			String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
			String datablock = forumName;
			String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
			String finalMessage = "FORUMQUESTIONLISTREQ#"+forumName+"#"+signedMessage;
			String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
			String IVSPEC = bhc.encodeHexString(IV.getIV());
			
			serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
			if(serverAnswer!=null) {
				finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
				String[] messageSplitter = finalAnswer.split("#",3);
				datablock = messageSplitter[0]+""+messageSplitter[1];
				if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
					finalAnswer="";
				}
			}
			
		}
		
		return finalAnswer;
	}
	
	public void sendForumMessagesList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException, IOException {
		String[] messageSplitter = message.split("#",3);
		String forumName = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		datablock = forumName;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(!CHC.forumMessageFileExists(forumName)){
				datablock = nonceClient+""+"No questions!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"No questions!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
			}
			
			else {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				datablock = nonceClient;
				File forumFile = new File(forumName+"_messages.txt");
				BufferedReader reader = new BufferedReader(new FileReader(forumFile));
				String currentForumElement = reader.readLine();
				while(currentForumElement!=null) {
					if(currentForumElement.contains(".")) {
						reader.readLine();
						reader.readLine();
						currentForumElement = reader.readLine();
						
						if(currentForumElement==null) {
							publisher.send("DONE!");
						}
					}
					
					else {
						finalMessage = nonceClient+"#"+currentForumElement;
						publisher.send(finalMessage);
						currentForumElement = reader.readLine();
					}
				}
				
				publisher.send("DONE!");
				
				reader.close();
			}
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendForumAnswer(ZContext ctx, IvParameterSpec IV, String username, String forumName, String questionNumber, String answer) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String finalAnswer=null;
		ClientForumRequests CFR = new ClientForumRequests(ctx);
		boolean keyRetrieved = false;
		if(!CHC.forumKeyExists(forumName)) {
			String serverAnswer = ResendForumKeyREQ(ctx, IV, username, forumName);
			if(serverAnswer!=null) {
				if(!serverAnswer.equals("")) {
					String[] messageSplitter;
					messageSplitter = serverAnswer.split("#",3);
					if(messageSplitter[0].equals(ForumController.nonceClient)) {
						if(!messageSplitter[1].equals("Rejected signature!") || !messageSplitter[1].equals("Error!")) {
							CHC.saveForumKey(messageSplitter[1], forumName);
							keyRetrieved = true;
						}
					}
				}
			}
		}
		
		else {
			keyRetrieved = true;
		}
		
		if(keyRetrieved) {
			
			SecretKey finalForumKey = CHC.initializeSessionKey(CHC.getForumSimmetricKey(forumName));
			SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
			
			String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
			String commandDatablock = "FORUMANSWER"+forumName+questionNumber;
			String signedCommandMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(commandDatablock,sessionKey));
			String IVSPEC = bhc.encodeHexString(IV.getIV());
			String questionDatablock = username+""+answer;
			String signedQuestionMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(questionDatablock,finalForumKey));
			String finalCommandMessage = "FORUMANSWER#"+forumName+"#"+questionNumber+"#"+signedCommandMessage;
			String finalQuestionMessage = username+"#"+answer+"#"+signedQuestionMessage;
			String enc_finalCommandMessage = bhc.encodeHexString(AES.Encrypt(finalCommandMessage, sessionKey, IV));
			String enc_finalQuestionMessage = bhc.encodeHexString(AES.Encrypt(finalQuestionMessage, finalForumKey, IV));
			
			serverAnswer = CFR.attemptToSndQuestionToServer(encrypted_nonces, enc_finalCommandMessage, enc_finalQuestionMessage,IVSPEC);
			if(serverAnswer!=null) {
				finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
				String[] messageSplitter = finalAnswer.split("#",3);
				datablock = messageSplitter[0]+""+messageSplitter[1];
				if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
					finalAnswer="";
				}
			}
		}
		return finalAnswer;
	}
	
	public void sendForumAnswerResult(String message, String answerReceived, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException, IOException {
		String[] messageSplitter = message.split("#",4);
		String command = messageSplitter[0];
		String forumName = messageSplitter[1];
		String questionNumber = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = command+""+forumName+""+questionNumber;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			try {
				String IVSPEC = bhc.encodeHexString(IV.getIV());
				CHC.writeToFileForumAnswer(forumName, answerReceived, IVSPEC, questionNumber);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			datablock = nonceClient+""+"Answer saved!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Answer saved!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
			server.send(serverAnswer);
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public String sendForumAnswersListREQ(ZContext ctx, IvParameterSpec IV, String username, String forumName, String questionNumber) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String finalAnswer=null;
		ClientForumRequests CFR = new ClientForumRequests(ctx);
		boolean keyRetrieved = false;
		if(!CHC.forumKeyExists(forumName)) {
			String serverAnswer = ResendForumKeyREQ(ctx, IV, username, forumName);
			if(serverAnswer!=null) {
				if(!serverAnswer.equals("")) {
					String[] messageSplitter;
					messageSplitter = serverAnswer.split("#",3);
					if(messageSplitter[0].equals(ForumController.nonceClient)) {
						if(!messageSplitter[1].equals("Rejected signature!") || !messageSplitter[1].equals("Error!")) {
							CHC.saveForumKey(messageSplitter[1], forumName);
							keyRetrieved = true;
						}
					}
				}
			}
		}
		
		else {
			keyRetrieved = true;
		}
		
		if(keyRetrieved) {
			SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
			
			String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
			String datablock = forumName+""+questionNumber;
			String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
			String finalMessage = "FORUMANSWERSLISTREQ#"+forumName+"#"+questionNumber+"#"+signedMessage;
			String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
			String IVSPEC = bhc.encodeHexString(IV.getIV());
			
			serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
			if(serverAnswer!=null) {
				finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
				String[] messageSplitter = finalAnswer.split("#",3);
				datablock = messageSplitter[0]+""+messageSplitter[1];
				if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
					finalAnswer="";
				}
			}
			
		}
		
		return finalAnswer;
	}
	
	public void sendForumAnswersList(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException, IOException {
		String[] messageSplitter = message.split("#",4);
		String forumName = messageSplitter[1];
		String questionNumber = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		datablock = forumName+""+questionNumber;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				datablock = nonceClient;
				File forumFile = new File(forumName+"_messages.txt");
				BufferedReader reader = new BufferedReader(new FileReader(forumFile));
				String currentForumElement = reader.readLine();
				while(currentForumElement!=null) {
					if(currentForumElement.contains(".")) {
						String[] splitter = currentForumElement.split("\\.",2);
						if(splitter[0].equals(questionNumber)) {
							String forumAnswer = reader.readLine();
							String Iv = reader.readLine();
							
							String final_forumAnswer = nonceClient+"#"+forumAnswer;
							String final_IV = nonceClient+"#"+Iv;
							String final_questionNumber = nonceClient+"#"+currentForumElement;
							
							publisher.send(final_questionNumber);
							publisher.send(final_forumAnswer);
							publisher.send(final_IV);
							
							currentForumElement = reader.readLine();
						}
						
						else {
							currentForumElement = reader.readLine();
						}
					}
					
					else {
						currentForumElement = reader.readLine();
						
						if(currentForumElement==null) {
							publisher.send("DONE!");
						}
					}
				}
				
				publisher.send("DONE!");
				
				reader.close();
		}
		
		else {
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receiveForumAnswers(Socket subscriber) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> forumAnswersList = new ArrayList<String>();
		String element="";
		while(true) {
			element = subscriber.recvStr();
			if(element.equals("DONE!")) {
				break;
			}
			
			else {
				forumAnswersList.add(element);
			}
		}
		
		return forumAnswersList;
	}
	
	public String sendLatestForumQuestionsREQ(ZContext ctx, IvParameterSpec IV, String username, String forumName) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String finalAnswer=null;
		ClientForumRequests CFR = new ClientForumRequests(ctx);
		boolean keyRetrieved = false;
		if(!CHC.forumKeyExists(forumName)) {
			String serverAnswer = ResendForumKeyREQ(ctx, IV, username, forumName);
			if(serverAnswer!=null) {
				if(!serverAnswer.equals("")) {
					String[] messageSplitter;
					messageSplitter = serverAnswer.split("#",3);
					if(messageSplitter[0].equals(ForumController.nonceClient)) {
						if(!messageSplitter[1].equals("Rejected signature!") || !messageSplitter[1].equals("Error!")) {
							CHC.saveForumKey(messageSplitter[1], forumName);
							keyRetrieved = true;
						}
					}
				}
			}
		}
		
		else {
			keyRetrieved = true;
		}
		
		if(keyRetrieved) {
			SecretKey sessionKey = CHC.initializeSessionKey(ForumController.sessionKey);
			
			String encrypted_nonces = encryptNonces(ForumController.nonceClient, ForumController.nonceServer);
			String datablock = forumName;
			String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,sessionKey));
			String finalMessage = "LATESTFORUMQUESTIONSREQ#"+forumName+"#"+signedMessage;
			String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, sessionKey, IV));
			String IVSPEC = bhc.encodeHexString(IV.getIV());
			
			serverAnswer = CFR.attemptToMsgServer(encrypted_nonces, enc_finalMessage, IVSPEC);
			if(serverAnswer!=null) {
				finalAnswer = decryptForumMessage(serverAnswer, sessionKey, IV);
				String[] messageSplitter = finalAnswer.split("#",3);
				datablock = messageSplitter[0]+""+messageSplitter[1];
				if(!MAC.isSignatureMatching(datablock, messageSplitter[2], sessionKey)) {
					finalAnswer="";
				}
			}
			
		}
		
		return finalAnswer;
	}
	
	public void sendLatestForumQuestions(String message, Socket server, SecretKey secretKey, IvParameterSpec IV, String nonceClient, Socket publisher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException, IOException {
		String[] messageSplitter = message.split("#",3);
		String forumName = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		datablock = forumName;
		
		if(MAC.isSignatureMatching(datablock, signedMAC, secretKey)) {
			
			if(!CHC.forumMessageFileExists(forumName)){
				datablock = nonceClient+""+"No questions!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"No questions!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
			}
			
			else {
				datablock = nonceClient+""+"Sending volumes of data!";
				signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
				finalMessage = nonceClient+"#"+"Sending volumes of data!"+"#"+signedMessage;
				serverAnswer = encryptForumMessage(finalMessage,secretKey, IV);
				server.send(serverAnswer);
				
				datablock = nonceClient;
				File forumFile = new File(forumName+"_messages.txt");
				BufferedReader reader = new BufferedReader(new FileReader(forumFile));
				String currentForumElement = reader.readLine();
				String forumQuestion="";
				String Iv="";
				String questionNumber="";
				while(currentForumElement!=null) {
						if(currentForumElement.contains(".")) {
							reader.readLine();
							reader.readLine();
							currentForumElement = reader.readLine();
							
							if(currentForumElement==null) {
								if(!questionNumber.equals("") && !forumQuestion.equals("") && !Iv.equals("")) {
									publisher.send(nonceClient+"#"+questionNumber);
									publisher.send(nonceClient+"#"+forumQuestion);
									publisher.send(nonceClient+"#"+Iv);
								}
								publisher.send("DONE!");
							}
						}
						
						else{
							
							questionNumber = currentForumElement;
							forumQuestion = reader.readLine();
							Iv = reader.readLine();
							
							currentForumElement = reader.readLine();
							
							if(currentForumElement==null) {
								publisher.send(nonceClient+"#"+questionNumber);
								publisher.send(nonceClient+"#"+forumQuestion);
								publisher.send(nonceClient+"#"+Iv);
								publisher.send("DONE!");
							}
						}
				}
				
				reader.close();
				
				publisher.send("DONE!");
				
			}
		}
		
		else {
			
			datablock = nonceClient+""+"Rejected signature!";
			signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock,secretKey));
			finalMessage = nonceClient+"#"+"Rejected signature!"+"#"+signedMessage;
			serverAnswer = encryptForumMessage(finalMessage, secretKey, IV);
			server.send(serverAnswer);
		}
	}
	
	public ArrayList<String> receiveLatestForumQuestions(Socket subscriber) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		ArrayList<String> latestforumQuestionsList = new ArrayList<String>();
		String element="";
		while(true) {
			element = subscriber.recvStr();
			if(element.equals("DONE!")) {
				break;
			}
			
			else {
				latestforumQuestionsList.add(element);
			}
		}
		
		return latestforumQuestionsList;
	}
}

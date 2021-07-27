package other;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import client.ClientAuthentication;
import client.ClientLogin;
import gui.ForumController;
import server.WHelpDatabase;

public class SchroederAuthenticationProtocol {
	
	AES_ALGORITHM AES = new AES_ALGORITHM();
	ByteAndHexConversions bhc = new ByteAndHexConversions();
	MessageAuthenticationCode MAC = new MessageAuthenticationCode();
	RSA_ALGORITHM RSA = new RSA_ALGORITHM();
	CriptographicHelperClass CHC = new CriptographicHelperClass();
	WHelpDatabase WD = new WHelpDatabase();
	Random random = new Random();
	
	
	public String sendUserPublicKey(String serverPubKey,String username, String pubKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		String datablock=username+""+pubKey;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(datablock, secretKey));
		String enc_secretKey = bhc.encodeHexString(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "SAVEUPUBK#"+username+"#"+pubKey+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		
		if(serverAnswer!=null) {
			finalAnswer = CHC.decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		return finalAnswer;
	}
	
	public void saveUserPublicKey(String message, String IV, String secretKey, Socket server) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SQLException {
		String[] messageSplitter = message.split("#",4);
		
		String username = messageSplitter[1];
		String publicKey = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		
		String serverAnswer;
		String datablock = username+""+publicKey;
		
		if(CHC.MACMatches(datablock, signedMAC, secretKey)) {
			WD.getConnection("PublicKeysTable");
			
			if(!WD.pubKeyExists(publicKey) && !WD.userExists(username, "PublicKeysTable")) {
				WD.saveUserPublicKey(username, publicKey);
				serverAnswer = CHC.encryptServerMessage("Public key saved!", IV, secretKey);
				server.send(serverAnswer);
			}
			
			else if(!WD.pubKeyExists(publicKey) && WD.userExists(username, "PublicKeysTable")) {
				WD.updateUserPublicKey(username, publicKey);
				serverAnswer = CHC.encryptServerMessage("Public key updated!", IV, secretKey);
				server.send(serverAnswer);
			}
			
			else {
				serverAnswer = CHC.encryptServerMessage("Fatal error!", IV, secretKey);
				server.send(serverAnswer);
			}
		}
		else {
			serverAnswer = CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
			server.send(serverAnswer);
		}
	}
	
	public String initiateAuthenticationMessage(String username) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
		
		
		String finalAnswer=null;
		String serverAnswer;
		if(!CHC.keyPairExists(username)) {
			CHC.generateAndSaveUserKeyPair(username);
			
			serverAnswer = sendUserPublicKey(CHC.getServerPublicKey(),username,CHC.getUserPublicKey(username));
			
			if(serverAnswer.equals("Public key updated!")) {
				
				ForumController.nonceClient = String.valueOf(random.nextInt(999999 - 100000) + 100000);
				ZContext ctx = new ZContext();
				ClientAuthentication AUTH = new ClientAuthentication(ctx);
				String finalMessage = "REQAUTH#"+username+"#"+String.valueOf(ForumController.nonceClient);
				String enc_finalMessage = CHC.publicEncryptWithServerKey(finalMessage);
				serverAnswer = AUTH.attemptToMsgServer(enc_finalMessage);
				
				if(serverAnswer!=null) {
					finalAnswer = CHC.privateDecryptWithClientKey(serverAnswer, username);
				}
			}
			
			else {
				finalAnswer="";
			}
		}
		
		else {
			
			ForumController.nonceClient = String.valueOf(random.nextInt(999999 - 100000) + 100000);
			ZContext ctx = new ZContext();
			ClientAuthentication AUTH = new ClientAuthentication(ctx);
			String finalMessage = "REQAUTH#"+username+"#"+String.valueOf(ForumController.nonceClient);
			String enc_finalMessage = CHC.publicEncryptWithServerKey(finalMessage);
			serverAnswer = AUTH.attemptToMsgServer(enc_finalMessage);
			
			if(serverAnswer!=null) {
				finalAnswer = CHC.privateDecryptWithClientKey(serverAnswer, username);
			}
		}
		
		return finalAnswer;
	}
	
	public void sendNoncesBackToUser(String message, String nonceServer, Socket server) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SQLException, IOException {
		String[] messageSplitter = message.split("#",3);
		
		String username = messageSplitter[1];
		String nonceClient = messageSplitter[2];
		
		String serverAnswer;
		
			WD.getConnection("PublicKeysTable");
			
				String userPubKey = WD.getUserPublicKey(username);
				if(userPubKey!=null) {
					String finalMessage = "AUTHRESPONSE#"+nonceClient+"#"+nonceServer;
					serverAnswer = CHC.publicEncryptWithClientKey(finalMessage, userPubKey);
					server.send(serverAnswer);
				}
		}
	
	public boolean serverAuthenticityVerified(String nonceClient, String nonceServer) {
		boolean isServerAuthenticated = false;
		
		if(nonceClient.equals(String.valueOf(ForumController.nonceClient))) {
			isServerAuthenticated = true;
			ForumController.nonceServer = nonceServer;
		}
		
		return isServerAuthenticated;
	}
	
	public String AuthenticateWithServer(String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		ZContext ctx = new ZContext();
		ClientAuthentication AUTH = new ClientAuthentication(ctx);
		String finalMessage = "AUTHWITHSERVER#"+username+"#"+ForumController.nonceServer;
		
		String enc_finalMessage = CHC.publicEncryptWithServerKey(finalMessage);
		String serverAnswer = AUTH.attemptToMsgServer(enc_finalMessage);
		String finalAnswer=null;
		
		if(serverAnswer!=null) {
			finalAnswer = CHC.privateDecryptWithClientKey(serverAnswer, username);
		}
		
		return finalAnswer;
		
	}
	
	public void sendSessionKeyToClient(String decrypted_authRequest, 
			ArrayList<ArrayList<String>> temporaryNonceValues, ArrayList<ArrayList<String>> finalSessionsList,
			Socket server) throws NoSuchAlgorithmException, SQLException, InvalidKeyException,
	InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		String[] messageSplitter = decrypted_authRequest.split("#",3);
		String nonceServer = messageSplitter[2];
		for(int i=0;i<temporaryNonceValues.size();i++) {
			for(int j=0;j<2;j++) {
				if(temporaryNonceValues.get(i).get(j).equals(nonceServer)) {
					AES_ALGORITHM AES = new AES_ALGORITHM();
		    		ByteAndHexConversions bhc = new ByteAndHexConversions();
					SecretKey secretKey = AES.generateEphemeralKey(256);
					String conv_secretKey = bhc.encodeHexString(secretKey.getEncoded());
					String nonceClient = temporaryNonceValues.get(i).get(j-1);
					ArrayList<String> confirmedSessionData = new 
							ArrayList<String>(Arrays.asList(temporaryNonceValues.get(i).get(j-1),
									temporaryNonceValues.get(i).get(j),conv_secretKey));
					
					finalSessionsList.add(confirmedSessionData);
					
					String username = messageSplitter[1];
					WD.getConnection("PublicKeysTable");
					String userPubKey = WD.getUserPublicKey(username);
					String finalMessage = "AUTHENTICATED#"+nonceClient+"#"+conv_secretKey;
					String enc_finalMessage = CHC.publicEncryptWithClientKey(finalMessage, userPubKey);
					server.send(enc_finalMessage);
				}
			}
		}
	}
	
	public boolean AuthenticationSuccessful(String username,String nonceClient, String nonceServer) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
		boolean isSuccessful = false;
		
		String firstStep = initiateAuthenticationMessage(username);
		
		if(firstStep==null || firstStep.equals("")) {
			isSuccessful = false;
		}
		
		else {
			String[] messageSplitter = firstStep.split("#",3);
			String givenNonceServer = messageSplitter[2];
			String givenNonceClient = messageSplitter[1];
			
			if(serverAuthenticityVerified(givenNonceClient, givenNonceServer)) {
				String lastStep = AuthenticateWithServer(username);
				
				messageSplitter = lastStep.split("#",3);
				if(messageSplitter[0].equals("AUTHENTICATED")) {
					if(messageSplitter[1].equals(ForumController.nonceClient)) {
						String sessionKey = messageSplitter[2];
						ForumController.sessionKey=sessionKey;
						ForumController.sessionUser=username;
						isSuccessful = true;
					}
					
					else {
						isSuccessful = false;
					}
				}
				
				else {
					isSuccessful = false;
				}
			}
			
			else {
				isSuccessful = false;
			}
		}
		
		return isSuccessful;
	}
	
	public String getNonceSessionKey(String nonces, ArrayList<ArrayList<String>> finalSessionsList) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		String[] messageSplitter = nonces.split("#",3);
		String nonceClientReceived = messageSplitter[1];
		String nonceServerReceived = messageSplitter[2];
		
		String sessionKey = null;
		for(int i=0;i<finalSessionsList.size();i++) {
				if(finalSessionsList.get(i).get(0).equals(nonceClientReceived) && 
						finalSessionsList.get(i).get(1).equals(nonceServerReceived)) {
					sessionKey = finalSessionsList.get(i).get(2);
				}
		}
		
		return sessionKey;
	}
	
}

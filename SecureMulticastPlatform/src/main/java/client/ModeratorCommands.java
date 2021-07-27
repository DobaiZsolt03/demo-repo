package client;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import other.AES_ALGORITHM;
import other.ByteAndHexConversions;
import other.RSA_ALGORITHM;

public class ModeratorCommands {
	public String identity;
	private ZMQ.Socket moderator;
	private String[] messageSplitter;
	private RSA_ALGORITHM RSA;
	private String sender;
	private String answer;
	private String publicKey;
	private ByteAndHexConversions bhc;
	
	public ModeratorCommands(ZMQ.Socket moderator, String identity) {
		this.moderator = moderator;
		this.identity = identity;
	}
	
	public void Connect(String identity) {
 		moderator.setIdentity(identity.getBytes(ZMQ.CHARSET));
 		moderator.connect("tcp://localhost:5570");
 		moderator.send(identity+"#Conn#"+"Logging in");
 	}
	
	public boolean isForumRequest(ZMsg message) {
		
		boolean isForumReq = false;
		
		if (message.getLast().toString().startsWith("PubK#")){
			isForumReq = true;
		}
		
		return isForumReq;
	}
	
	public void joinForumRequest(ZMsg message, Scanner choice) {
		
    	messageSplitter = message.getLast().toString().split("#",3);
    	
    	sender = messageSplitter[1];
		publicKey = messageSplitter[2];
		
		System.out.println(sender+" wants to join the forum!");
		//System.out.println(publicKey);
		System.out.println("Y - ACCEPT, N - REJECT");
		
		answer = choice.next();
		
		if(answer.toUpperCase().equals("Y")) {
			moderator.send(identity+"#addU#"+answer+"#"+sender);
		}
		
		else if(answer.toUpperCase().equals("N"))
			System.out.println("User rejected!");
		
	}
	
	public boolean isSecretMess(ZMsg message) {
		boolean isSecret = false;
		if(message.getLast().toString().startsWith("AuthM#")) {
			isSecret = true;
		}
		return isSecret;
	}
	
	public void secretMessage(ZMsg message) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
			RSA = new RSA_ALGORITHM();
			bhc = new ByteAndHexConversions();
			messageSplitter = message.getLast().toString().split("#",3);
			
			String secret_password = "Correct Horse Battery Staple";
			
			String user = messageSplitter[1];
			String secretMessage = messageSplitter[2];
			System.out.println(secretMessage);
			
			byte[] privateKey = bhc.decodeHexString(RSA.getPrivateKey("Moderator"));
			byte[] decryptedText = RSA.PrivateDecryption(bhc.decodeHexString(secretMessage),privateKey);
			
			String password = new String(decryptedText, StandardCharsets.UTF_8);
			
			if(password.equals(secret_password)) {
				acceptUser(user);
			}
			else {
				message.destroy();
			}
	}
	
	public void acceptUser(String user) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		AES_ALGORITHM AES = new AES_ALGORITHM();
		RSA_ALGORITHM RSA = new RSA_ALGORITHM();
		bhc = new ByteAndHexConversions();
		byte[] secretKey = RSA.PublicEncryption(bhc.encodeHexString(AES.getSimmetricKey().getEncoded()), bhc.decodeHexString(RSA.getPublicKey(user)));
		
		moderator.send("FinalizeAddU#"+user+"#"+bhc.encodeHexString(secretKey)+"#User accepted in the forum!");
	}
	
	
}

package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import other.AES_ALGORITHM;
import other.ByteAndHexConversions;
import other.MessageAuthenticationCode;
import other.RSA_ALGORITHM;

public class ClientCommands {
	public String identity;
	private ZMQ.Socket client;
	private String[] messageSplitter;
	private RSA_ALGORITHM RSA;
	private ByteAndHexConversions bhc;
	private MessageAuthenticationCode MAC;
	private AES_ALGORITHM AES;
	
	
	public ClientCommands(ZMQ.Socket client, String identity) {
		this.client = client;
		this.identity = identity;
	}
	
	public void Connect(String identity) {
 		client.setIdentity(identity.getBytes(ZMQ.CHARSET));
 		client.connect("tcp://192.168.1.3:5570");
 		client.send("Conn#"+identity+"#"+"Logging in");
 	}
	
	public void sendPublicKey(String identity) {
 		RSA = new RSA_ALGORITHM();
 		//Moderator can be friend later! To keep the general message pattern constant!
    	client.send("PubK#"+"Moderator"+"#"+identity+"#"+RSA.getPublicKey(identity));
 	}
	
	public boolean isAccepted(String message) {
		boolean isAllowed = false;
		messageSplitter = message.split("#",2);
		if(messageSplitter[0].equals("ACC_GRA") && 
				messageSplitter[1].equals("You have been accepted in the forum!")) {
			isAllowed = true;
		}
		return isAllowed;
	}
	
	public boolean isMissedMessage(String message) {
		boolean isMissed = false;
		messageSplitter = message.split("#",2);
		if(messageSplitter[0].equals("MissedM")) {
			isMissed = true;
		}
		
		return isMissed;
	}
	
	public void receiveMissedMessages(String message) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
			messageSplitter = message.split("#",4);
			String sender = messageSplitter[1];
			String missedMessage = messageSplitter[2];
			String IV = messageSplitter[3];
			AES = new AES_ALGORITHM();
			bhc = new ByteAndHexConversions();
			
			IvParameterSpec IVSpec = new IvParameterSpec(bhc.decodeHexString(IV));
			
			String decryptedMissedMessage = AES.Decrypt(bhc.decodeHexString(missedMessage), AES.getSharedSimmetricKey(identity), IVSpec);
			System.out.println(sender+" "+decryptedMissedMessage);
		
	}
	
	public void secretAuthentication() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		RSA = new RSA_ALGORITHM();
		bhc = new ByteAndHexConversions();
		
		byte[] publicKey = bhc.decodeHexString(RSA.getPublicKey("Moderator"));
		
		byte[] encryptedText = RSA.PublicEncryption("Correct Horse Battery Staple",publicKey);
		System.out.println(bhc.encodeHexString(encryptedText));
		
    	client.send("AuthM#"+"Moderator#"+identity+"#"+bhc.encodeHexString(encryptedText));
			
		}
	
	public void acceptedToForumMessage(String message) throws IOException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		bhc = new ByteAndHexConversions();
		
		messageSplitter = message.split("#",2);
		String enc_symmetricKey = messageSplitter[1];
		
		File symmKey = null;
		try {
			symmKey = new File("SymmKeyTo"+identity+".txt");
            if (symmKey.createNewFile())
              System.out.println("File created: " + symmKey.getName());
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
		
		byte[] symmetricKey = RSA.PrivateDecryption(bhc.decodeHexString(enc_symmetricKey), bhc.decodeHexString(RSA.getPrivateKey(identity)));
		String final_key = new String(symmetricKey, StandardCharsets.UTF_8);
		
		FileWriter fw = new FileWriter(symmKey);
		fw.write(final_key);
		fw.close();
	}
	
	public void sendMessage(String message,String friend) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		MAC = new MessageAuthenticationCode();
		AES = new AES_ALGORITHM();
		bhc = new ByteAndHexConversions();
		
		IvParameterSpec IV = AES.generateIv();
		
		byte[] enc_message = AES.Encrypt(message, AES.getSharedSimmetricKey(identity), IV);
		byte[] messageMAC = MAC.applyMAC_to_Message(message, identity);
        client.send("Mess#"+friend+"#"+identity+":#"+bhc.encodeHexString(enc_message)+"#"+bhc.encodeHexString(IV.getIV())+"#"+bhc.encodeHexString(messageMAC));
	}
	
	public void checkMessageIntegrity(String message) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		messageSplitter = message.split("#",5);
		String sender = messageSplitter[1];
		String content = messageSplitter[2];
		String IV = messageSplitter[3];
		String contentHASH = messageSplitter[4];
		
		bhc = new ByteAndHexConversions();
		MAC = new MessageAuthenticationCode();
		AES = new AES_ALGORITHM();
		
		IvParameterSpec IVSpec = new IvParameterSpec(bhc.decodeHexString(IV));
		
		String decrypted_message = AES.Decrypt(bhc.decodeHexString(content), AES.getSharedSimmetricKey(identity),IVSpec);
		
		
		if(MAC.isMatching(decrypted_message, contentHASH, identity)) {
			System.out.println(sender+" "+decrypted_message);
		}
		else {
			System.out.println("NOPITIY NOPE");
		}
	}
}

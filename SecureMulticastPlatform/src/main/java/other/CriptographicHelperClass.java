package other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.zeromq.ZContext;

import client.ClientLogin;
import gui.ForumController;

public class CriptographicHelperClass {
	private RSA_ALGORITHM RSA = new RSA_ALGORITHM();
	private ByteAndHexConversions bhc = new ByteAndHexConversions();
	private AES_ALGORITHM AES = new AES_ALGORITHM();
	private MessageAuthenticationCode MAC = new MessageAuthenticationCode();
	private String[] messageSplitter = new String[2];
	
	public String initialHelloHandshake() {
		
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		String helloHandshake = REQ.attemptToMsgServer("Hello",null,null);
		
		return helloHandshake;
	}
	
	public void saveServerPublicKey(String pubKey) {
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
			
			try {
				File publicKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\spubkey.txt"));
	            if (publicKeyFile.createNewFile())
	              System.out.println("File created: " + publicKeyFile.getName());
	            
	            FileWriter publicKeyWriter = new FileWriter(publicKeyFile);
	            publicKeyWriter.write(pubKey);
	            publicKeyWriter.close();
	            
	          } catch (IOException e) {
	            System.out.println("An error occurred.");
	            e.printStackTrace();
	          }
	}
	
	public void generateAndSaveUserKeyPair(String username) {
 		
 		if(!RSA.hasKeyPair(username)) {
 			System.out.println("Generating keypair for first-time use..");
 			
 			try {
				KeyPair keypair = RSA.GenerateKeyPair();
				byte[] publickey = keypair.getPublic().getEncoded();
				byte[] privatekey = keypair.getPrivate().getEncoded();
				
				RSA.SavePublicAndPrivateKey(bhc.encodeHexString(privatekey), username, 
						bhc.encodeHexString(publickey), username);
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
 		}
 	}
	
	public void generateAndSaveForumSimmetricKey(String forumName) throws IOException {
 		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
 		if(!RSA.hasKeyPair(forumName)) {
 			
 			try {
				String forumKey = bhc.encodeHexString(AES.generateEphemeralKey(256).getEncoded());
				
				File forumKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+forumName+".txt"));
	            if (forumKeyFile.createNewFile())
	              System.out.println("File created: " + forumKeyFile.getName());
	            
	            FileWriter publicKeyWriter = new FileWriter(forumKeyFile);
	            publicKeyWriter.write(forumKey);
	            publicKeyWriter.close();
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
 		}
 	}
	
	public String getForumSimmetricKey(String forumName) throws IOException {
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
		BufferedReader br = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir")
				.concat("WhelpData\\"+forumName+".txt"))); 
		
    	String forumKey;
    	forumKey = br.readLine();
    	br.close();
    	  
    	  return forumKey;
	}
	
	public boolean forumKeyExists(String forumName) {
		boolean exists = false;
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+forumName+".txt"));
		if(whelpfile.exists()) {
			exists = true;
		}
		
		return exists;
	}
	
	public void saveForumKey(String forumKey, String forumName) throws IOException {
 		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
 			
 			File forumKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+forumName+".txt"));
			if (forumKeyFile.createNewFile())
			  System.out.println("File created: " + forumKeyFile.getName());
			
			FileWriter publicKeyWriter = new FileWriter(forumKeyFile);
			publicKeyWriter.write(forumKey);
			publicKeyWriter.close();
 		}
	
	public boolean keyPairExists(String username) {
		
		boolean pubKExists = false;
		boolean  privKExists = false;
		boolean keyPairExists = false;
		
		File publicKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+username
				+"PUB.txt"));
		
        if (publicKeyFile.exists()) {
        	pubKExists = true;
        }
        
		File privateKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+username
				+"PRIV.txt"));
		
        if (privateKeyFile.exists()) {
        	privKExists = true;
        }
        
        if(privKExists && pubKExists) {
        	keyPairExists = true;
        }
        
        else if(privKExists && !pubKExists) {
        	
        	privateKeyFile.delete();
        }
        
        else if(!privKExists && pubKExists) {
        	
        	publicKeyFile.delete();
        }
        
        return keyPairExists;
        
	}
	
	public boolean forumMessageFileExists(String forumName) {
		boolean exists = false;
		
		File forumFile = new File(forumName+"_messages.txt");
		
		if(forumFile.exists()) {
			exists = true;
		}
		
		return exists;
	}
	
	public void createForumFile(String forumName) {
		
		File forumFile = new File(forumName+"_messages.txt");
		
		try {
			if (forumFile.createNewFile())
				  System.out.println("File created: " + forumFile.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToFileForumQuestion(String forumName, String forumQuestion, String IV) throws IOException {
		
		File forumFile = new File(forumName+"_messages.txt");
			
			FileWriter questionWriter = new FileWriter(forumFile,true);
			PrintWriter printWriter = new PrintWriter(questionWriter);
			
		    if(forumFile.length()==0) {
				printWriter.println("1");
				printWriter.println(forumQuestion);
				printWriter.println(IV);
		    }
		    
		    else {
		    	
		    	BufferedReader reader = new BufferedReader(new FileReader(forumFile));
				String currentForumElement = reader.readLine();
				int counter = 0;
				int totalquestions = 0;
				while(currentForumElement!=null) {
					if (counter == 0 || counter ==3){
						if(!currentForumElement.contains(".")) {
							counter=0;
							totalquestions=Integer.valueOf(currentForumElement);
						}
						
						else {
							counter=0;
						}
					}
					currentForumElement = reader.readLine();
					counter++;
				}
				
				reader.close();
		    	++totalquestions;
				printWriter.println(String.valueOf(totalquestions));
				printWriter.println(forumQuestion);
				printWriter.println(IV);
		    }
		
			printWriter.close();
		
	}
	
	public void writeToFileForumAnswer(String forumName, String forumAnswer, String IV, String questionNumber) throws IOException {
		File forumFile = new File(forumName+"_messages.txt");
		
		FileWriter questionWriter = new FileWriter(forumFile,true);
		PrintWriter printWriter = new PrintWriter(questionWriter);
	    	
	    	BufferedReader reader = new BufferedReader(new FileReader(forumFile));
			String currentForumElement = reader.readLine();
			int counter = 0;
			int totalAnswers = 0;
			boolean answerFound = false;
			while(currentForumElement!=null) {
				if (counter == 0 || counter ==3){
					counter = 0;
					if(currentForumElement.contains(".")) {
						answerFound = true;
						String[] splitter = currentForumElement.split("\\.",2);
						if(splitter[0].equals(questionNumber)) {
							
							if(Integer.valueOf(splitter[1])>totalAnswers)
							totalAnswers=Integer.valueOf(splitter[1]);
						}
					}
				}
				currentForumElement = reader.readLine();
				counter++;
			}
			reader.close();
			++totalAnswers;
			if(answerFound) {
				printWriter.println(questionNumber+"."+String.valueOf(totalAnswers));
				printWriter.println(forumAnswer);
				printWriter.println(IV);
			}
			
			else {
				printWriter.println("1.1");
				printWriter.println(forumAnswer);
				printWriter.println(IV);
			}
			
			printWriter.close();
	}
	
	public String getServerPublicKey() throws IOException {
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
		BufferedReader br = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir").concat("WhelpData\\spubkey.txt"))); 
    	String publicKey;
    	
    	  publicKey = br.readLine();
    			  
    	  br.close();
    	  
    	  return publicKey;
	}
	
	public String getUserPublicKey(String username) throws IOException {
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
		BufferedReader br = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+username+"PUB.txt"))); 
    	String publicKey;
    	
    	  publicKey = br.readLine();
    			  
    	  br.close();
    	  
    	  return publicKey;
	}
	
	public String getUserPrivateKey(String username) throws IOException {
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
		BufferedReader br = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+username+"PRIV.txt"))); 
    	String privateKey;
    	
    	  privateKey = br.readLine();
    			  
    	  br.close();
    	  
    	  return privateKey;
	}
	
	public String getCurrentTempUser() throws IOException {
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
		BufferedReader br = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir").concat("WhelpData\\temp.txt"))); 
    	String userName;
    	
    	  userName = br.readLine();
    			  
    	  br.close();
    	  
    	  return userName;
	}
	
	public String decryptServerAnswer(String message, SecretKey SecretKey, IvParameterSpec IV) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
		String decryptedServerAnswer = AES.Decrypt(bhc.decodeHexString(message), SecretKey, IV);
		
		messageSplitter = decryptedServerAnswer.split("#",2);
		String serverMessage = messageSplitter[0];
		String signedMAC = messageSplitter[1];
		String finalMessage;
		
		if(MAC.isSignatureMatching(serverMessage, signedMAC, SecretKey)) {
			finalMessage = serverMessage;
		}
		
		else {
			finalMessage = "Message has been tampered with!";
		}
		
		return finalMessage;
	}
	
	public String privateDecryptWithServerKey(String message) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		String privateKey = RSA.getPrivateKey("RegisterLoginServer");
		byte[] decrypted_message = RSA.PrivateDecryption(bhc.decodeHexString(message), bhc.decodeHexString(privateKey));
		String final_message = new String(decrypted_message, StandardCharsets.UTF_8);
		
		return final_message;
	}
	
	public String publicEncryptWithClientKey(String message, String keyOwner) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String enc_clientMessage = bhc.encodeHexString(RSA.PublicEncryption(message, bhc.decodeHexString(keyOwner)));
		
		return enc_clientMessage;
	}
	
	public String privateDecryptWithClientKey(String message, String keyOwner) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		String privateKey = getUserPrivateKey(keyOwner);
		byte[] decrypted_message = RSA.PrivateDecryption(bhc.decodeHexString(message), bhc.decodeHexString(privateKey));
		String final_message = new String(decrypted_message, StandardCharsets.UTF_8);
		
		return final_message;
	}
	
	public String publicEncryptWithServerKey(String message) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		String enc_serverMessage = bhc.encodeHexString(RSA.PublicEncryption(message,
				bhc.decodeHexString(getServerPublicKey())));
		
		return enc_serverMessage;
	}
	
	public SecretKey initializeSimmetricKey(String secretKey) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String privKey = RSA.getPrivateKey("RegisterLoginServer");
		byte[] secret_key = RSA.PrivateDecryption(bhc.decodeHexString(secretKey), bhc.decodeHexString(privKey));
		String final_key = new String(secret_key, StandardCharsets.UTF_8);
		SecretKey originalKey = new SecretKeySpec(bhc.decodeHexString(final_key), 0, bhc.decodeHexString(final_key).length, "AES");
		return originalKey;
	}
	
	public SecretKey initializeSessionKey(String secretKey) {
		SecretKey originalKey = new SecretKeySpec(bhc.decodeHexString(secretKey), 0, bhc.decodeHexString(secretKey).length, "AES");
		return originalKey;
	}
	
	public IvParameterSpec initializeIV(String IV) {
		IvParameterSpec IVSpec = new IvParameterSpec(bhc.decodeHexString(IV));
		return IVSpec;
	}
	
	public String decryptMultipartMessage(String message, String IV, String secretKey) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		IvParameterSpec IVSpec = initializeIV(IV);
		SecretKey simmetricKey = initializeSimmetricKey(secretKey);
		String decrypted_message = AES.Decrypt(bhc.decodeHexString(message), simmetricKey, IVSpec);
		
		return decrypted_message;
	}
	
	public String encryptServerMessage(String message, String IV, String SecretKey) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		IvParameterSpec IVSpec = initializeIV(IV);
		SecretKey secretKey = initializeSimmetricKey(SecretKey);
		
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(message, secretKey));
		String finalMessage = message+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IVSpec));
		
		return enc_finalMessage;
	}
	
	public boolean MACMatches(String datablock, String signedMAC, String secretKey) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		boolean matches = false;
		SecretKey simmetricKey = initializeSimmetricKey(secretKey);
		if(MAC.isSignatureMatching(datablock, signedMAC, simmetricKey)) {
			matches = true;
		}
		
		return matches;
	}
	
	public String sendUsername(String serverPubKey, String username, String tag) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(username, secretKey));
		String enc_secretKey = bhc.encodeHexString(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = tag+"#"+username+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		
		return finalAnswer;
		
	}
	
	public static void main(String[] args) {
		
	}
	
}

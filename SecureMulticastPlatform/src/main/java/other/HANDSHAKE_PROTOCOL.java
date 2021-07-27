package other;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class HANDSHAKE_PROTOCOL {
	
	private RSA_ALGORITHM RSA = new RSA_ALGORITHM();
	private ByteAndHexConversions bhc = new ByteAndHexConversions();
	private AES_ALGORITHM AES = new AES_ALGORITHM();
	private WHelpDatabase UD = new WHelpDatabase();
	private MessageAuthenticationCode MAC = new MessageAuthenticationCode();
	private SALTEDHASH_ALGORITHM SHA = new SALTEDHASH_ALGORITHM();
	private String serverAnswer;
	private CriptographicHelperClass CHC = new CriptographicHelperClass();
	
	public String sendEncryptedRegistrationData(String serverPubKey, String firstName, String lastName, String username, String password) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		String hashedPass = bhc.encodeHexString(SHA.HASHChain(password, 999));
		String confidentialDataBlock = firstName+""+lastName+""+username+""+hashedPass;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(confidentialDataBlock, secretKey));
		String enc_secretKey = bhc.encodeHexString(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "REG#"+firstName+"#"+lastName+"#"+username+"#"+hashedPass+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = CHC.decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		
		return finalAnswer;
	}
	
	public void finalizeRegistrationProcess(String message,String IV,String secretKey,Socket server) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SQLException {
		
		String[] messageSplitter = message.split("#",6);
		
		String firstName = messageSplitter[1];
		String lName = messageSplitter[2];
		String Username = messageSplitter[3];
		String hashedPassword = messageSplitter[4];
		String signedMAC = messageSplitter[5];
		String datablock = firstName+""+lName+""+Username+""+hashedPassword;
		
		if(CHC.MACMatches(datablock, signedMAC,secretKey)) {
			UD.getConnection("Users");
			
			if(UD.userExists(Username, "Users")) {
				serverAnswer = CHC.encryptServerMessage("Username taken!", IV, secretKey);
				server.send(serverAnswer);
			}
			
			else {
				byte[] salt = SHA.generateSalt();
				String finalPassword = bhc.encodeHexString(SHA.saltAndHASHPassword(hashedPassword, salt));
				UD.addUser(firstName, lName, Username, finalPassword, bhc.encodeHexString(salt), 1000, "Users");
				serverAnswer = CHC.encryptServerMessage("Registration successful!", IV, secretKey);
				server.send(serverAnswer);
			}
		}
		else {
			serverAnswer = CHC.encryptServerMessage("Client signature missmatch!", IV, secretKey);
			server.send(serverAnswer);
		}
	}
	
	public void verifyLoginData(String message, String IV, String secretKey, Socket server) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException, InvalidKeySpecException {
		String[] messageSplitter = message.split("#",3);
		
		String Username = messageSplitter[1];
		String signedMAC = messageSplitter[2];
		
		if(CHC.MACMatches(Username, signedMAC, secretKey)) {
			UD.getConnection("Users");
			
			if(UD.userExists(Username, "Users")) {
				int authNR = UD.getAuthenticationsLeft("Users", Username);
				serverAnswer = CHC.encryptServerMessage(String.valueOf(authNR), IV, secretKey);
				server.send(serverAnswer);
			}
			
			else {
				serverAnswer = CHC.encryptServerMessage("Missmatch credentials!", IV, secretKey);
				server.send(serverAnswer);
			}
		}
		
		else {
			serverAnswer = CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
			server.send(serverAnswer);
		}
	}
	
	public String sendFinalLoginData(String serverPubKey, String username, String password, int hashesNeeded) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		ZContext ctx = new ZContext();
		ClientLogin REQ = new ClientLogin(ctx);
		SecretKey secretKey = AES.generateEphemeralKey(256);
		IvParameterSpec IV = AES.generateIv();
		String hashedPass = bhc.encodeHexString(SHA.HASHChain(password, hashesNeeded-1));
		String confidentialDataBlock = username+""+hashedPass;
		String signedMessage = bhc.encodeHexString(MAC.applyMAC_to_MessageForRegistration(confidentialDataBlock, secretKey));
		String enc_secretKey = bhc.encodeHexString(RSA.PublicEncryption(bhc.encodeHexString(secretKey.getEncoded()), bhc.decodeHexString(serverPubKey)));
		String IVSPEC = bhc.encodeHexString(IV.getIV());
		String finalMessage = "FINLOG#"+username+"#"+hashedPass+"#"+signedMessage;
		String enc_finalMessage = bhc.encodeHexString(AES.Encrypt(finalMessage, secretKey, IV));
		String serverAnswer = REQ.attemptToMsgServer(enc_finalMessage,IVSPEC,enc_secretKey);
		String finalAnswer=null;
		if(serverAnswer!=null) {
			finalAnswer = CHC.decryptServerAnswer(serverAnswer, secretKey, IV);
		}
		
		return finalAnswer;
	}
	
	public void finalizeLoginProcess(String message, String IV, String secretKey, Socket server) throws SQLException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		String[] messageSplitter = message.split("#",4);
		String Username = messageSplitter[1];
		String hashedPassword = messageSplitter[2];
		String signedMAC = messageSplitter[3];
		String datablock = Username+""+hashedPassword;
		
		
		if(CHC.MACMatches(datablock, signedMAC, secretKey)) {
			UD.getConnection("Users");
			
			if(UD.userExists(Username, "Users")) {
				SALTEDHASH_ALGORITHM SHA = new SALTEDHASH_ALGORITHM();
				byte[] salt = bhc.decodeHexString(UD.getSalt("Users", Username));
				int authNR = UD.getAuthenticationsLeft("Users", Username);
				String finalPassword="";
				
				if(authNR == 1000) {
					finalPassword = bhc.encodeHexString(SHA.saltAndHASHPassword(hashedPassword, salt));
				}
				else {
					String chain = bhc.encodeHexString(SHA.ContinueHASHCHAIN(hashedPassword, 1000-authNR));
					finalPassword = bhc.encodeHexString(SHA.saltAndHASHPassword(chain, salt));
				}
				
				if(finalPassword.equals(UD.getPassword("Users", Username))) {
					
					UD.updateAuthenticationNR("Users", Username, authNR);
					
					if(UD.userExists(Username, "PublicKeysTable")) {
						serverAnswer = CHC.encryptServerMessage("Credentials match!", IV, secretKey);
						server.send(serverAnswer);
					}
					
					else {
						serverAnswer = CHC.encryptServerMessage("No public key found!", IV, secretKey);
						server.send(serverAnswer);
					}
				}
				else {
					
					serverAnswer = CHC.encryptServerMessage("Missmatch credentials!", IV, secretKey);
					server.send(serverAnswer);
				}
			}
			
			else {
				serverAnswer = CHC.encryptServerMessage("Error!", IV, secretKey);
				server.send(serverAnswer);
			}
		}
		
		else {
			serverAnswer = CHC.encryptServerMessage("Rejected signature!", IV, secretKey);
			server.send(serverAnswer);
		}
	}
}

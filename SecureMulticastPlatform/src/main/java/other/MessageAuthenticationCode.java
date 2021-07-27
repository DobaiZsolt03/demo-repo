package other;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class MessageAuthenticationCode {
	
	ByteAndHexConversions bhc = new ByteAndHexConversions();
	
	public byte[] applyMAC_to_Message(String message, String user) throws NoSuchAlgorithmException, InvalidKeyException {
		 Mac mac = Mac.getInstance("HmacSHA256");
	     AES_ALGORITHM AES = new AES_ALGORITHM();
	     
	     mac.init(AES.getSharedSimmetricKey(user));
	     
	     byte[] MAC_result = mac.doFinal(message.getBytes());
	     
	     return MAC_result;
	}
	
	public byte[] applyMAC_to_MessageForRegistration(String message, SecretKey ephemeralKey) throws NoSuchAlgorithmException, InvalidKeyException {
		 Mac mac = Mac.getInstance("HmacSHA256");
	     
	     mac.init(ephemeralKey);
	     
	     byte[] MAC_result = mac.doFinal(message.getBytes());
	     
	     return MAC_result;
	}
	
	public boolean isMatching(String decrypted_message, String hashCode, String user) throws InvalidKeyException, NoSuchAlgorithmException {
		boolean matches = false;
		
		if(bhc.encodeHexString(applyMAC_to_Message(decrypted_message, user)).equals(hashCode)){
			matches = true;
		}
		
		return matches;
	}
	
	public boolean isSignatureMatching(String decrypted_message, String hashCode, SecretKey ephemeralKey) throws InvalidKeyException, NoSuchAlgorithmException {
		boolean matches = false;
		
		if(bhc.encodeHexString(applyMAC_to_MessageForRegistration(decrypted_message,ephemeralKey)).equals(hashCode)){
			matches = true;
		}
		
		return matches;
	}
	
	public static void main(String args[]) throws Exception{

	      //Creating a Mac object
		
		
	      
	   }
}

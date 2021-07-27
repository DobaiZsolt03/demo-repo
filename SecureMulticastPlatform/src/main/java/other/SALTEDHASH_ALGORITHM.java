package other;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SALTEDHASH_ALGORITHM {
	
	public byte[] generateSalt() throws NoSuchAlgorithmException {
		
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		
		byte[] salt = new byte[16];
		
		sr.nextBytes(salt);
		
		return salt;
	}
	
	public byte[] saltAndHASHPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(salt);
		byte hashedPass[] = md.digest(password.getBytes());
		
		return hashedPass;
	}
	
	
	public byte[] HASHPassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte hashedPass[] = md.digest(password.getBytes());
		
		return hashedPass;
	}
	
	public byte[] HASHChain(String password, int nr_of_chains) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		ByteAndHexConversions bhc = new ByteAndHexConversions();
		
		byte hashedPass[] = password.getBytes();
		
		for(int i=0;i<nr_of_chains;i++) {
			hashedPass = md.digest(hashedPass);
		}
		
		return hashedPass;
	}
	
	public byte[] ContinueHASHCHAIN(String hashedpassword, int nr_of_chains) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		ByteAndHexConversions bhc = new ByteAndHexConversions();
		
		byte hashedPass[] = bhc.decodeHexString(hashedpassword);
		
		for(int i=0;i<nr_of_chains;i++) {
			hashedPass = md.digest(hashedPass);
		}
		
		return hashedPass;
	}
	
	public static void main(String[] args) {
		
	}
}

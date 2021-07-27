package other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_ALGORITHM {
	
 
    public void generateKey(int keysize) throws NoSuchAlgorithmException, IOException {
    	File simmetricKey;
    	ByteAndHexConversions bhc = new ByteAndHexConversions();
    	try {
    		simmetricKey = new File("SecretSimmetricKey.txt");
            if (simmetricKey.createNewFile())
              System.out.println("File created: " + simmetricKey.getName());
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
    	
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keysize);
        SecretKey key = keyGenerator.generateKey();
        
        FileWriter myWriter = new FileWriter("SecretSimmetricKey.txt");
        myWriter.write(bhc.encodeHexString(key.getEncoded()));
        myWriter.close();
    }
    
    public SecretKey generateEphemeralKey(int keysize) throws NoSuchAlgorithmException {
    	 KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
         keyGenerator.init(keysize);
         SecretKey key = keyGenerator.generateKey();
         
         return key;
    }
    
    public SecretKey getSimmetricKey() {
    	ByteAndHexConversions bhc = new ByteAndHexConversions();
    	File fileName = new File("SecretSimmetricKey.txt");
		String simmetricKey = null;
		
		Scanner sc = null;
		try {
			sc = new Scanner(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(sc.hasNextLine()) {
			if(sc.hasNextLine()) {
				simmetricKey = sc.nextLine();
	    	}
		}
		
		sc.close();
		
		
		byte[] byteKey = bhc.decodeHexString(simmetricKey);
		SecretKey convertedKey = new SecretKeySpec(byteKey, 0, byteKey.length, "AES");
		
		return convertedKey;
    }
    
    public SecretKey getSharedSimmetricKey(String user) {
    	ByteAndHexConversions bhc = new ByteAndHexConversions();
    	File fileName = new File("SymmKeyTo"+user+".txt");
		String simmetricKey = null;
		
		Scanner sc = null;
		try {
			sc = new Scanner(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(sc.hasNextLine()) {
			if(sc.hasNextLine()) {
				simmetricKey = sc.nextLine();
	    	}
		}
		
		sc.close();
		
		
		byte[] byteKey = bhc.decodeHexString(simmetricKey);
		SecretKey convertedKey = new SecretKeySpec(byteKey, 0, byteKey.length, "AES");
		
		return convertedKey;
    }
 
    public IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
 
    public byte[] Encrypt(String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
    	    InvalidAlgorithmParameterException, InvalidKeyException,
    	    BadPaddingException, IllegalBlockSizeException {
    	    
    	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    	    byte[] cipherText = cipher.doFinal(input.getBytes());
    	    return cipherText;
    	}
    
    public String Decrypt(byte[] cipherText, SecretKey key,IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
    	    InvalidAlgorithmParameterException, InvalidKeyException,
    	    BadPaddingException, IllegalBlockSizeException {
    	    
    	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    	    byte[] plainText = cipher.doFinal(cipherText);
    	    return new String(plainText);
    	}
	
	public static void main (String args[]) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		
		/*
		AES_ALGORITHM AES = new AES_ALGORITHM();
		ByteAndHexConversions bhc = new ByteAndHexConversions();
	    IvParameterSpec ivParameterSpec = AES.generateIv();
	    
	    IvParameterSpec ivParameterSpec2 = new IvParameterSpec(bhc.decodeHexString(bhc.encodeHexString(ivParameterSpec.getIV())));
	    
	    System.out.println(ivParameterSpec.getIV());
	    System.out.println(bhc.encodeHexString(ivParameterSpec.getIV()));
	    
		
		byte[] enc_message = AES.Encrypt("EcPeccKimeheccHolnaputanbejohetszCernaracinegereugorjcicaazegerrehophophopmertittamacskagyorsanfussmertelkapmertamacskaeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeelkapennyertem", AES.getSimmetricKey(), ivParameterSpec);
		
	    System.out.println(bhc.encodeHexString(enc_message));
	    System.out.println(AES.Decrypt(enc_message, AES.getSimmetricKey(), ivParameterSpec2));
	    */
		
		File forumFile = new File("The Trojan War_messages.txt");
		
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
							System.out.println(questionNumber);
							System.out.println(forumQuestion);
							System.out.println(Iv);
						}
					}
				}
				
				else{
					
					questionNumber = currentForumElement;
					forumQuestion = reader.readLine();
					Iv = reader.readLine();
					
					currentForumElement = reader.readLine();
					
					if(currentForumElement==null) {
						System.out.println(questionNumber);
						System.out.println(forumQuestion);
						System.out.println(Iv);
					}
				}
		}
		
		reader.close();
		
	    
	}
	
}

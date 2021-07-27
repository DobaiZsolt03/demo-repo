package other;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA_ALGORITHM {
	
	ByteAndHexConversions bhc = new ByteAndHexConversions();
	
	public KeyPair GenerateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
		kpGen.initialize(1024);
		KeyPair keypair = kpGen.generateKeyPair();
		
		return keypair;
	}
	
	public void SavePublicAndPrivateKey(String privateKey, String user_privateKey, 
			String publicKey, String user_publicKey){
		
		File whelpfile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData"));
        
		if(!whelpfile.exists()) {
			whelpfile.mkdir();
		}
		
		try {
			File publicKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+user_publicKey
					+"PUB.txt"));
			
            if (publicKeyFile.createNewFile())
              System.out.println("File created: " + publicKeyFile.getName());
            
            FileWriter publicKeyWriter = new FileWriter(publicKeyFile);
            publicKeyWriter.write(publicKey);
            publicKeyWriter.close();
            
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
		
		try {
			File privateKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+user_privateKey
					+"PRIV.txt"));
			
            if (privateKeyFile.createNewFile())
              System.out.println("File created: " + privateKeyFile.getName());
            
            FileWriter publicKeyWriter = new FileWriter(privateKeyFile);
            publicKeyWriter.write(privateKey);
            publicKeyWriter.close();
            
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
		
	}
	
	public boolean hasKeyPair(String identity) {
 		
 		boolean hasKey = false;
 		
 		File publicKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+identity+"PUB.txt"));
 		File privateKeyFile = new File(System.getProperty("java.io.tmpdir").concat("WhelpData\\"+identity+"PRIV.txt"));
 		
 		if(publicKeyFile.exists() && privateKeyFile.exists()) {
        	hasKey = true;
        }
 		else {
 			hasKey = false;
 		}
 		
 		return hasKey;
 		
 	}
	
	public void saveKey(String identity) {
 		
 		if(!hasKeyPair(identity)) {
 			System.out.println("Generating keypair for first-time use..");
 			
 			try {
				KeyPair keypair = GenerateKeyPair();
				byte[] publickey = keypair.getPublic().getEncoded();
				byte[] privatekey = keypair.getPrivate().getEncoded();
				
				SavePublicAndPrivateKey(bhc.encodeHexString(privatekey), identity, 
						bhc.encodeHexString(publickey), identity);
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
 		}
 	}
	
	public String getPublicKey(String username) {
		
		File publicKeyFile = new File(username+"_pub.txt");
		String publicKey = null;
		
		Scanner sc = null;
		try {
			sc = new Scanner(publicKeyFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	  
	    while (sc.hasNextLine()) {
	    	if(sc.hasNextLine()) {
	    		publicKey = sc.nextLine();
	    	}
	    }
		
		sc.close();
		
		return publicKey;
	}
	
	public String getPrivateKey(String username) {
		
		File privateKeyFile = new File(username+"_priv.txt");
		String privateKey = null;
		
		Scanner sc = null;
		try {
			sc = new Scanner(privateKeyFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(sc.hasNextLine()) {
			if(sc.hasNextLine()) {
	    		privateKey = sc.nextLine();
	    	}
		}
		
		sc.close();
		
		return privateKey;
	}
    
	public byte[] PublicEncryption(String message, byte[] Converted_publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		Cipher cipher = Cipher.getInstance("RSA");
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Converted_publicKey));
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedText = cipher.doFinal(message.getBytes());
		
		return encryptedText;
		
	}
	
	public byte[] PrivateDecryption(byte[] enc_message, byte[] Converted_privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		Cipher cipher = Cipher.getInstance("RSA");
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Converted_privateKey));
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedText = cipher.doFinal(enc_message);
		
		return decryptedText;
		
	}
	
	public byte[] PrivateEncryption(String message, byte[] Converted_privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		Cipher cipher = Cipher.getInstance("RSA");
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Converted_privateKey));
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		byte[] encryptedText = cipher.doFinal(message.getBytes());
		
		return encryptedText;
		
	}
	
	public byte[] PublicDecryption(byte[] enc_message, byte[] Converted_publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		Cipher cipher = Cipher.getInstance("RSA");
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Converted_publicKey));
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		byte[] decryptedText = cipher.doFinal(enc_message);
		
		return decryptedText;
		
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeySpecException {
		
		
		RSA_ALGORITHM RSA = new RSA_ALGORITHM();
		ByteAndHexConversions bhc = new ByteAndHexConversions();
		
		byte[] publicKey = bhc.decodeHexString(RSA.getPublicKey("Alice"));
		byte[] privateKey = bhc.decodeHexString(RSA.getPrivateKey("Alice"));
		
		byte[] encryptedText = RSA.PublicEncryption("Salamalaiku MalaikumSalai",publicKey);
		System.out.println(bhc.encodeHexString(encryptedText));
	
		byte[] decryptedText = RSA.PrivateDecryption(encryptedText,privateKey);
		System.out.println(new String(decryptedText, StandardCharsets.UTF_8));
	
	}

}

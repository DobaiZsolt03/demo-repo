package client;

import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import other.ByteAndHexConversions;
import other.RSA_ALGORITHM;

public class ChatClients implements Runnable
{
	private ZContext ctx = new ZContext();
	public ZMQ.Socket client;
 	private File publicKeyFile;
 	private File privateKeyFile;
 	private RSA_ALGORITHM RSA;
 	private ClientCommands cc;
 	private ByteAndHexConversions bhc = new ByteAndHexConversions();
 	
 	private void setSocket(ZMQ.Socket client) {
 		 client = ctx.createSocket(ZMQ.DEALER);
 		 this.client = client;
 	}
 	
 	
 	
 	private boolean hasKeyPair(String identity) {
 		
 		 
 		boolean hasKey = false;
 		publicKeyFile = new File(identity+"_pub.txt");
 		privateKeyFile = new File(identity+"_priv.txt");
 		
 		if(publicKeyFile.exists() && privateKeyFile.exists()) {
        	hasKey = true;
        }
 		else {
 			hasKey = false;
 		}
 		
 		return hasKey;
 		
 	}
 	
 	private void saveKey(String identity) {
 		
 		if(hasKeyPair(identity)) {
 			System.out.println(identity+" has a keypair..");
 		}
 		else {
 			System.out.println(identity+" does not possess a keypair..");
 			System.out.println("Generating keypair..");
 			
 			RSA = new RSA_ALGORITHM();
 			try {
				KeyPair keypair = RSA.GenerateKeyPair();
				byte[] publickey = keypair.getPublic().getEncoded();
				byte[] privatekey = keypair.getPrivate().getEncoded();
				
				RSA.SavePublicAndPrivateKey(bhc.encodeHexString(privatekey), identity, 
						bhc.encodeHexString(publickey), identity);
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
 		}
 	}

    @Override
    public void run()
    {
    		setSocket(client);
    		Scanner input = new Scanner(System.in);
    		
    		System.out.println("Welcome! Please provide an identity!");
    		
    		String identity = input.nextLine();
    		
    		new Thread(new ClientListenerThread(client,identity)).start();
            
            cc = new ClientCommands(client,identity);
            
            cc.Connect(identity);
            
            saveKey(identity);
            cc.sendPublicKey(identity);
            
            System.out.println("Who do you want to send this message to "+identity+"?");
            String friend = input.nextLine();
            System.out.println("Write a message "+identity+"!");

            while (!Thread.currentThread().isInterrupted()) {
            	String message = input.nextLine();    
            	
            		try {
						cc.sendMessage(message, friend);
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
					}
            }
            
            input.close();
            ctx.close();
    }
    
    public static void main(String[] args) {
    	
    	new Thread(new ChatClients()).start();
    	
    }
}

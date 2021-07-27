package client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import other.ByteAndHexConversions;
import other.RSA_ALGORITHM;

public class Moderator implements Runnable{

	public String identity = "Moderator";
	private ZContext ctx = new ZContext();
	public ZMQ.Socket moderator;
 	private File publicKeyFile;
 	private File privateKeyFile;
 	private RSA_ALGORITHM RSA;
 	private ModeratorCommands mc;
 	private ByteAndHexConversions bhc = new ByteAndHexConversions();
 	
 	private void setSocket(ZMQ.Socket moderator) {
 		 moderator = ctx.createSocket(ZMQ.DEALER);
 		 this.moderator = moderator;
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
    	setSocket(moderator);
    	mc = new ModeratorCommands(moderator,identity);
        mc.Connect(identity);
        saveKey(identity);
        
        System.out.println("I am the "+identity+"! I handle incoming requests to join the forum");
        System.out.println("-------------------------------------------------------------------");
        Scanner choice = new Scanner(System.in);
		
            while (!Thread.currentThread().isInterrupted()) {
            	ZMsg msg = ZMsg.recvMsg(moderator);
            	
            	if(mc.isForumRequest(msg)) {
            		mc.joinForumRequest(msg, choice);
            	}
            	
            	else if(mc.isSecretMess(msg)) {
            		try {
						mc.secretMessage(msg);
					} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	
            	/*
            	else{
            		//answer from the server, that fetches all pending users, needed to be accepted!
            		System.out.println(msg.getLast());
            		System.out.println("These users want to join our forum! What should we do?");
            		System.out.println("Y - ACCEPT user, N - ACCEPT NONE");
            		
            		answer = choice.next();
            		
            		 if(answer.toUpperCase().equals("N"))
            			System.out.println("All users rejected!");
            		
            		 else if(answer.toUpperCase().equals("Y")) {
            			System.out.println("Which user do you want to add?");
            			userToAdd= choice.next(); 
            			moderator.send(identity+"#addU#"+answer+"#"+userToAdd);
            			System.out.println("User added to the accepted table!");
            			
            			while(!answer.toUpperCase().equals("N")) {
            				System.out.println("Anyone else?");
                			answer = choice.next();
                			if(answer.toUpperCase().equals("N")) {
                				System.out.println("Allright!");
                			}
                			else if(answer.toUpperCase().equals("Y")) {
                				System.out.println("Who?");
                				userToAdd = choice.next();
                				moderator.send(identity+"#addU#"+answer+"#"+userToAdd);
                			}
            			}
            		}
            	}
            		*/
                msg.destroy();
            }
            choice.close();
        ctx.close();
    }
    
    public static void main(String[] args) {
    	
    	new Thread(new Moderator()).start();
    	
    }

}

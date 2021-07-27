package client;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import other.MessageAuthenticationCode;
import other.RSA_ALGORITHM;

public class ClientListenerThread implements Runnable{
	public String identity;
	private ZMQ.Socket client;
	private ClientCommands cc;
	public MessageAuthenticationCode MAC;
	
	public ClientListenerThread(ZMQ.Socket client, String identity) {
		this.client = client;
		this.identity = identity;
	}
	
	@Override
	public void run() {
		
			ClientCommands cc = new ClientCommands(client,identity);
 		
            while (!Thread.currentThread().isInterrupted()) {
    		
            	ZMsg msg = ZMsg.recvMsg(client);
            	
            	if(cc.isMissedMessage(msg.getLast().toString())){
            		try {
						cc.receiveMissedMessages(msg.getLast().toString());
					} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
							| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	
            	if(cc.isAccepted(msg.getLast().toString())) {
						try {
							cc.secretAuthentication();
						} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
								| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            	}
            	else if(msg.getLast().toString().startsWith("Accepted#")) {
            		try {
						cc.acceptedToForumMessage(msg.getLast().toString());
					} catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
							| IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	
            	else if(msg.getLast().toString().startsWith("Mess#")){
            		try {
						cc.checkMessageIntegrity(msg.getLast().toString());
					} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
							| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	
            		msg.destroy();
    	           
            	}
			}
	}

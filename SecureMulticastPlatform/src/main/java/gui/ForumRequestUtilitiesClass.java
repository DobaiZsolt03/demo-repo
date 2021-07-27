package gui;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import other.AES_ALGORITHM;
import other.ForumRequests;

public class ForumRequestUtilitiesClass {
	
	private AES_ALGORITHM AES = new AES_ALGORITHM();
	
	public ArrayList<String> fetchForumsList() throws IOException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		ForumRequests FR = new ForumRequests();
		ArrayList<String> finalArray = new ArrayList<String>();
		
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendJoinedForumsListREQ(ctx,IV);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumsList;
						forumsList = FR.receiveJoinedForumsList(subscriber, IV);
					
					if(forumsList.isEmpty()) {
						finalArray = null;
					}
					
					else {
						for(int i=0;i<forumsList.size();i++) {
							messageSplitter = forumsList.get(i).split("#",3);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(messageSplitter[1]);
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				}
			}
			return finalArray;
		}
	
	public ArrayList<String> fetchForumInvites(String username) throws IOException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		ForumRequests FR = new ForumRequests();
		ArrayList<String> finalArray = new ArrayList<String>();
		
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendInvitedForumREQ(ctx,IV,username);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumsList;
						forumsList = FR.receiveInvitedForumREQ(subscriber, IV);
					
					if(forumsList.isEmpty()) {
						finalArray = null;
					}
					
					else {
						for(int i=0;i<forumsList.size();i++) {
							messageSplitter = forumsList.get(i).split("#",3);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(messageSplitter[1]);
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				}
			}
			return finalArray;
		}
	
	public ArrayList<String> fetchNotJoinedForumsList() {
		ForumRequests FR = new ForumRequests();
		
		ArrayList<String> finalArray = new ArrayList<String>();
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
		try {
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendNotJoinedForumListREQ(ctx, IV);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumsList = FR.receiveNotJoinedForumsList(subscriber, IV);
					
					if(forumsList.isEmpty()) {
						finalArray = null;
					}
					
					else {
						for(int i=0;i<forumsList.size();i++) {
							messageSplitter = forumsList.get(i).split("#",6);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(forumsList.get(i));
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
						
					}
				}
				
				else {
					finalArray = null;
				}
			}
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
		
		return finalArray;
				
	}
	
	public ArrayList<String> fetchForumMembersList(String forumName, String username) {
		ForumRequests FR = new ForumRequests();
		
		ArrayList<String> finalArray = new ArrayList<String>();
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
		try {
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendForumMembersListREQ(ctx, IV, forumName, username);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumMembersList = FR.receiveForumMembersList(subscriber, IV);
					
						for(int i=0;i<forumMembersList.size();i++) {
							messageSplitter = forumMembersList.get(i).split("#",4);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(forumMembersList.get(i));
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				
				else {
					finalArray = null;
				}
			}
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
		return finalArray;
				
	}
	
	public ArrayList<String> fetchPendingForumREQ(String forumName, String username) {
		ForumRequests FR = new ForumRequests();
		
		ArrayList<String> finalArray = new ArrayList<String>();
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
		try {
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendPendingForumREQ(ctx, IV, forumName, username);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> pendingForumREQList = FR.receivePendingForumREQ(subscriber, IV);
					
						for(int i=0;i<pendingForumREQList.size();i++) {
							messageSplitter = pendingForumREQList.get(i).split("#",3);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(pendingForumREQList.get(i));
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				
				else {
					finalArray = null;
				}
			}
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
		return finalArray;
				
	}
	
	public ArrayList<String> fetchForumQuestions(String username, String forumName, String subject, String question) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		ForumRequests FR = new ForumRequests();
		ArrayList<String> finalArray = new ArrayList<String>();
		
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendForumQuestion(ctx,IV,username, forumName, subject, question);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				System.out.println("I'm here!");
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumQuestions;
					forumQuestions = FR.receiveForumQuestions(subscriber);
					
					if(forumQuestions.isEmpty()) {
						finalArray = null;
					}
					
					else {
						for(int i=0;i<forumQuestions.size();i++) {
							messageSplitter = forumQuestions.get(i).split("#",2);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(messageSplitter[1]);
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				}
			}
			return finalArray;
				
	}
	
	public ArrayList<String> fetchForumQuestionsModeratorMenu(String forumName, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		ForumRequests FR = new ForumRequests();
		ArrayList<String> finalArray = new ArrayList<String>();
		
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendForumQuestionsListREQ(ctx,IV, username, forumName);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumQuestions;
					forumQuestions = FR.receiveForumQuestions(subscriber);
					
					if(forumQuestions.isEmpty()) {
						finalArray = null;
					}
					
					else {
						for(int i=0;i<forumQuestions.size();i++) {
							messageSplitter = forumQuestions.get(i).split("#",2);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(messageSplitter[1]);
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				}
			}
			return finalArray;
				
	}
	
	public ArrayList<String> fetchForumAnswers(String forumName, String username, String questionNumber) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		ForumRequests FR = new ForumRequests();
		ArrayList<String> finalArray = new ArrayList<String>();
		
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendForumAnswersListREQ(ctx,IV, username, forumName, questionNumber);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> forumAnswers;
					forumAnswers = FR.receiveForumAnswers(subscriber);
					
					if(forumAnswers.isEmpty()) {
						finalArray = null;
					}
					
					else {
						for(int i=0;i<forumAnswers.size();i++) {
							messageSplitter = forumAnswers.get(i).split("#",2);
							if(messageSplitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(messageSplitter[1]);
							}
						}
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				}
			}
			return finalArray;
	}
	
	public ArrayList<String> fetchLatestForumQuestions(String forumName, String username) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		ForumRequests FR = new ForumRequests();
		ArrayList<String> finalArray = new ArrayList<String>();
		
		ZContext ctx = new ZContext();
		Socket subscriber = ctx.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://10.191.11.68:53");
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        
			IvParameterSpec IV = AES.generateIv();
			String serverAnswer = FR.sendLatestForumQuestionsREQ(ctx,IV, username, forumName);
			String[] messageSplitter = null;
			if(serverAnswer==null) {
				finalArray = null;
			}
			
			else if(serverAnswer.equals("")) {
				finalArray = null;
			}
			
			else {
				messageSplitter = serverAnswer.split("#",3);
				if(messageSplitter[0].equals(ForumController.nonceClient) && 
						messageSplitter[1].equals("Sending volumes of data!")) {
					
					ArrayList<String> latestForumQuestions;
					latestForumQuestions = FR.receiveLatestForumQuestions(subscriber);
					if(latestForumQuestions.isEmpty()) {
						finalArray = null;
					}
					
					else {
						
							String[] splitter = latestForumQuestions.get(0).split("#",2);
							if(splitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(splitter[1]);
							}
							
							splitter = latestForumQuestions.get(1).split("#",2);
							if(splitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(splitter[1]);
							}
							
							splitter = latestForumQuestions.get(2).split("#",2);
							if(splitter[0].equals(ForumController.nonceClient)) {
								finalArray.add(splitter[1]);
							}
							
						
						if(finalArray.isEmpty()) {
							
							finalArray = null;
						}
					}
				}
			}
			return finalArray;
	}

}

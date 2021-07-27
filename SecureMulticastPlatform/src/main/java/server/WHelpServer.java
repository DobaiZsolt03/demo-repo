package server;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import other.AES_ALGORITHM;
import other.ByteAndHexConversions;
import other.CriptographicHelperClass;
import other.ForumRequests;
import other.HANDSHAKE_PROTOCOL;
import other.RSA_ALGORITHM;
import other.ResetPasswordHelper;
import other.SchroederAuthenticationProtocol;


public class WHelpServer {
	
	public void sendPublicKey(Socket server) {
		RSA_ALGORITHM RSA = new RSA_ALGORITHM();
    	String pubKey = RSA.getPublicKey("RegisterLoginServer");
    	server.send(pubKey);
	}
	
	public static void main(String[] args) throws Exception
	{
		
		try (ZContext context = new ZContext()) {
			
            Socket loginSocket = context.createSocket(ZMQ.REP);
            loginSocket.bind("tcp://10.191.11.68:50");
            
            Socket authenticationSocket = context.createSocket(ZMQ.REP);
            authenticationSocket.bind("tcp://10.191.11.68:51");
            
            Socket forumRequestsSocket = context.createSocket(ZMQ.REP);
            forumRequestsSocket.bind("tcp://10.191.11.68:52");
            
            Socket publishDataSocket = context.createSocket(ZMQ.PUB);
            publishDataSocket.bind("tcp://10.191.11.68:53");
            
            Poller items = context.createPoller(3);
            items.register(loginSocket, Poller.POLLIN);
            items.register(authenticationSocket, Poller.POLLIN);
            items.register(forumRequestsSocket, Poller.POLLIN);
            
            WHelpServer rls = new WHelpServer();
    		WHelpServerCommands rlsc = new WHelpServerCommands();
    		CriptographicHelperClass CHC = new CriptographicHelperClass();
    		HANDSHAKE_PROTOCOL HP = new HANDSHAKE_PROTOCOL();
    		ResetPasswordHelper RP = new ResetPasswordHelper();
    		SchroederAuthenticationProtocol SCHAP = new SchroederAuthenticationProtocol();
    		ArrayList<ArrayList<String>> temporaryNonceValues = new ArrayList<ArrayList<String>>();
    		ArrayList<ArrayList<String>> finalSessionsList = new ArrayList<ArrayList<String>>();
    		ForumRequests FR = new ForumRequests();
    		Random random = new Random();
    		
            while (!Thread.currentThread().isInterrupted()) {
                //  poll and memorize multipart detection
                items.poll();
                
                if (items.pollin(0)) {
                	String request = loginSocket.recvStr();
                    String multipartMessageIV = null;
                    String multipartMessageSecretKey = null;
                    	
                    	 if(loginSocket.hasReceiveMore()) {
                    		 
                    		 multipartMessageIV = loginSocket.recvStr();
                    		 multipartMessageSecretKey = loginSocket.recvStr();
        					 request = CHC.decryptMultipartMessage(request, multipartMessageIV, multipartMessageSecretKey);
        					
                    	 }
                    	 
                    	 if(rlsc.helloMessageCommand(request)) {
                         	rls.sendPublicKey(loginSocket);
                         }
                         
                         else if(rlsc.registrationMessageCommand(request)) {
        					HP.finalizeRegistrationProcess(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                    	 
                         else if(rlsc.userPublicKeyMessageCommand(request)) {
                        	 SCHAP.saveUserPublicKey(request, multipartMessageIV, multipartMessageSecretKey, loginSocket);
                         }
                         
                         else if(rlsc.loginMessageCommand(request)) {
        					HP.verifyLoginData(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                         
                         else if(rlsc.finalLoginMessageCommand(request)) {
        					HP.finalizeLoginProcess(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                         
                         else if(rlsc.QuestionsMessageCommand(request)) {
        					RP.storeQuestionsData(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                    	 
                         else if(rlsc.userExistsMessageCommand(request)) {
        					RP.sendQuestions(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                    	 
                         else if(rlsc.questionsAnsweredMessageCommand(request)) {
        					RP.sendAnsweredQuestionsResult(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                    	 
                         else if(rlsc.resetPasswordMessageCommand(request)) {
        					RP.storeNewPassword(request,multipartMessageIV,multipartMessageSecretKey,loginSocket);
                         }
                }
                
	            if(items.pollin(1)) {
	            	String authRequest = authenticationSocket.recvStr();
	            	String decrypted_authRequest = CHC.privateDecryptWithServerKey(authRequest);
	            	
	            	
	            	if(rlsc.userAuthenticationRequestMessageCommand(decrypted_authRequest)) {
	            		
	            		String[] messageSplitter = decrypted_authRequest.split("#",3);
		        		String nonceClient = messageSplitter[2];
		            	int nonceServer = random.nextInt(999999 - 100000) + 100000;
		            	String conv_nonceServer = String.valueOf(nonceServer);
		            	ArrayList<String> temporaryData = new ArrayList<String>(Arrays.asList(nonceClient,conv_nonceServer));
		                temporaryNonceValues.add(temporaryData);
		                
	            		SCHAP.sendNoncesBackToUser(decrypted_authRequest, String.valueOf(conv_nonceServer), authenticationSocket);
	            	}
	            	
	            	else if(rlsc.userAuthenticationAttemptMessageCommand(decrypted_authRequest)) {
	            		SCHAP.sendSessionKeyToClient(decrypted_authRequest,temporaryNonceValues,finalSessionsList,
	            				authenticationSocket);
	            		
	            			
	            	}
	           	}
	            
	            if(items.pollin(2)) {
	            	String sessionNonces = forumRequestsSocket.recvStr();
                    String multipartMessageRequest = null;
                    String multipartMessageIV = null;
                    
                    
                    	
                    	 if(forumRequestsSocket.hasReceiveMore()) {
                    		 
                    		 multipartMessageRequest = forumRequestsSocket.recvStr();
                    		 multipartMessageIV = forumRequestsSocket.recvStr();
                    		 String dec_nonces = FR.decryptNonces(sessionNonces);
                    		 
                    		 if(rlsc.nonceMessageCommand(dec_nonces)) {
                    			 String[] messageSplitter = dec_nonces.split("#",3);
                        		 String nonceClientReceived = messageSplitter[1];
                        		 
                        		 System.out.println(multipartMessageRequest);
                    			 String sessionKey = SCHAP.getNonceSessionKey(dec_nonces, finalSessionsList);
                    			 SecretKey convKey = CHC.initializeSessionKey(sessionKey);
                    			 IvParameterSpec IV = CHC.initializeIV(multipartMessageIV);
                    			 String decrypted_request =
                    					 FR.decryptForumMessage(multipartMessageRequest, convKey, IV);
                    			 
                    			 System.out.println(decrypted_request);
                    			 
                    			 if(rlsc.createForumMessageCommand(decrypted_request)) {
                    				 FR.sendCreateForumResult(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived);
                    			 }
                    			 
                    			 else if(rlsc.isForumListRequestCommand(decrypted_request)) {
                    				 FR.sendNotJoinedForumsList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isJoinedForumListRequestCommand(decrypted_request)) {
                    				 FR.sendJoinedForumsList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isForumMembersListRequestCommand(decrypted_request)) {
                    				 FR.sendForumMembersList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isjoinForumRequestCommand(decrypted_request)) {
                    				 FR.sendJoinForumAnswer(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isPendingForumRequestCommand(decrypted_request)) {
                    				 FR.sendPendingForumREQList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isJoinForumDecisionRequestCommand(decrypted_request)) {
                    				 FR.sendJoinForumDecisionResult(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isForumInviteREQ(decrypted_request)) {
                    				 FR.sendForumInviteAnswer(decrypted_request, forumRequestsSocket, convKey, IV,nonceClientReceived,publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isforumInviteLISTREQ(decrypted_request)) {
                    				 FR.sendInvitedForumREQList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isForumKeyResendREQ(decrypted_request)) {
                    				 FR.resendForumKey(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived);
                    			 }
                    			 
                    			 else if(rlsc.isPostForumQuestionREQ(decrypted_request)) {
                    				 
                    				 String questionReceived = forumRequestsSocket.recvStr();
                    				 FR.sendForumQuestionResult(decrypted_request,
                    						 questionReceived, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isFetchForumQuestionsListREQ(decrypted_request)) {
                    				 FR.sendForumMessagesList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isForumAnswerCommand(decrypted_request)) {
                    				 
                    				 String answerReceived = forumRequestsSocket.recvStr();
                    				 FR.sendForumAnswerResult(decrypted_request, 
                    						 answerReceived, forumRequestsSocket, convKey, IV, nonceClientReceived);
                    			 }
                    			 
                    			 else if(rlsc.isForumAnswersListREQ(decrypted_request)) {
                    				 FR.sendForumAnswersList(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    			 
                    			 else if(rlsc.isLatestForumQuestionREQ(decrypted_request)) {
                    				 FR.sendLatestForumQuestions(decrypted_request, forumRequestsSocket, convKey, IV, nonceClientReceived, publishDataSocket);
                    			 }
                    		 }
                    	 }
	            	}
            	}
			}
    	}	
	}

package client;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class ClientLogin {
	private final static int    REQUEST_TIMEOUT = 1500;
    private final static int    REQUEST_RETRIES = 3;
    private ZContext ctx;

	public ClientLogin(ZContext ctx) {
    	this.ctx = ctx;
    }
	
	private void sendMultipartMessage(Socket client, String message, String multipartMessage, String multipartMessage2) {
		String request = message;
    	String requestContinued = multipartMessage;
    	String requestContinued2 = multipartMessage2;
    	client.send(request,ZMQ.SNDMORE);
    	client.send(requestContinued,ZMQ.SNDMORE);
    	client.send(requestContinued2);
	}
	
	private void sendSimpleMessage(Socket client, String message) {
		String request = message;
        client.send(request);
	}
    
    public String attemptToMsgServer(String message, String multipartMessage, String multipartMessage2) {
    	
        Socket client = ctx.createSocket(ZMQ.REQ);
        client.connect("tcp://10.191.11.68:50");

        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);

        int retriesLeft = REQUEST_RETRIES;
        boolean isMultipart = false;
      
        if(multipartMessage == null) {
        	sendSimpleMessage(client, message);
        }
        
        else {
        	isMultipart = true;
        	sendMultipartMessage(client, message, multipartMessage, multipartMessage2);
        	
        }

            boolean expectingReply = true;
            String serverResponse = null;
            
            while (expectingReply) {
                int replyTimer = poller.poll(REQUEST_TIMEOUT);
                
                if (replyTimer == -1)
                    break; //  Interrupted
                
                if (poller.pollin(0)) {
                    serverResponse = client.recvStr();
                    if (serverResponse == null)
                        break; //  Interrupted
                    
                    retriesLeft = REQUEST_RETRIES;
                    expectingReply = false;
                    //return serverResponse;
                }
                else if (--retriesLeft == 0) {
                	//return serverResponse;
                	break;
                }
                else {
                	
                	poller.unregister(client);
                    ctx.destroySocket(client);
                    client = ctx.createSocket(ZMQ.REQ);
                    client.connect("tcp://10.191.11.68:50");
                    poller.register(client, Poller.POLLIN);
                	if(isMultipart) {
                		sendMultipartMessage(client,message,multipartMessage,multipartMessage2);
                	}
                	else {
                		sendSimpleMessage(client, message);
                	}
                }
            }
            
            return serverResponse;
    	}
}

package client;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class ClientForumRequests {
	private final static int    REQUEST_TIMEOUT = 1500;
    private final static int    REQUEST_RETRIES = 3;
    private ZContext ctx;

	public ClientForumRequests(ZContext ctx) {
    	this.ctx = ctx;
    }
	
	private void sendForumRequestMessage(Socket client, String nonces, String forumRequest, String IV) {
    	client.send(nonces,ZMQ.SNDMORE);
    	client.send(forumRequest,ZMQ.SNDMORE);
    	client.send(IV);
	}
	
	private void sendForumQuestionMessage(Socket client, String nonces, String command, String forumRequest, String IV) {
		client.send(nonces,ZMQ.SNDMORE);
		client.send(command,ZMQ.SNDMORE);
		client.send(IV,ZMQ.SNDMORE);
		client.send(forumRequest);
	}
	
	public String attemptToSndQuestionToServer(String nonces, String command, String forumRequest, String IV) {
		Socket client = ctx.createSocket(ZMQ.REQ);
        client.connect("tcp://10.191.11.68:52");

        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);

        int retriesLeft = REQUEST_RETRIES;
      
        sendForumQuestionMessage(client, nonces, command, forumRequest, IV);
        
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
                    client.connect("tcp://10.191.11.68:52");
                    poller.register(client, Poller.POLLIN);
                    sendForumQuestionMessage(client, nonces, command, forumRequest, IV);
                }
            }
            
            return serverResponse;
	}
    
    public String attemptToMsgServer(String nonces, String forumRequest, String IV) {
    	
        Socket client = ctx.createSocket(ZMQ.REQ);
        client.connect("tcp://10.191.11.68:52");

        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);

        int retriesLeft = REQUEST_RETRIES;
      
        sendForumRequestMessage(client, nonces, forumRequest, IV);
        
            boolean expectingReply = true;
            String serverResponse = null;
            
            while (expectingReply) {
                int replyTimer = poller.poll(REQUEST_TIMEOUT);
                
                if (replyTimer == -1)
                    break;
                
                if (poller.pollin(0)) {
                    serverResponse = client.recvStr();
                    if (serverResponse == null)
                        break;
                    
                    retriesLeft = REQUEST_RETRIES;
                    expectingReply = false;
                }
                else if (--retriesLeft == 0) {
                	
                	break;
                }
                else {
                	
                	poller.unregister(client);
                    ctx.destroySocket(client);
                    client = ctx.createSocket(ZMQ.REQ);
                    client.connect("tcp://10.191.11.68:52");
                    poller.register(client, Poller.POLLIN);
                    sendForumRequestMessage(client, nonces, forumRequest, IV);
                }
            }
            
            return serverResponse;
    	}
}

package client;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class ClientAuthentication {
	
	private final static int    REQUEST_TIMEOUT = 1500;
    private final static int    REQUEST_RETRIES = 3;
    private ZContext ctx;

	public ClientAuthentication(ZContext ctx) {
    	this.ctx = ctx;
    }
    
    public String attemptToMsgServer(String message) {
    	
        Socket client = ctx.createSocket(ZMQ.REQ);
        client.connect("tcp://10.191.11.68:51");

        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);

        int retriesLeft = REQUEST_RETRIES;
        
        String request = message;
        client.send(request);

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
                    client.connect("tcp://10.191.11.68:51");
                    poller.register(client, Poller.POLLIN);
                    request = message;
                    client.send(request);
                }
            }
            
            return serverResponse;
    	}
}

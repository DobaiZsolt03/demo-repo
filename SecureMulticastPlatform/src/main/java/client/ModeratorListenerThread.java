package client;

import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

	public class ModeratorListenerThread implements Runnable{
		public String identity;
		private ZMQ.Socket moderator;
		
		public ModeratorListenerThread(ZMQ.Socket moderator, String identity) {
			this.moderator = moderator;
			this.identity = identity;
		}
		
		@Override
		public void run() {
			String[] messageSplitter;
			String sender;
			String publicKey;
	            
	            while (!Thread.currentThread().isInterrupted()) {
	    		
	            	ZMsg msg = ZMsg.recvMsg(moderator);
	            	if(msg.getLast().toString().startsWith("PubK#")) {
	            		messageSplitter = msg.toString().split("#",3);
	            		sender = messageSplitter[1];
	            		publicKey = messageSplitter[2];
	            		
	            		System.out.println(sender+" wants to join the forum!");
	            		System.out.println(publicKey);
	            	}
	            	
	            	else {
	            		System.out.println(msg);
	            		msg.destroy();
	            	}
	                msg.destroy();
	    	           
	            	}
				}
}

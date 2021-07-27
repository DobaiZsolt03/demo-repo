package client;

import java.util.Random;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SubscriberClientListener implements Runnable{
	
	@Override
	public void run() {
		try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(ZMQ.SUB);
            subscriber.connect("tcp://localhost:5558");
            subscriber.subscribe("Hello".getBytes(ZMQ.CHARSET));
            
            System.out.println("Connecting to the server...");
            
            
            String server_answer = subscriber.recvStr();
            
            
            System.out.println("Succesfully connected to the server!");
            
            subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
            

            while (!Thread.currentThread().isInterrupted()) {
            	subscriber.unsubscribe("Hello".getBytes(ZMQ.CHARSET));
            	
                String message = subscriber.recvStr();
                
                System.out.println(message);
            }
        }
		
	}
}

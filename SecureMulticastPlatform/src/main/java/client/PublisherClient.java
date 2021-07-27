package client;

import java.util.Random;
import java.util.Scanner;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class PublisherClient {
	public static void main(String[] args) throws Exception
    {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket publisher = context.createSocket(ZMQ.PUB);
            publisher.connect("tcp://*:5557");
            
            new Thread(new SubscriberClientListener()).start();
            
            Scanner input = new Scanner(System.in);
            
            
            while(!Thread.currentThread().isInterrupted()) {
            	String message = input.nextLine();
            	publisher.send(message);
            }
            
            input.close();
                
        }
          
    }
}

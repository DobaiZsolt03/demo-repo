package server;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMsg;

import client.ChatClients;
import other.ChatWorkers;

public class ChatBroker {

    public static void main(String[] args) throws Exception
    {
    	
    	try (ZContext ctx = new ZContext()) {
    		
            //  Frontend socket talks to clients over TCP
            ZMQ.Socket frontend = ctx.createSocket(ZMQ.ROUTER);
            frontend.bind("tcp://192.168.1.3:5570");
            
            //  Backend socket talks to workers over inproc
            ZMQ.Socket backend = ctx.createSocket(ZMQ.DEALER);
            backend.bind("inproc://backend");
            

            //  Launch pool of worker threads, precise number is not critical
            for(int i=0;i<5;i++) {
            	new Thread(new ChatWorkers(ctx)).start();
            }
                

            //  Connect backend to frontend via a proxy
            
            
            ZMQ.proxy(frontend, backend, null);
            
           
           
            
        	}
    	}
    	
    }

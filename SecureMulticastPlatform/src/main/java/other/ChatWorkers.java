package other;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import server.PublicKeyDatabase;

public class ChatWorkers implements Runnable
{
    private ZContext ctx;
    private ZMQ.Socket worker;
    private WorkerCommands wc;

    public ChatWorkers(ZContext ctx)
    {
    	try {
            File myObj = new File("Messages.txt");
            if (myObj.createNewFile())
              System.out.println("File created: " + myObj.getName());
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
    	
    	this.ctx = ctx;
    }
    
    private void setWorkerSocket(ZMQ.Socket worker) {
		 worker = ctx.createSocket(ZMQ.DEALER);
		 this.worker = worker;
	} 
    
    @Override
    public void run()
    {
    	setWorkerSocket(worker);
    	wc = new WorkerCommands(worker);
    	wc.connectWorkers();
    	
    	Poller poller = ctx.createPoller(1);
        poller.register(worker, Poller.POLLIN);
       
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg msg = ZMsg.recvMsg(worker);
            String content = msg.getLast().toString();
            System.out.println(content);
            msg.destroy();
            
            
            if(wc.isConnectionMess(content)) {
            	try {
					wc.sendMissedMessages(content);
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
            	
            }
            
            else if(wc.isKeyMess(content))
            	wc.sendPublicKey(content);
            
            else if(wc.isDirectMess(content))
            	wc.sendDirectMessage(content);
            
            else if(wc.isSecretMess(content)) {
				wc.secret_Authentication(content);
			}
            
            
            else if(wc.isModerator(content))
            	
				try {
					if(content.contains("#Conn#"))
					wc.informModerator();
					else if(content.contains("#addU#")){
						wc.moderatorDecision(content);
						
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
            
            try {
				wc.addUser(content);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        		
        
        ctx.destroy();
    }
}

package other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import server.PublicKeyDatabase;

public class WorkerCommands {
	private ZMQ.Socket worker;
	private String[] messageSplitter;
	private PublicKeyDatabase PKDatabase = new PublicKeyDatabase();
	
	public WorkerCommands(ZMQ.Socket worker) {
		this.worker = worker;
	}
	
	public void connectWorkers() {
    	worker.connect("inproc://backend");
    }
	
	public boolean isConnectionMess(String message) {
    	boolean isConn = false;
    	if(message.startsWith("Conn#")) {
    		isConn = true;
    	}
    	
    	return isConn;
    }
    
	public boolean isModerator(String message) {
    	boolean isMod = false;
        
    	if(message.startsWith("Moderator"))
    		isMod = true;
    	
    	return isMod;
    }
    
	public boolean isDirectMess(String message) {
    	boolean isDirMess = false;
    	if(message.startsWith("Mess#")){
    		isDirMess = true;
    	}
    	return isDirMess;
    }
    
	public boolean isKeyMess(String message) {
    	boolean isKeyMess = false;
    	if(message.startsWith("PubK#")) {
    		isKeyMess = true;
    	}
    	return isKeyMess;
    }
	
	public boolean isSecretMess(String message) {
    	boolean isSecretMessage = false;
    	if(message.startsWith("AuthM#")) {
    		isSecretMessage = true;
    	}
    	return isSecretMessage;
    }
	
	public void sendDirectMessage(String content) {
    	messageSplitter = content.split("#",6);
        String receiver = messageSplitter[1];
        String sender = messageSplitter[2];
        String message = messageSplitter[3];
        String IV = messageSplitter[4];
        String messageHASH = messageSplitter[5];
           
            worker.send(receiver, ZFrame.REUSE + ZFrame.MORE);
            worker.send("Mess#"+sender+"#"+message+"#"+IV+"#"+messageHASH, ZFrame.REUSE);
            
            try {
                FileWriter myWriter = new FileWriter("Messages.txt",true);
                myWriter.write("\n"+receiver+"\n");
                myWriter.write(sender+"#"+message+"#"+IV);
                myWriter.close();
                
              } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
              }
    }
    
	public void sendMissedMessages(String content) throws SQLException, IOException {
    	
    	messageSplitter = content.split("#",3);
        String receiver = messageSplitter[1];
        
    	
    	File file = new File("Messages.txt");
    	BufferedReader br = new BufferedReader(new FileReader(file)); 
    	String indexLine; 
    	  while ((indexLine = br.readLine()) != null) {
    		  if(indexLine.equals(receiver)) {
    			  worker.send(receiver, ZFrame.REUSE + ZFrame.MORE);
   		       	  worker.send("MissedM#"+br.readLine(), ZFrame.REUSE);
    		  }
    		  else if(indexLine.startsWith(receiver+":")) {
    			  worker.send(receiver, ZFrame.REUSE + ZFrame.MORE);
   		       	  worker.send("MissedM#"+indexLine, ZFrame.REUSE);
    		  }
    	  }
    	  
    	  br.close();
    	  
    	  } 
    
	public void sendPublicKey(String content) {
    	messageSplitter = content.split("#",4);
    	String moderator = messageSplitter[1];
    	String sender = messageSplitter[2];
    	String publicKey = messageSplitter[3];
    	
    	
    	try {
    		
    		String sendPublicKey = "PubK#"+sender+"#"+publicKey;
    		
    		if(PKDatabase.keyExists(sender,"pendingUsers")) {
    			System.out.println("Key found in the DB!");
    			System.out.println(PKDatabase.getPublicKey(sender,"pendingUsers"));
    			worker.send(moderator, ZFrame.REUSE + ZFrame.MORE);
    	    	worker.send(sendPublicKey,
    	    			ZFrame.REUSE);
    		}
    		
    		else if(!PKDatabase.keyExists(sender, "acceptedUsers")){
    			System.out.println("Key added in the DB!");
    			PKDatabase.addPublicKey(sender, publicKey,"pendingUsers");
    			worker.send(moderator, ZFrame.REUSE + ZFrame.MORE);
    	    	worker.send(sendPublicKey,
    	    			ZFrame.REUSE);
    		}
    		
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
    }
    
	public void informModerator() throws SQLException {
    	if(!PKDatabase.displayUsersFromTable("pendingUsers").isEmpty()) {
		    
    		worker.send("Moderator", ZFrame.REUSE + ZFrame.MORE);
		    worker.send(PKDatabase.displayUsersFromTable("pendingUsers").toString(), ZFrame.REUSE);
    	}
    }
    
	public void moderatorDecision(String message) throws SQLException {
    	messageSplitter = message.split("#",4);
    	String decision = messageSplitter[2];
    	String userToAdd = messageSplitter[3];
    	
        if(decision.equals("n")||decision.equals("N")) {
        	
        	System.out.println("Nothing to do!");
        }
        
        else if(decision.equals("y")||decision.equals("Y")){      	
        	worker.send(userToAdd, ZFrame.REUSE + ZFrame.MORE);
        	worker.send("ACC_GRA#You have been accepted in the forum!",ZFrame.REUSE);
        }
    }
    
	public void secret_Authentication(String message) {
    	messageSplitter = message.split("#",4);
    	String receiver = messageSplitter[1];
    	String sender = messageSplitter[2];
    	String enc_message = messageSplitter[3];
    	
    	
    	
    	worker.send(receiver, ZFrame.REUSE + ZFrame.MORE);
    	worker.send("AuthM#"+sender+"#"+enc_message,ZFrame.REUSE);
    	
    }
	
	public void addUser(String message) throws SQLException {
		if(message.startsWith("FinalizeAddU#")) {
			messageSplitter = message.split("#",4);
	    	String user = messageSplitter[1];
	    	String symmetricKey = messageSplitter[2];
			
			PKDatabase.addPublicKey(user, PKDatabase.getPublicKey(user, "pendingUsers")
	    			, "acceptedUsers");
	    	
	    	PKDatabase.DeleteUser(user, "pendingUsers");
	    	
	    	worker.send(user, ZFrame.REUSE + ZFrame.MORE);
	    	worker.send("Accepted#"+symmetricKey,ZFrame.REUSE);
	    	
	    	System.out.println("IT WORKED!!!!");
		}
	}
	
}

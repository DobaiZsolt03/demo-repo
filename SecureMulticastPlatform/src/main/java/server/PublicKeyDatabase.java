package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PublicKeyDatabase {
	
	private static Connection conn;
	private boolean hasData = false;
	
	public String getPublicKey(String username, String table) throws SQLException {
		
		String publicKey;
		
		if(conn == null) {
			getConnection(table);
		}
		
		Statement stmnt = conn.createStatement(); 
		ResultSet res = stmnt.executeQuery("SELECT publicKey FROM "+table+" WHERE "
				+ "Username='"+username+"'");
		
		publicKey = res.getString("publicKey");
		
		return publicKey;
	}
	
	public void getConnection(String table) throws SQLException {
		conn = DriverManager.getConnection("jdbc:sqlite:PublicKeyDatabase.db");
		
		initializeTable(table);
	}
	
	public List<String> displayUsersFromTable(String table) throws SQLException {
		List<String> requests = new ArrayList<String>();
		if(conn == null) {
			getConnection(table);
		}
		
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Username FROM "+table);
		
		while(res.next()) {
			requests.add(res.getString("Username"));
		}
		
		return requests;
		
	}
	

	private void initializeTable(String table) throws SQLException {
		
		if(!hasData) {
			hasData = true;
			
			Statement stmnt = conn.createStatement();
			ResultSet res = stmnt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND "
					+ "name='"+table+"'");
			
			if(!res.next()) {
				System.out.println("Building the "+table+" table...");
				
				stmnt.execute("CREATE TABLE "+table+"(id integer,"+ "Username varchar(60),"
				+"publicKey varchar(60),"+"primary key(id));");
			}
		}
	}
	
	
	public void addPublicKey(String username, String publicKey, String table) throws SQLException{
		
		if(conn==null) {
			getConnection(table);
		}
		
		PreparedStatement prep = conn.prepareStatement("INSERT INTO "+table+" values(?,?,?);");
		prep.setString(2, username);
		prep.setString(3, publicKey);
		prep.execute();
	}
	
	public boolean keyExists(String username, String table) throws SQLException {
		boolean isThere = false;
		
		if(conn==null) {
			getConnection(table);
		}
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT publicKey FROM "+table+" WHERE Username='"+username+"'");
		
		if(res.next()) {
			isThere = true;
		}
		else {
			isThere = false;
		}
		
		return isThere;
		
	}
	
	public void CloseConnection() throws SQLException {
		if(conn!=null) {
			conn.close();
			conn=null;
		}
	}
	
	public void DeleteUser(String username, String table) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		PreparedStatement prep = conn.prepareStatement("DELETE FROM "+table+" "
				+ "WHERE Username IN ('"+username+"')");
		prep.execute();
	}
	
	public static void main(String[] args){
		
		
		
		PublicKeyDatabase pkd = new PublicKeyDatabase();
		try {
			System.out.println(pkd.displayUsersFromTable("acceptedUsers"));
			//pkd.CloseConnection();
			//System.out.println(pkd.displayPendingUsers());
			//pkd.DeleteUser("Janna","acceptedUsers");
			
				
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

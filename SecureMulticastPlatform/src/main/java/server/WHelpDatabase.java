package server;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import javafx.scene.control.Button;
import other.AES_ALGORITHM;
import other.ByteAndHexConversions;
import other.RSA_ALGORITHM;

public class WHelpDatabase {
	private Connection conn;
	private boolean hasData = false;
	
	public void getConnection(String table) throws SQLException {
		conn = DriverManager.getConnection("jdbc:sqlite:WHelpDatabase.db");
		
		initializeTable(table);
	}
	
	public int getAuthenticationsLeft(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		int authleft=0;
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT AuthenticationsLeft FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		authleft = res.getInt("AuthenticationsLeft");
		
		CloseConnection();
		
		return authleft;
	}
	
	public String getSalt(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		String salt="";
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT PasswordSalt FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		salt = res.getString("PasswordSalt");
		
		CloseConnection();
		
		return salt;
	}
	
	public String getPassword(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		String password="";
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Password FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		password = res.getString("Password");
		
		CloseConnection();
		
		return password;
	}
	
	public void updateAuthenticationNR(String table, String username, int currentAuthNR) throws SQLException {
		if(conn==null) {
			getConnection(table);
		}
		
			int finalAuthCount = currentAuthNR-1;
			if(finalAuthCount < 1)
				finalAuthCount = 1000;
			
			int ID;
			Statement stmnt = conn.createStatement();
			ResultSet res = stmnt.executeQuery("SELECT ID FROM "+
					table+" WHERE "+ "Username='"+username+"'");
			ID = res.getInt("ID");
		
			PreparedStatement prep = conn.prepareStatement("UPDATE "+table+" SET AuthenticationsLeft=? "
					+ "WHERE ID = "+ID);
			prep.setInt(1, finalAuthCount);
			prep.execute();
			
			CloseConnection();
	}
	
	private void initializeTable(String table) throws SQLException {
		
		if(!hasData) {
			hasData = true;
			
			Statement stmnt = conn.createStatement();
			ResultSet res = stmnt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND "
					+ "name='"+table+"'");
			
			if(!res.next()) {
				System.out.println("Building the "+table+" table...");
				
				if(table.equals("Users")) {
					stmnt.execute("CREATE TABLE "+table+"(ID integer,"+ "FirstName varchar(20),"+"LastName varchar(20),"
							+ "Username varchar(20),"+"Password varchar(80),"+"PasswordSalt varchar(80),"
								+"AuthenticationsLeft integer,"+"primary key(ID));");
				}
				
				else if(table.equals("Questions")){
					stmnt.execute("CREATE TABLE "+table+"(ID integer,"+ "Username varchar(20),"+"Question1 varchar(30),"
							+"Answer1 varchar(30),"+ "Question2 varchar(30),"+"Answer2 varchar(30),"
								+"Question3 varchar(30),"+"Answer3 varchar(30),"+"AnswersSalt varchar(20),"
										+ "ResetsLeft integer,"+"primary key(ID));");
				}
				
				else if(table.equals("PublicKeysTable")) {
					stmnt.execute("CREATE TABLE "+table+"(ID integer,"+ "Username varchar(20),"
							+"PublicKey varchar(255),"+ "primary key(ID));");
				}
				
				else if(table.equals("ForumsListTable")) {
					stmnt.execute("CREATE TABLE "+table+"(ID integer,"+ "ForumName varchar(40),"
							+"ModeratorName varchar(255),"+ "Description varchar(200),"
							+ "nbr_of_Members integer, ForumKey varchar(255), primary key(ID));");
				}
			}
		}
	}
	
	public boolean userExists(String username, String table) throws SQLException {
		boolean exists = false;
		
		if(conn==null) {
			getConnection(table);
		}
		
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Username FROM "+table+" WHERE "+ "Username='"+username+"'");
		
		if(res.next()) {
			exists = true;
		}
		
		CloseConnection();
		
		return exists;
	}
	
	public boolean pubKeyExists(String pubKey) throws SQLException {
		boolean exists = false;
		
		if(conn==null) {
			getConnection("PublicKeysTable");
		}
		
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT * FROM PublicKeysTable WHERE PublicKey='"+pubKey+"'");
		
		if(res.next()) {
			exists = true;
		}
		
		CloseConnection();
		
		return exists;
	}
	
	public void saveUserPublicKey(String username, String publicKey) throws SQLException {
		if(conn==null) {
			getConnection("PublicKeysTable");
		}
		
		PreparedStatement prep = conn.prepareStatement("INSERT INTO PublicKeysTable values(?,?,?);");
		prep.setString(2, username);
		prep.setString(3, publicKey);
		prep.execute();
		
		CloseConnection();
	}
	
	public void updateUserPublicKey(String username, String publicKey) throws SQLException {
		if(conn==null) {
			getConnection("PublicKeysTable");
		}
		
		PreparedStatement prep = conn.prepareStatement("UPDATE PublicKeysTable " +"SET PublicKey ='"+publicKey+"' "
				+ "WHERE Username='"+username+"'");
		prep.executeUpdate();
		
		CloseConnection();
	}
	
	public String getUserPublicKey(String username) throws SQLException {
		if(conn == null) {
			getConnection("PublicKeysTable");
		}
		
		String publicKey;
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT PublicKey FROM PublicKeysTable WHERE Username='"+username+"'");
		
		publicKey = res.getString("PublicKey");
		CloseConnection();
		
		return publicKey;
	}
	
	public String getForumKey(String forumName) throws SQLException {
		if(conn == null) {
			getConnection("ForumsListTable");
		}
		
		String simmetricKey;
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT ForumKey FROM ForumsListTable WHERE Forumname ='"+forumName+"'");
		
		simmetricKey = res.getString("ForumKey");
		CloseConnection();
		
		return simmetricKey;
	}
	
	public String[] getQuestions(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		String[] questions = new String[3];
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Question1,Question2,Question3 FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		questions[0] = res.getString("Question1");
		questions[1] = res.getString("Question2");
		questions[2] = res.getString("Question3");
		
		CloseConnection();
		
		return questions;
	}
	
	public int getResetsLeft(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		int resets;
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT ResetsLeft FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		resets = res.getInt("ResetsLeft");
		
		CloseConnection();
		
		return resets;
	}
	
	public String[] getAnswers(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		String[] answers = new String[3];
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Answer1,Answer2,Answer3 FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		answers[0] = res.getString("Answer1");
		answers[1] = res.getString("Answer2");
		answers[2] = res.getString("Answer3");
		
		CloseConnection();
		
		return answers;
	}
	
	public String getAnswersSalt(String table, String username) throws SQLException {
		if(conn == null) {
			getConnection(table);
		}
		
		String salt;
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT AnswersSalt FROM "+
				table+" WHERE "+ "Username='"+username+"'");
		
		salt = res.getString("AnswersSalt");
		
		CloseConnection();
		
		return salt;
	}
	
	public void updateResetsLeftNumber(String table, String username, int currentResetsNbr) throws SQLException {
		if(conn==null) {
			getConnection(table);
		}
		
			int finalResetNbr = currentResetsNbr-1;
			
			int ID;
			Statement stmnt = conn.createStatement();
			ResultSet res = stmnt.executeQuery("SELECT ID FROM "+
					table+" WHERE "+ "Username='"+username+"'");
			ID = res.getInt("ID");
		
			PreparedStatement prep = conn.prepareStatement("UPDATE "+table+" SET ResetsLeft=? "
					+ "WHERE ID = "+ID);
			prep.setInt(1, finalResetNbr);
			prep.execute();
			
			CloseConnection();
	}
	
	public void updatePassword(String table, String username, String currentPassword, String newPassword) throws SQLException {
		if(conn==null) {
			getConnection(table);
		}
		
			
			int ID;
			Statement stmnt = conn.createStatement();
			ResultSet res = stmnt.executeQuery("SELECT ID FROM "+
					table+" WHERE "+ "Username='"+username+"'");
			ID = res.getInt("ID");
		
			PreparedStatement prep = conn.prepareStatement("UPDATE "+table+" SET Password=? "
					+ "WHERE ID = "+ID);
			prep.setString(1, newPassword);
			prep.execute();
			
			CloseConnection();
	}
	
	
	public void addUser(String firstName, String lastName, String username, String password, String passwordsalt,
			int authentications_left, String table) throws SQLException{
		
		if(conn==null) {
			getConnection(table);
		}
		
			PreparedStatement prep = conn.prepareStatement("INSERT INTO "+table+" values(?,?,?,?,?,?,?);");
			prep.setString(2, firstName);
			prep.setString(3, lastName);
			prep.setString(4, username);
			prep.setString(5, password);
			prep.setString(6, passwordsalt);
			prep.setInt(7, authentications_left);
			prep.execute();
			
			CloseConnection();
	}
	
	public void addUserQuestions(String Username, String question1, String answer1, String question2, String answer2,
			String question3, String answer3, String answersSalt, int resetsLeft, String table) throws SQLException {
		
		if(conn==null) {
			getConnection(table);
		}
		
			PreparedStatement prep = conn.prepareStatement("INSERT INTO "+table+" values(?,?,?,?,?,?,?,?,?,?);");
			prep.setString(2, Username);
			prep.setString(3, question1);
			prep.setString(4, answer1);
			prep.setString(5, question2);
			prep.setString(6, answer2);
			prep.setString(7, question3);
			prep.setString(8, answer3);
			prep.setString(9, answersSalt);
			prep.setInt(10, resetsLeft);
			prep.execute();
			
			CloseConnection();
	}
	
	
	public void CloseConnection() throws SQLException {
		if(conn!=null) {
			conn.close();
			conn=null;
		}
	}
	
	public boolean forumCreationSuccessful(String forumName, String moderatorName, String description, String forumKey) throws SQLException {
		
		boolean isSuccess;
		if(conn==null) {
			getConnection(forumName);
			
		}
		
		String originalForumName = forumName;
		
		forumName = forumName.replaceAll("\\s", "");
		
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND "
				+ "name='"+forumName+"'");
		
		if(!res.next()) {
			stmnt.execute("CREATE TABLE pending_"+forumName+"(ID integer, Username varchar(20), Status varchar(255) "
					+ ",primary key(ID));");
		
			stmnt.execute("CREATE TABLE "+forumName+"(ID integer,"+ "MemberUName varchar(20),"
					+"MemberPosition varchar(20)," +"primary key(ID));");
			
			PreparedStatement prep = conn.prepareStatement("INSERT INTO "+forumName+" values(?,?,?);");
			prep.setString(2, moderatorName);
			prep.setString(3, "Moderator");
			prep.execute();
			
			PreparedStatement prep2 = conn.prepareStatement("INSERT INTO ForumsListTable values(?,?,?,?,?,?);");
			prep2.setString(2, originalForumName);
			prep2.setString(3, moderatorName);
			prep2.setString(4, description);
			prep2.setInt(5, 1);
			prep2.setString(6, forumKey);
			prep2.execute();
			
			isSuccess = true;
		}
		
		else {
			isSuccess = false;
		}
		
			CloseConnection();
			
			return isSuccess;
	}
	
	private ArrayList<String> getAllIndividualForums() throws SQLException{
		if(conn == null) {
			getConnection("ForumsListTable");
		}
		
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND "
				+ "name LIKE 'pending_%'");
		
		ArrayList<String> pendingList = new ArrayList<String>();
		while(res.next()) {
			String forumName = res.getString("name").substring(8);
			pendingList.add(forumName);
		}
		
		CloseConnection();
		
		return pendingList;
	}
	
	private ArrayList<String> getAllPendingForumsList() throws SQLException{
		if(conn == null) {
			getConnection("ForumsListTable");
		}
		
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND "
				+ "name LIKE 'pending_%'");
		
		ArrayList<String> pendingList = new ArrayList<String>();
		while(res.next()) {
			String forumName = res.getString("name");
			pendingList.add(forumName);
		}
		
		CloseConnection();
		
		return pendingList;
	}
	
	public ArrayList<String> getAllJoinedForums(String username) throws SQLException{
		ArrayList<String> allForums = getAllIndividualForums();
		
		if(conn == null) {
			getConnection("ForumsListTable");
		}
		
		Statement stmnt = conn.createStatement();
		
		ArrayList<String> joinedForums = new ArrayList<String>();
		for(int i=0;i<allForums.size();i++) {
			ResultSet res = stmnt.executeQuery("SELECT MemberUName FROM "+allForums.get(i)+" WHERE MemberUName ="
					+ "'"+username+"'");
			if(res.next()) {
				
				ResultSet result = stmnt.executeQuery("SELECT ForumName FROM ForumsListTable WHERE "
						+ "replace(ForumName, ' ', '') LIKE '%"+allForums.get(i)+"'");
				if(result.next()) {
					joinedForums.add(result.getString("ForumName"));
				}
			}
		}
		
		CloseConnection();
		
		return joinedForums;
	}
	
	public ArrayList<String> getAllForumInvites(String username) throws SQLException{
		ArrayList<String> allForums = getAllPendingForumsList();
		
		if(conn == null) {
			getConnection("ForumsListTable");
		}
		
		Statement stmnt = conn.createStatement();
		
		ArrayList<String> invitedForums = new ArrayList<String>();
		for(int i=0;i<allForums.size();i++) {
			ResultSet res = stmnt.executeQuery("SELECT Username FROM "+allForums.get(i)+" WHERE Username ="
					+ "'"+username+"' AND Status ='Invited'");
			if(res.next()) {
				
				String forumName = allForums.get(i).substring(8);
				
				ResultSet result = stmnt.executeQuery("SELECT ForumName FROM ForumsListTable WHERE "
						+ "replace(ForumName, ' ', '') LIKE '%"+forumName+"'");
				if(result.next()) {
					invitedForums.add(result.getString("ForumName"));
				}
			}
		}
		
		CloseConnection();
		
		return invitedForums;
	}
	
	public ArrayList<ArrayList<Object>> getAllNotJoinedForums(String username) throws SQLException{
		
		ArrayList<String> allForums = getAllIndividualForums();
		
		if(conn == null) {
			getConnection("ForumsListTable");
		}
		
		Statement stmnt = conn.createStatement();
		
		ArrayList<String> notJoinedForums = new ArrayList<String>();
		for(int i=0;i<allForums.size();i++) {
			ResultSet res = stmnt.executeQuery("SELECT MemberUName FROM "+allForums.get(i)+" WHERE MemberUName LIKE"
					+ "'%"+username+"%'");
			if(!res.next()) {
				notJoinedForums.add(allForums.get(i));
			}
		}
		
		ArrayList<ArrayList<Object>> forumsList = new ArrayList<ArrayList<Object>>();
		Statement insertStmnt = conn.createStatement();
		for(int i=0;i<notJoinedForums.size();i++) {
			ResultSet res = insertStmnt.executeQuery("SELECT ForumName,ModeratorName,Description,nbr_of_Members"
					+ " FROM ForumsListTable WHERE replace(ForumName, ' ', '') LIKE '%"+notJoinedForums.get(i)+"'");
			
			if(res.next()) {
				
				ArrayList<Object> forumEntry = new ArrayList<Object>(Arrays.asList(res.getString("ForumName"),
						res.getString("ModeratorName"),res.getString("Description"),res.getInt("nbr_of_Members")));
		        forumsList.add(forumEntry);
			}
		}
		
		CloseConnection();
		
		return forumsList;
	}
	
	public ArrayList<ArrayList<String>> getAllForumMembers(String forumName) throws SQLException{
		
		ArrayList<ArrayList<String>> forumEntry = new ArrayList<ArrayList<String>>();
		
		if(conn == null) {
			getConnection(forumName);
		}
		
		Statement stmnt = conn.createStatement();
		
		forumName = forumName.replaceAll("\\s", "");
		
			ResultSet res = stmnt.executeQuery("SELECT MemberUName,MemberPosition FROM "+forumName);
			while(res.next()) {
				
				ArrayList<String> forumMember = new ArrayList<String>();
				forumMember.add(res.getString("MemberUName"));
				forumMember.add(res.getString("MemberPosition"));
				forumEntry.add(forumMember);
			}
			
			
		
		CloseConnection();
		
		return forumEntry;
	}
	
	public boolean pendingUserExists(String username, String forumName) throws SQLException {
		
		boolean exists = false;
		
		if(conn == null) {
			getConnection("pending_"+forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Username FROM pending_"+forumName+" WHERE Username ='"+username+"'");
		
		if(res.next()) {
			exists = true;
		}
		
		CloseConnection();
		
		return exists;
		
	}
	
	public boolean forumUserExists(String username, String forumName) throws SQLException {
		boolean exists = false;
		
		if(conn == null) {
			getConnection(forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT MemberUName FROM "+forumName+" WHERE MemberUName ='"+username+"'");
		
		if(res.next()) {
			exists = true;
		}
		
		CloseConnection();
		
		return exists;
	}
	
	public ArrayList<String> getAllPendingForumUsers(String username, String forumName) throws SQLException{
		
		if(conn == null) {
			getConnection("pending_"+forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT Username FROM pending_"+forumName+" WHERE Status ='Pending'");
		
		ArrayList<String> pendingList = new ArrayList<String>();
		while(res.next()) {
			pendingList.add(res.getString("Username"));
		}
		
		CloseConnection();
		
		return pendingList;
	}
	
	public void addPendingUserToForum(String username, String forumName) throws SQLException {
		
		if(conn == null) {
			getConnection("pending_"+forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		
		if(!pendingUserExists(username, forumName)) {
			
			if(conn == null) {
				getConnection("pending_"+forumName);
			}
			
			PreparedStatement prep = conn.prepareStatement("INSERT INTO pending_"+forumName+" values(?,?,?);");
			prep.setString(2, username);
			prep.setString(3, "Pending");
			prep.execute();
		}
		
		CloseConnection();
	}
	
	public void addInvitedUserToForum(String username, String forumName) throws SQLException {
		if(conn == null) {
			getConnection("pending_"+forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		
		if(!pendingUserExists(username, forumName) && !forumUserExists(username, forumName)) {
			
			if(conn == null) {
				getConnection("pending_"+forumName);
			}
			
			PreparedStatement prep = conn.prepareStatement("INSERT INTO pending_"+forumName+" values(?,?,?);");
			prep.setString(2, username);
			prep.setString(3, "Invited");
			prep.execute();
		}
		
		CloseConnection();
	}
	
	public void updatePendingUserToForum(String username, String forumName, String decision) throws SQLException {
		if(conn == null) {
			getConnection("pending_"+forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		
		if(decision.equals("Accept")) {
			decision = "Accepted";
			
			addNewUserToForum(forumName, username);
		}
		
		else if(decision.equals("Decline")){
			decision = "Declined";
		}
		
		if(conn == null) {
			getConnection("pending_"+forumName);
		}
		
		PreparedStatement prep = conn.prepareStatement("UPDATE pending_"+forumName+" SET Status=? "
				+ "WHERE Username ='"+username+"'");
		prep.setString(1, decision);
		prep.execute();
		
		CloseConnection();
	}
	
	public void addNewUserToForum(String forumName, String userName) throws SQLException {
		if(conn == null) {
			getConnection(forumName);
		}
			
		if(!forumUserExists(userName, forumName)) {
			
			if(conn == null) {
				getConnection(forumName);
			}
			
			PreparedStatement prep = conn.prepareStatement("INSERT INTO "+forumName+" values(?,?,?);");
			prep.setString(2, userName);
			prep.setString(3, "Member");
			prep.execute();
			
		}
		
		if(conn == null) {
			getConnection(forumName);
			
		}
		
		int count = getMemberCount(forumName);
		
		PreparedStatement prep2 = conn.prepareStatement("UPDATE ForumsListTable SET nbr_of_Members=? "
				+ "WHERE replace(ForumName, ' ', '') ='"+forumName+"'");
		prep2.setInt(1, count);
		prep2.execute();
		
		CloseConnection();
	}
	
	private int getMemberCount(String forumName) throws SQLException {
		if(conn == null) {
			getConnection(forumName);
		}
		
		forumName = forumName.replaceAll("\\s", "");
		Statement stmnt = conn.createStatement();
		ResultSet res = stmnt.executeQuery("SELECT ID FROM "+forumName+" WHERE ID ="
				+ " (SELECT MAX(ID) FROM "+forumName+")");
		
		int forumCounter=0;
		if(res.next()) {
			forumCounter = res.getInt("ID");
		}
		
		return forumCounter;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, SQLException{
		WHelpDatabase wd = new WHelpDatabase();
		wd.addNewUserToForum("TheTrojanWar", "Achilles");
	}
}

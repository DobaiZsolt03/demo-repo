package server;

public class WHelpServerCommands {
	private String[] messageSplitter;

	public boolean helloMessageCommand(String message) {
		
		boolean isHello = false;
		if(message.equals("Hello")) {
			isHello = true;
		}
		
		return isHello;
	}
	
	public boolean registrationMessageCommand(String message) {
		boolean isRegistration = false;
		messageSplitter = message.split("#",6);
		if(messageSplitter[0].equals("REG")) {
			isRegistration = true;
		}
		
		return isRegistration;
	}
	
	public boolean loginMessageCommand(String message) {
		boolean isLogin = false;
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("LOG")) {
			isLogin = true;
		}
		
		return isLogin;
	}
	
	public boolean finalLoginMessageCommand(String message) {
		boolean isFinalLogin = false;
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("FINLOG")) {
			isFinalLogin = true;
		}
		
		return isFinalLogin;
	}
	
	public boolean QuestionsMessageCommand(String message) {
		boolean isQuestions = false;
		messageSplitter = message.split("#",9);
		if(messageSplitter[0].equals("QUES")) {
			isQuestions = true;
		}
		
		return isQuestions;
	}
	
	public boolean userExistsMessageCommand(String message) {
		boolean isUserMessage = false;
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("UMESS")) {
			isUserMessage = true;
		}
		
		return isUserMessage;
	}
	
	public boolean questionsAnsweredMessageCommand(String message) {
		boolean isQuestionsAnswered = false;
		messageSplitter = message.split("#",6);
		if(messageSplitter[0].equals("AQUES")) {
			isQuestionsAnswered = true;
		}
		
		return isQuestionsAnswered;
	}
	
	public boolean resetPasswordMessageCommand(String message) {
		boolean isresetPassword = false;
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("RPASS")) {
			isresetPassword = true;
		}
		
		return isresetPassword;
	}
	
	public boolean userPublicKeyMessageCommand(String message) {
		boolean isuserPUBKEYMessage = false;
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("SAVEUPUBK")) {
			isuserPUBKEYMessage = true;
		}
		
		return isuserPUBKEYMessage;
	}
	
	public boolean userAuthenticationRequestMessageCommand(String message) {
		boolean isUserAuth = false;
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("REQAUTH")) {
			isUserAuth = true;
		}
		
		return isUserAuth;
	}
	
	public boolean userAuthenticationAttemptMessageCommand(String message) {
		boolean isUserAuthAttempt = false;
		
		messageSplitter = message.split("#",2);
		if(messageSplitter[0].equals("AUTHWITHSERVER")) {
			isUserAuthAttempt = true;
		}
		
		return isUserAuthAttempt;
	}
	
	public boolean nonceMessageCommand(String message) {
		boolean isNonceMessage = false;
		
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("FORUMREQ")) {
			isNonceMessage = true;
		}
		
		return isNonceMessage;
	}
	
	public boolean createForumMessageCommand(String message) {
		boolean isCreateForumMessage = false;
		
		messageSplitter = message.split("#",6);
		if(messageSplitter[0].equals("CREATEFORUM")) {
			isCreateForumMessage = true;
		}
		
		return isCreateForumMessage;
	}
	
	public boolean isForumListRequestCommand(String message) {
		boolean isForumListMessage = false;
		
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("GETFORUMLIST")) {
			isForumListMessage = true;
		}
		
		return isForumListMessage;
	}
	
	public boolean isJoinedForumListRequestCommand(String message) {
		boolean isJoinedForumListMessage = false;
		
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("GETJOINEDFORUMSLIST")) {
			isJoinedForumListMessage = true;
		}
		
		return isJoinedForumListMessage;
	}
	
	public boolean isForumMembersListRequestCommand(String message) {
		boolean isJoinedForumListMessage = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("GETFORUMMEMBERSLIST")) {
			isJoinedForumListMessage = true;
		}
		
		return isJoinedForumListMessage;
	}
	
	public boolean isjoinForumRequestCommand(String message) {
		boolean isJoinForumREQ = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("JOINFORUMREQ")) {
			isJoinForumREQ = true;
		}
		
		return isJoinForumREQ;
	}
	
	public boolean isPendingForumRequestCommand(String message) {
		boolean isPendindForumREQ = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("PENDINGFORUMREQ")) {
			isPendindForumREQ = true;
		}
		
		return isPendindForumREQ;
	}
	
	public boolean isJoinForumDecisionRequestCommand(String message) {
		boolean isForumDecision = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("JOINFORUMDECISION")) {
			isForumDecision = true;
		}
		
		return isForumDecision;
	}
	
	public boolean isForumInviteREQ(String message) {
		boolean isForumInvite = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("FORUMINVITE")) {
			isForumInvite = true;
		}
		
		return isForumInvite;
	}
	
	public boolean isforumInviteLISTREQ(String message) {
		boolean isForumInviteListREQ = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("INVITEDFORUMREQ")) {
			isForumInviteListREQ = true;
		}
		
		return isForumInviteListREQ;
	}
	
	public boolean isForumKeyResendREQ(String message) {
		boolean isresendKey = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("FORUMKEYREQ")) {
			isresendKey = true;
		}
		
		return isresendKey;
	}
	
	public boolean isPostForumQuestionREQ(String message) {
		boolean isPostQuestion = false;
		
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("FORUMQUESTION")) {
			isPostQuestion = true;
		}
		
		return isPostQuestion;
	}
	
	public boolean isFetchForumQuestionsListREQ(String message) {
		boolean isFetchList = false;
		
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("FORUMQUESTIONLISTREQ")) {
			isFetchList = true;
		}
		
		return isFetchList;
	}
	
	public boolean isForumAnswerCommand(String message) {
		boolean isForumAnswer = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("FORUMANSWER")) {
			isForumAnswer = true;
		}
		return isForumAnswer;
	}
	
	public boolean isForumAnswersListREQ(String message) {
		boolean isForumAnswersListREQ = false;
		
		messageSplitter = message.split("#",4);
		if(messageSplitter[0].equals("FORUMANSWERSLISTREQ")) {
			isForumAnswersListREQ = true;
		}
		return isForumAnswersListREQ;
	}
	
	public boolean isLatestForumQuestionREQ(String message) {
		boolean isLatestForumREQ = false;
		
		messageSplitter = message.split("#",3);
		if(messageSplitter[0].equals("LATESTFORUMQUESTIONSREQ")) {
			isLatestForumREQ = true;
		}
		return isLatestForumREQ;
	}
}

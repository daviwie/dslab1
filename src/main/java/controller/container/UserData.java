package controller.container;

public class UserData {
	
	private String userName;
	private String password;
	private long credits;
	private boolean online;
	
	public UserData() {
		
	}
	
	/**
	 * Creates a new user to be stored in UserConcurrentHashMap
	 * 
	 * @param userName The username used to log into the system
	 * @param password The password used to log into the system
	 * @param credits The credits necessary to compute formulas
	 */
	public UserData(String userName, String password, Integer credits) {
		this.userName = userName;
		this.password = password;
		this.credits = credits;
	}

	// TODO if(bored) fillInJavadocs else fuckIt
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String name) {
		this.userName = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getCredits() {
		return credits;
	}

	public void setCredits(long credits) {
		this.credits = credits;
	}
	
	public void decCredits(long credits) {
		this.credits -= credits;
	}
	
	public void buyCredits(long credits) {
		this.credits += credits;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public String stringOnline() {
		if(isOnline())
			return "online";
		else 
			return "offline";
	}
}

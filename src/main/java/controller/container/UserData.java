package controller.container;

/**
 * Encapsulates all of the data relevant about a specific user (Client) necessary for the CloudController. 
 *
 */
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

	/**
	 * @return Returns the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name
	 * 
	 * @param name
	 */
	public void setUserName(String name) {
		this.userName = name;
	}

	/**
	 * @return the user's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the user password
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the user's credits
	 */
	public long getCredits() {
		return credits;
	}

	/**
	 * Sets the user's credits
	 * 
	 * @param credits
	 */
	public void setCredits(long credits) {
		this.credits = credits;
	}

	/**
	 * Decrements a user's credits
	 * 
	 * @param credits the number of credits to be subtracted from a user
	 */
	public void decCredits(long credits) {
		this.credits -= credits;
	}

	/**
	 * Adds credits to a user's account
	 * 
	 * @param credits the number of credits to be added to the user
	 */
	public void buyCredits(long credits) {
		this.credits += credits;
	}

	/**
	 * @return true if online, else false
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * Sets a user's online status
	 * 
	 * @param online
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * Provides a string representation of a user's online status
	 * 
	 * @return "online" if a user is online, else "offline"
	 */
	public String stringOnline() {
		if (isOnline())
			return "online";
		else
			return "offline";
	}
}

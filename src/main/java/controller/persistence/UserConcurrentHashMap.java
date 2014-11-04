package controller.persistence;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class UserConcurrentHashMap extends ConcurrentHashMap<String, UserData> {
	/**
	 * Login a certain user. 
	 * 
	 * @param name the username of the client
	 * @param password the password to match the username
	 * @return A string signifying a successful or a failed login
	 */
	public synchronized String login(String name, String password) {
		UserData user = get(name);

		/*
		 * In the event that login fails due to invalid username or password,
		 * the client should not be notified which of the inputs is false so as
		 * to increase the security of the system.
		 */
		if (user == null)
			return "Wrong username or password.";
		else if (!user.getPassword().equals(password))
			return "Wrong username or password.";
		else if (user.isOnline())
			return "You are already logged in!";
		else {
			user.setOnline(true);
			return "Successfully logged in.";
		}
	}
}

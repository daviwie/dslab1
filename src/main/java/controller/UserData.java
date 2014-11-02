package controller;

public class UserData {
	
	private String name;
	private String password;
	private Integer credits;
	private boolean online;
	
	public UserData() {
		
	}
	
	public UserData(String name, String password, Integer credits) {
		this.name = name;
		this.password = password;
		this.credits = credits;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getCredits() {
		return credits;
	}

	public void setCredits(Integer credits) {
		this.credits = credits;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}

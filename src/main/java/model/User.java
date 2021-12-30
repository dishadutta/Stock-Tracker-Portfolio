package main.java.model;
import main.java.utility.SecurityUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class User implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private int alertTime;
	private int stockUpdateTime;
	private String phoneNumber;
	private Date birthday;
	private Account account;
	private Set<UserStock> userStocks = new HashSet<UserStock>(0);
	
	final int DEFAULT_STOCK_UPDATE_TIME = 2;
	final int DEFAULT_ALERT_TIME = 30;

	public User() {
	}

	public User(String username, String password, String firstName, String lastName, String email, String phoneNumber, Date birthday) {
		this.username = username;
		this.setHashedPassword(password);
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		setPhoneNumber(phoneNumber);
		this.birthday = birthday;
		alertTime = DEFAULT_ALERT_TIME;
		stockUpdateTime = DEFAULT_STOCK_UPDATE_TIME;
	}

	public User(String username, String password, String firstName, String lastName, String email, String phoneNumber, Date birthday,
			Account account, Set<UserStock> userStocks) {
		this(username, password, firstName, lastName, email, phoneNumber, birthday);
		this.account = account;
		this.userStocks = userStocks;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setHashedPassword(String password) {
		this.password = SecurityUtils.hash(password);
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthday() {
		return this.birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Set<UserStock> getUserStocks() {
		return this.userStocks;
	}

	public void setUserStocks(Set<UserStock> userStocks) {
		this.userStocks = userStocks;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
	
	public void setAlertTime(int alertTime) {
		this.alertTime = alertTime;
	}
	
	public int getAlertTime() {
		return alertTime;
	}

	public int getStockUpdateTime() {
		return stockUpdateTime;
	}

	public void setStockUpdateTime(int stockUpdateTime) {
		this.stockUpdateTime = stockUpdateTime;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber.replaceAll("-", "");
	}

	@Override
	public String toString() {
		String userToString = "User instance: \n" + 
							"id: " + this.id + "\n" +
							"username: " + this.username + "\n" +
							"first name: " + this.firstName + "\n" +
							"last name: " + this.lastName + "\n" +
							"email: " + this.email + "\n" +
							"dob: " + this.birthday + "\n" +
							"alert time: " + this.alertTime + "\n" + 
							"stock update time: " + this.stockUpdateTime + "\n";
		return userToString;
	}

}

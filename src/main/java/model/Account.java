package main.java.model;

import java.util.HashSet;
import java.util.Set;

public class Account implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int userId;
	private User user;
	private double balance;
	private String accountName;
	private Set<Transaction> transactions = new HashSet<Transaction>(0);

	public Account() {
	}
	public Account(User user, double balance, String accountName, Set<Transaction> transactions) {
		this.user = user;
		this.balance = balance;
		this.accountName = accountName;
		this.transactions = transactions;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getUserId() {
		return this.userId;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public double getBalance() {
		return this.balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getAccountName() {
		return this.accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public Set<Transaction> getTransactions() {
		return this.transactions;
	}

	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
	}
}

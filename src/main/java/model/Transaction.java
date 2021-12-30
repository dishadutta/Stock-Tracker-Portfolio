package main.java.model;
// default package
// Generated Feb 9, 2017 11:38:22 PM by Hibernate Tools 5.2.0.CR1

import main.java.utility.Utils;

import java.util.Date;

public class Transaction implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Account account;
	private Date transactionDate;
	private int stockId;
	private Stock stock;
	private double payment;
	private double balance;

	public Transaction() {
		
	}

	public Transaction(Account account, Date transactionDate) {
		this.account = account;
		this.transactionDate = transactionDate;
	}

	public Transaction(Account account, Date transactionDate, Stock stock) {
		this.account = account;
		this.transactionDate = transactionDate;
		this.stock = stock;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Date getTransactionDate() {
		return this.transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public double getPayment() {
		return payment;
	}

	public void setPayment(double payment) {
		this.payment = Utils.round(payment, 2);
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = Utils.round(balance, 2);;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}
}

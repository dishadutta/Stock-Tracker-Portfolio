package main.java.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionWrapper {
	private Transaction transaction;
	private Stock stock;
	
	private BooleanProperty selected;
	private double totalValue;

	public TransactionWrapper() {}
	
	public TransactionWrapper(Transaction transaction, Stock stock) {
		this.transaction = transaction;
		this.stock = stock;
		selected = new SimpleBooleanProperty(false);
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public String getTransactionDate() {
		Date date = transaction.getTransactionDate();
		SimpleDateFormat fm = new SimpleDateFormat("MM/dd/yyyy");
		return fm.format(date);
	}
	
	public String getTransactionTime() {
		Date date = transaction.getTransactionDate();
		SimpleDateFormat fm = new SimpleDateFormat("HH:mm:ss");
		return fm.format(date);
	}
	
	public String getStockCode() {
		return stock.getStockCode();
	}
	
	public String getStockCompany() {
		return stock.getStockName();
	}
	
	public String getAmount() {
		return String.valueOf(stock.getAmount());
	}
	
	public String getPrice() {
		return "$" + stock.getPriceString();
	}

	public BooleanProperty selectedProperty() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}
	
	public boolean getSelected() {
		return selected.get();
	}
	
	public String getTransactionPayment() {
		return String.valueOf(transaction.getPayment());
	}
	
	public String getBalance() {
		return String.valueOf("$" + transaction.getBalance());
	}

	public String getTotalValue() {
		return "$" + String.valueOf(totalValue);
	}

	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}
}

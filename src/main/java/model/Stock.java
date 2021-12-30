package main.java.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import main.java.utility.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

public class Stock extends RecursiveTreeObject<Stock> implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Transaction transaction;
	// Stock attribute
	private String stockName;
	private String stockCode;
	private BigDecimal price;
	private BigDecimal previousPrice;
	private int amount;
	private Set<UserStock> userStocks = new HashSet<UserStock>(0);
	private StockDetail stockDetail;
	
	// Only for printing out purpose
	private BigDecimal priceChange;
	private BigDecimal priceChangePercent;
	
	
	public Stock() {
		
	}
	
	// Clone
	public Stock(Stock stock) {
		this.stockName = stock.stockName;
		this.amount = stock.amount;
		this.stockCode = stock.stockCode;
		// previous price = bought price for sold stock
		this.previousPrice = stock.previousPrice;
		this.price = stock.price;
	}

	public Stock(Transaction transaction, String stockName, String stockCode, int amount, int owned, BigDecimal price, BigDecimal previousPrice) {
		this.transaction = transaction;
		this.stockName = stockName;
		this.stockCode = stockCode;
		this.amount = amount;
		setPrice(price);
		setPreviousPrice(previousPrice);
	}

	public Stock(Transaction transaction, String stockName, String stockCode, int amount, int owned, BigDecimal price, BigDecimal previousPrice,
	        Set<UserStock> userStocks, StockDetail stockDetail) {
		this(transaction, stockName, stockCode, amount, owned, price, previousPrice);
		this.stockDetail = stockDetail;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Transaction getTransaction() {
		return this.transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public String getStockName() {
		return this.stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getStockCode() {
		return this.stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getPreviousPrice() {
		return this.previousPrice;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public void setPreviousPrice(BigDecimal previousPrice) {
		this.previousPrice = previousPrice;
	}

	public Set<UserStock> getUserStocks() {
		return this.userStocks;
	}

	public void setUserStocks(Set<UserStock> userStocks) {
		this.userStocks = userStocks;
	}

	public StockDetail getStockDetail() {
		return this.stockDetail;
	}

	public void setStockDetail(StockDetail stockDetail) {
		this.stockDetail = stockDetail;
	}
	
	// Used to print out in stock list
	
	public String getPriceString() {
		return Utils.formatCurrencyNumber(price);//Double.toString(price.doubleValue()); //price.toString();
	}
	
	public String getPreviousPriceString() {
		return Utils.formatCurrencyNumber(previousPrice);//Double.toString(previousPrice.doubleValue()); //previousPrice.toString();
	}
	
	public void setPriceChange(BigDecimal priceChange) {
		this.priceChange = priceChange;
	}
	
	public BigDecimal getPriceChange() {
		return priceChange;
	}
	
	public String getPriceChangeString() {
		return Utils.formatCurrencyNumber(priceChange);//Double.toString(priceChange.doubleValue()); // priceChange.toString();
	}
	
	public void setPriceChangePercent(BigDecimal priceChangePercent) {
		priceChangePercent = priceChangePercent.setScale(2, RoundingMode.CEILING);
		this.priceChangePercent = priceChangePercent;
	}
	
	public BigDecimal getPriceChangePercent() {
		return priceChangePercent;
	}
	
	public String getPriceChangePercentString() {
		return Utils.formatCurrencyNumber(priceChangePercent);	}
}

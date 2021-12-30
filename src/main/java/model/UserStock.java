package main.java.model;

import main.java.common.CommonDefine;

import java.math.BigDecimal;

public class UserStock implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private UserStockId id;
	private Stock stock;
	private User user;
	private BigDecimal valueThreshold;
	private BigDecimal combinedValueThreshold;
	private BigDecimal netProfitThreshold;
	private BigDecimal currentValueThreshold;
	private BigDecimal currentCombinedValueThreshold;
	private BigDecimal currentNetProfitThreshold;
	
	private int stockType;
	public UserStock() {
	}

	public UserStock(UserStockId id, Stock stock, User user, int stockType) {
		this.id = id;
		this.stock = stock;
		this.user = user;
		this.valueThreshold = new BigDecimal(-1);
		this.combinedValueThreshold = new BigDecimal(-1);
		this.netProfitThreshold = new BigDecimal(-1);
		this.setStockType(stockType);
	}

	public UserStock(UserStockId id, Stock stock, User user, BigDecimal valueThreshold, BigDecimal combinedValueThreshold, BigDecimal netProfitThreshold) {
		this.id = id;
		this.stock = stock;
		this.user = user;
		this.valueThreshold = valueThreshold;
		this.combinedValueThreshold = combinedValueThreshold;
		this.netProfitThreshold = netProfitThreshold;
	}

	public UserStockId getId() {
		return this.id;
	}

	public void setId(UserStockId id) {
		this.id = id;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getValueThreshold() {
		return valueThreshold;
	}

	public void setValueThreshold(BigDecimal valueThreshold) {
		this.valueThreshold = valueThreshold;
	}

	public BigDecimal getCombinedValueThreshold() {
		return this.combinedValueThreshold;
	}

	public void setCombinedValueThreshold(BigDecimal combinedValueThreshold) {
		this.combinedValueThreshold = combinedValueThreshold;
	}

	public BigDecimal getNetProfitThreshold() {
		return this.netProfitThreshold;
	}

	public void setNetProfitThreshold(BigDecimal netProfitThreshold) {
		this.netProfitThreshold = netProfitThreshold;
	}
	
	public String getValueThresholdString() {
		return valueThreshold.compareTo(new BigDecimal(-1)) == 0 ? "N/A" : String.valueOf(valueThreshold);
	}
	
	public String getCombinedValueThresholdString() {
		return combinedValueThreshold.compareTo(new BigDecimal(-1)) == 0 ? "N/A" : String.valueOf(combinedValueThreshold);
	}
	
	public String getNetProfitThresholdString() {
		return netProfitThreshold.compareTo(new BigDecimal(-1)) == 0 ? "N/A" : String.valueOf(netProfitThreshold);
	}

	public BigDecimal getCurrentValueThreshold() {
		return currentValueThreshold;
	}

	public void setCurrentValueThreshold(BigDecimal currentValueThreshold) {
		this.currentValueThreshold = currentValueThreshold;
	}

	public BigDecimal getCurrentCombinedValueThreshold() {
		return currentCombinedValueThreshold;
	}

	public void setCurrentCombinedValueThreshold(BigDecimal currentCombinedValueThreshold) {
		this.currentCombinedValueThreshold = currentCombinedValueThreshold;
	}

	public BigDecimal getCurrentNetProfitThreshold() {
		return currentNetProfitThreshold;
	}

	public void setCurrentNetProfitThreshold(BigDecimal currentNetProfitThreshold) {
		this.currentNetProfitThreshold = currentNetProfitThreshold;
	}
	
	public String getCurrentValueThresholdString() {
		return currentValueThreshold == null ? "N/A" : String.valueOf(currentValueThreshold);
	}
	
	public String getCurrentCombinedValueThresholdString() {
		return currentCombinedValueThreshold == null ? "N/A" : String.valueOf(currentCombinedValueThreshold);
	}
	
	public String getCurrentNetProfitThresholdString() {
		return currentNetProfitThreshold == null ? "N/A" : String.valueOf(currentNetProfitThreshold);
	}

	public int getStockType() {
		return stockType;
	}

	public void setStockType(int stockType) {
		if (stockType < -2 && stockType > 2) {
			stockType = CommonDefine.INTERESTED_STOCK; 
		}
		this.stockType = stockType;
	}
}

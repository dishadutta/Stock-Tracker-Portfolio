package main.java.model;
// default package
// Generated Feb 9, 2017 11:38:22 PM by Hibernate Tools 5.2.0.CR1

import java.util.Date;

public class StockDetail implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private int stockId;
	private Stock stock;
	private String companyName;
	private String companyDescription;
	private Date listedDate;
	private String remark;

	public StockDetail() {
	}

	public StockDetail(Stock stock) {
		this.stock = stock;
	}

	public StockDetail(Stock stock, String companyName, String companyDescription, Date listedDate, String remark) {
		this.stock = stock;
		this.companyName = companyName;
		this.companyDescription = companyDescription;
		this.listedDate = listedDate;
		this.remark = remark;
	}

	public int getStockId() {
		return this.stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public String getCompanyName() {
		return this.companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyDescription() {
		return this.companyDescription;
	}

	public void setCompanyDescription(String companyDescription) {
		this.companyDescription = companyDescription;
	}

	public Date getListedDate() {
		return this.listedDate;
	}

	public void setListedDate(Date listedDate) {
		this.listedDate = listedDate;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}

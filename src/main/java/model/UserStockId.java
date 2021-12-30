package main.java.model;

public class UserStockId implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private int stockId;
	private int userId;

	public UserStockId() {
	}

	public UserStockId(int stockId, int userId) {
		this.stockId = stockId;
		this.userId = userId;
	}

	public int getStockId() {
		return this.stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UserStockId))
			return false;
		UserStockId castOther = (UserStockId) other;

		return (this.getStockId() == castOther.getStockId()) && (this.getUserId() == castOther.getUserId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getStockId();
		result = 37 * result + this.getUserId();
		return result;
	}

}

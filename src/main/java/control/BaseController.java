package main.java.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import main.java.dao.StockManager;
import main.java.dao.TransactionManager;
import main.java.dao.UserManager;
import main.java.dao.UserStockManager;
import main.java.model.*;

public class BaseController {
	
	protected User user;
	protected UserManager<User> userManager;
	protected StockManager<Stock> stockManager;
	protected UserStockManager<UserStock> userStockManager;
	protected TransactionManager<Transaction> transactionManager;
	
	public BaseController() {
		 userManager = new UserManager<User>();
		 stockManager = new StockManager<Stock>();
		 userStockManager = new UserStockManager<UserStock>();
		 transactionManager = new TransactionManager<Transaction>();
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}

	protected void styleTableCell(TableColumn<TransactionWrapper, String> col) {
		col.setCellFactory(param -> new TableCell<TransactionWrapper, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				if (empty) { // Remove any text and graphic before for every non-qualified row
					setText(null);
					setGraphic(null);
				}
				else {
					super.updateItem(item, empty);
					item = item.replaceAll(",", "");
					StringBuilder bd = new StringBuilder();
					double value = Double.valueOf(item);
					// Price went down
					if (value < 0) {
						setTextFill(Color.RED);
						bd.append("- ").append(item.replace("-", ""));
					} else if (value > 0) { // Price went up
						setTextFill(Color.GREEN);
						bd.append("+ ").append(item);
					} else {

						setTextFill(Color.BLACK);
						bd.append(item);
					}
					setText(bd.toString());
				}
			}
		});
	}
}

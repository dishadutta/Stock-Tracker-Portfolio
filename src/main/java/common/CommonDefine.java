package main.java.common;

public class CommonDefine {
	public static final String EMPTY_FIELD_ERR = "You can't leave the field empty.";
	public static final String INVALID_NAME_ERR = "Your name should be alphabetical.";
	public static final String INVALID_EMAIL_ERR = "Please use only letters (a-z), numbers, and periods.";
	public static final String INVALID_USERNAME_ERR = "Please use only letters (A-Z or a-z), numbers (0-9), periods (.), underscores (_) and dashes (-)";;;
	public static final String PASSWORD_NOT_MATCHED_ERR = "These passwords don't match. Please try again!";
	public static final String PASSWORD_TOO_SHORT_ERR = "Password should include at least 8 characters.";
	public static final String INVALID_USERNAME_LENGTH_ERR = "Please use the username between 6 and 15 characters.";
	public static final String USERNAME_TAKEN_ERR = "The username is already taken. Try another.";
	public static final String EMAIL_TAKEN_ERR = "The email is already taken. Try another.";
	public static final String INCORRECT_INFORMATION_ERR = "Some of the information provided are not correct. Try again.";
	public static final String CURRENT_PASSWORD_INCORRECT = "Current password is incorrect. Try again.";
	public static final String INVALID_PHONE_NUMBER = "Invalid phone number. Please try again.";
	
	// Alert message
	public static final String REMOVE_STOCK_SMS = "Do you really want to remove it?";
	public static final String SELL_STOCK_SMS = "Are you sure you really want to sell these stocks?";
	public static final String TRANSACTION_SUCCESSFUL_SMS = "Your transaction is successful!";
	public static final String UPDATE_ALERT_SETTINGS_SMS = "You've updated alert settings successfully!";
	public static final String NOT_SELECT_ANY_STOCK_SMS = "You must choose at least one stock to sell!";
	public static final String NOT_SELECT_STOCK_AMOUNT_SMS = "You haven't selected any stock amount to buy!";
	public static final String NOT_ENOUGH_BALANCE_TO_BUY_SMS = "Your balance is not sufficient to perform transaction!";
	public static final String RESET_PASSWORD_SUCCESSFULLY_SMS = "You've successfully reset your password! Click OK to switch to Login";
	public static final String ADDED_STOCK_SUCCESSFULLY_SMS = "Added stocks succesfully!";
	public static final String ALREADY_HAD_STOCK_SMS = "You already had this stock!";
	
	// Stage title
	public static final String HOME_TITLE = "Stock Tracker";
	public static final String LOGIN_TITLE = "Login";
	public static final String REGISTRATION_TITLE = "Registration";
	public static final String RESET_PASSWORD_TITLE = "Reset Password";
	public static final String USER_SETTINGS_TITLE = "Settings";
	public static final String STOCK_DETAILS_TITLE = "Stock Details";
	public static final String ALERT_SETTINGS_TITLE = "Alert Settings";
	public static final String PORTFOLIO_TITLE = "Portfolio and Transaction History";
	

	public enum Interval {
		ONE_WEEK,
		ONE_MONTH,
		THREE_MONTH,
		SIX_MONTH,
		ONE_YEAR
	}

	public static final int REMAINING_STOCK = 2;
	public static final int OWNED_STOCK = 1;
	public static final int INTERESTED_STOCK = 0;
	public static final int SOLD_STOCK = -1;
	public static final int TRANSACTION_STOCK = -2;

	public static final String[] DEFAULT_INTERESTED_STOCKS = new String[] { "TCS", "WIPRO", "GODDREJIND" };

}

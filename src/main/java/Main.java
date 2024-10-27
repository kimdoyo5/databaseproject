package cs.toronto.edu; // Ignore red line

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.sql.rowset.serial.SQLInputImpl;

import cs.toronto.edu.FriendManager;
import cs.toronto.edu.PortfolioManager;
import cs.toronto.edu.ReviewManager;
import cs.toronto.edu.SQLUtilities;
import cs.toronto.edu.StockListManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

	static Map<String, Object> cache = new HashMap<String, Object>();

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		int loggedInUserID = -1; // -1 means no user logged in
		Scanner scanner = new Scanner(System.in);

		try {
			// Register the PostgreSQL driver
			Class.forName("org.postgresql.Driver");

			// Connect to the database
			conn = DriverManager.getConnection("jdbc:postgresql://34.130.121.69:5432/appdb",
					"postgres", "postgres");
			System.out.println("Opened database successfully");

			// Create a statement object
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			// Setup tables
			TableCreator.createTables(stmt);

			while (true) {
				// Not logged in
				while (loggedInUserID == -1) {
					int choice = SQLUtilities.printMenuGetInput(new String[] {
							"Sign up",
							"Log in",
							"Exit"
					});
					switch (choice) {
						case 1:
							AccountManager.createAccount(stmt);
							break;
						case 2:
							loggedInUserID = AccountManager.logIn(stmt);
							break;
						case 3:
							System.out.println("Exiting. Goodbye!");
							scanner.close();
							return; // Exit the program
						default:
							System.out.println("Invalid choice. Please select again.");
					}
				}

				// Logged in
				while (loggedInUserID != -1) {
					int choice = SQLUtilities.printMenuGetInput(new String[] {
							"Send friend request",
							"View friend requests",
							"View friends",
							"Create a new portfolio",
							"View your portfolios",
							"Create a new stock list",
							"View your stock lists",
							"View stock lists from other users",
							"Add stock data",
							"View review requests",
							"Log out",
							"Exit"
					});
					switch (choice) {
						// Friends
						case 1:
							FriendManager.sendFriendRequest(stmt, loggedInUserID);
							break;
						case 2:
							FriendManager.viewFriendRequests(stmt, loggedInUserID);
							break;
						case 3:
							FriendManager.viewFriends(stmt, loggedInUserID);
							break;

						// Portfolios
						case 4:
							PortfolioManager.createPortfolioWithInput(stmt, loggedInUserID);
							break;
						case 5:
							PortfolioManager.viewPortfolios(stmt, loggedInUserID);
							break;

						// Stock lists
						case 6:
							StockListManager.createStockListWithInput(stmt, loggedInUserID);
							break;
						case 7:
							StockListManager.viewStockLists(stmt, loggedInUserID);
							break;
						case 8:
							StockListManager.viewStockListsFromOtherUsers(stmt, loggedInUserID);
							break;

						// Stocks
						case 9:
							StockManager.addStockData(stmt);
							break;

						// Reviews
						case 10:
							ReviewManager.viewReviewRequests(stmt, loggedInUserID);
							break;

						// Log out & Exit
						case 11:
							loggedInUserID = -1;
							System.out.println("You are now logged out.");
							break;
						case 12:
							System.out.println("Exiting. Goodbye!");
							scanner.close();
							return; // Exit the program
						default:
							System.out.println("Invalid choice. Please select again.");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();

				System.out.println("Disconnected from the database");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

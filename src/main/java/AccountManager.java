package cs.toronto.edu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.UUID;

import cs.toronto.edu.SQLUtilities;

public class AccountManager {

    public static void createAccount(Statement stmt) {
        while (true) {
            try {
                String firstName = SQLUtilities.printPromptGetString("Enter first name (max 30 characters)", 30);
                String lastName = SQLUtilities.printPromptGetString("Enter last name (max 30 characters)", 30);
                String email = SQLUtilities.printPromptGetString("Enter email address (max 100 characters)", 100);
                String password = SQLUtilities.printPromptGetString("Enter password (max 100 characters)", 100);

                String sql = "INSERT INTO Accounts(firstName, lastName, email, password) VALUES('" + firstName + "', '"
                        + lastName + "', '" + email + "', '" + password + "') RETURNING userID";
                stmt.execute(sql);

                System.out.println("Account created successfully.");

                ResultSet resultFromAccounts = stmt.getResultSet();
                resultFromAccounts.next();
                int userID = resultFromAccounts.getInt(1);
                PortfolioManager.createPortfolio(stmt, userID, firstName + "s Portfolio");
                break;
            } catch (Exception e) {
                System.out.println("Error creating account. Please try again.");
            }
        }
    }

    public static int logIn(Statement stmt) {
        while (true) {
            try {
                String email = SQLUtilities.printPromptGetString("Enter email address", 100);
                String password = SQLUtilities.printPromptGetString("Enter password", 100);

                String sql = SQLUtilities.createSelectStatement("Accounts", new String[] { "userID" },
                        "email = '" + email + "' AND password = '" + password + "'");
                ResultSet result = stmt.executeQuery(sql);

                int numRows = SQLUtilities.numOfResults(result);
                if (numRows == 1) {
                    result.absolute(1);
                    System.out.println("Login successful.");
                    int userID = result.getInt("userID");
                    return userID;
                } else {
                    System.out.println("You do not have an account. Please create an account.");
                    return -1;
                }
            } catch (Exception e) {
                System.out.println("Error logging in. Please try again.");
            }
        }
    }

    public static boolean emailExists(Statement stmt, String email) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("Accounts", new String[] { "1" },
                "email = '" + email + "'");
        ResultSet resultFromAccounts = stmt.executeQuery(sql);

        return SQLUtilities.numOfResults(resultFromAccounts) > 0;
    }

    public static int getUserIDFromEmail(Statement stmt, String email) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("Accounts", new String[] { "userID" },
                "email = '" + email + "'");
        ResultSet resultFromAccounts = stmt.executeQuery(sql);

        resultFromAccounts.absolute(1);
        return resultFromAccounts.getInt("userID");
    }

}
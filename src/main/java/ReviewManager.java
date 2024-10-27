package cs.toronto.edu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cs.toronto.edu.SQLUtilities;
import cs.toronto.edu.StockListManager;

public class ReviewManager {

        public static void requestStockListReview(Statement stmt, int loggedInUserID, int stockListID)
                        throws SQLException {
                String email = SQLUtilities.printPromptGetString(
                                "Enter the email address of the friend you would like to request a review from", 100);

                int friendID = AccountManager.getUserIDFromEmail(stmt, email);

                // Share stock list
                StockListManager.shareStockList(stmt, loggedInUserID, stockListID, email);

                // Request review
                String sql = SQLUtilities.createInsertStatement(
                                "ReviewRequests",
                                new String[] { "sentUserID", "receivedUserID", "stockListID" },
                                new String[] { String.valueOf(loggedInUserID), String.valueOf(friendID),
                                                String.valueOf(stockListID) });
                stmt.execute(sql);

                System.out.println("Review request sent successfully.");
        }

        public static void viewReviewRequests(Statement stmt, int loggedInUserID) throws SQLException {
                String sql = SQLUtilities.createSelectStatement(
                                "ReviewRequests JOIN Accounts ON ReviewRequests.sentUserID = Accounts.userID JOIN StockLists ON ReviewRequests.stockListID = StockLists.stockListID",
                                new String[] { "sentUserID", "firstName", "lastName", "email", "StockLists.stockListID",
                                                "stockListName", "dateTime" },
                                "sentUserID = " + loggedInUserID);
                ResultSet resultFromReviewRequestsAccountsStockLists = stmt.executeQuery(sql);

                int numRowsFromReviewRequestsAccountsStockLists = SQLUtilities
                                .numOfResults(resultFromReviewRequestsAccountsStockLists);
                if (numRowsFromReviewRequestsAccountsStockLists == 0) {
                        System.out.println("You did not send any review requests that are pending.");
                } else {
                        SQLUtilities.printTable(resultFromReviewRequestsAccountsStockLists,
                                        "Active Review Requests You Sent",
                                        new String[] { "First Name", "Last Name", "Email", "Stock List Name", "Time" },
                                        new String[] { "firstName", "lastName", "email", "stockListName", "dateTime" });
                }

                sql = SQLUtilities.createSelectStatement(
                                "ReviewRequests JOIN Accounts ON ReviewRequests.sentUserID = Accounts.userID JOIN StockLists ON ReviewRequests.stockListID = StockLists.stockListID",
                                new String[] { "sentUserID", "firstName", "lastName", "email", "StockLists.stockListID",
                                                "stockListName", "dateTime" },
                                "receivedUserID = " + loggedInUserID);
                resultFromReviewRequestsAccountsStockLists = stmt.executeQuery(sql);

                numRowsFromReviewRequestsAccountsStockLists = SQLUtilities
                                .numOfResults(resultFromReviewRequestsAccountsStockLists);
                if (numRowsFromReviewRequestsAccountsStockLists == 0) {
                        System.out.println("There are no review requests sent to you.");
                        return;
                }

                SQLUtilities.printTable(resultFromReviewRequestsAccountsStockLists,
                                "Active Review Requests Sent to You",
                                new String[] { "First Name", "Last Name", "Email", "Stock List Name", "Time" },
                                new String[] { "firstName", "lastName", "email", "stockListName", "dateTime" });

                int choice = SQLUtilities.printMenuGetInput(new String[] {
                                "Accept a review request",
                                "Reject a review request",
                                "Return to main menu"
                });
                switch (choice) {
                        case 1:
                                int num = SQLUtilities.printPromptGetInput(
                                                "Enter the number of the review request you would like to accept", 1,
                                                numRowsFromReviewRequestsAccountsStockLists);

                                resultFromReviewRequestsAccountsStockLists.absolute(num);
                                int stockListID = resultFromReviewRequestsAccountsStockLists.getInt("stockListID");

                                addOrUpdateReview(stmt, loggedInUserID, stockListID);
                                break;
                        case 2:
                                num = SQLUtilities.printPromptGetInput(
                                                "Enter the number of the review request you would like to reject", 1,
                                                numRowsFromReviewRequestsAccountsStockLists);

                                resultFromReviewRequestsAccountsStockLists.absolute(num);
                                stockListID = resultFromReviewRequestsAccountsStockLists.getInt("stockListID");

                                deleteReviewRequest(stmt, loggedInUserID, stockListID);

                                System.out.println("Review request rejected.");
                                break;
                        case 3:
                                return;
                }
        }

        public static void addOrUpdateReview(Statement stmt, int loggedInUserID, int stockListID) throws SQLException {
                String text = SQLUtilities.printPromptGetString("Enter your review", 4000);

                String sql = SQLUtilities.createSelectStatement("Reviews", new String[] { "1" },
                                "stockListID = " + stockListID + " AND userID = " + loggedInUserID);
                ResultSet resultFromReviews = stmt.executeQuery(sql);

                int numRowsFromReviews = SQLUtilities.numOfResults(resultFromReviews);
                if (numRowsFromReviews != 0) {
                        sql = "UPDATE Reviews SET timestamp = CURRENT_TIMESTAMP, response = '" + text
                                        + "' WHERE userID = " + loggedInUserID + " AND stockListID = " + stockListID;
                        stmt.executeUpdate(sql);
                } else {
                        sql = SQLUtilities.createInsertStatement(
                                        "Reviews", new String[] { "userID", "stockListID", "response" },
                                        new String[] { String.valueOf(loggedInUserID), String.valueOf(stockListID),
                                                        text });
                        stmt.executeUpdate(sql);
                }

                deleteReviewRequest(stmt, loggedInUserID, stockListID);

                System.out.println("Review added successfully.");
        }

        public static void viewReviews(Statement stmt, int loggedInUserID, int stockListID) throws SQLException {
                String sql = SQLUtilities.createSelectStatement(
                                "OwnsStockList NATURAL JOIN StockLists",
                                new String[] { "userID", "StockLists.stockListID", "category" },
                                "StockLists.stockListID = " + stockListID);
                ResultSet resultFromStockListsOwnsStockList = stmt.executeQuery(sql);

                resultFromStockListsOwnsStockList.next();
                int stockListOwnerID = resultFromStockListsOwnsStockList.getInt("userID");
                String stockListCategory = resultFromStockListsOwnsStockList.getString("category");
                ResultSet resultFromReviewsAccounts;
                if (stockListOwnerID == loggedInUserID || stockListCategory.equals("public")) {
                        sql = SQLUtilities.createSelectStatement("Reviews NATURAL JOIN Accounts",
                                        new String[] { "userID", "firstName", "lastName", "email", "timestamp",
                                                        "response" },
                                        "stockListID = " + stockListID);
                        resultFromReviewsAccounts = stmt.executeQuery(sql);

                        int numRowsFromReviewsAccounts = SQLUtilities.numOfResults(resultFromReviewsAccounts);
                        if (numRowsFromReviewsAccounts == 0) {
                                System.out.println("There are no reviews for this stock list.");
                                return;
                        }
                } else if (stockListCategory.equals("shared")) {
                        sql = SQLUtilities.createSelectStatement(
                                        "Reviews NATURAL JOIN Accounts",
                                        new String[] { "userID", "firstName", "lastName", "email", "timestamp",
                                                        "response" },
                                        "stockListID = " + stockListID + " AND userID = " + loggedInUserID);
                        resultFromReviewsAccounts = stmt.executeQuery(sql);

                        int numRowsFromReviewsAccounts = SQLUtilities.numOfResults(resultFromReviewsAccounts);
                        if (numRowsFromReviewsAccounts == 0) {
                                System.out.println("You did not post any reviews for this stock list.");
                                return;
                        }
                } else {
                        System.out.println("You do not have access to reviews.");
                        return;
                }

                SQLUtilities.printTableLastColumnSeparateRow(resultFromReviewsAccounts, "Reviews",
                                new String[] { "First Name", "Last Name", "Email", "Time", "Response" },
                                new String[] { "firstName", "lastName", "email", "timestamp", "response" });

                // Delete review
                int choice = SQLUtilities.printMenuGetInput(new String[] {
                                "Delete a review",
                                "Return to main menu"
                });
                switch (choice) {
                        case 1:
                                int num = SQLUtilities.printPromptGetInput(
                                                "Enter the number of the review you would like to delete", 1,
                                                SQLUtilities.numOfResults(resultFromReviewsAccounts));
                                resultFromReviewsAccounts.absolute(num);
                                int reviewUserID = resultFromReviewsAccounts.getInt("userID");

                                if (reviewUserID == loggedInUserID || stockListOwnerID == loggedInUserID) {
                                        deleteReview(stmt, reviewUserID, stockListID);
                                        System.out.println("Review deleted successfully.");
                                } else {
                                        System.out.println("You can only delete your own reviews.");
                                }
                                break;
                        case 2:
                                return;
                }
        }

        private static void deleteReviewRequest(Statement stmt, int loggedInUserID, int stockListID)
                        throws SQLException {
                String sql = SQLUtilities.createDeleteStatement("ReviewRequests",
                                new String[] { "receivedUserID", "stockListID" },
                                new String[] { String.valueOf(loggedInUserID), String.valueOf(stockListID) });
                stmt.executeUpdate(sql);
        }

        private static void deleteReview(Statement stmt, int reviewUserID, int stockListID) throws SQLException {
                String sql = SQLUtilities.createDeleteStatement("Reviews", new String[] { "userID", "stockListID" },
                                new String[] { String.valueOf(reviewUserID), String.valueOf(stockListID) });
                stmt.executeUpdate(sql);
        }

}
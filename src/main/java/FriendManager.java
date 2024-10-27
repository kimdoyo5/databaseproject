package cs.toronto.edu;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cs.toronto.edu.AccountManager;
import cs.toronto.edu.SQLUtilities;

public class FriendManager {

    public static void sendFriendRequest(Statement stmt, int loggedInUserID) throws SQLException {
        String email = SQLUtilities.printPromptGetString("Enter the email address of the friend you would like to add",
                100);

        if (!AccountManager.emailExists(stmt, email)) {
            System.out.println("No user with the given email address exists.");
            return;
        }

        int friendID = AccountManager.getUserIDFromEmail(stmt, email);
        if (friendID == loggedInUserID) {
            System.out.println("You cannot send a friend request to yourself.");
            return;
        }

        String sql = "SELECT status, dateTimeAddedDeleted FROM Friends WHERE ((user1 = '"
                + loggedInUserID
                + "' AND user2 = '"
                + friendID
                + "') OR (user1 = '"
                + friendID
                + "' AND user2 = '"
                + loggedInUserID
                + "'))";
        ResultSet resultFromFriends = stmt.executeQuery(sql);

        int numRowsFromFriends = SQLUtilities.numOfResults(resultFromFriends);
        if (numRowsFromFriends == 1) {
            resultFromFriends.absolute(1);
            String status = resultFromFriends.getString("status");
            if (status.equals("added")) {
                System.out.println("You are already friends with this user.");
                return;
            } else if (status.equals("deleted")) {
                Timestamp ts1 = new Timestamp(System.currentTimeMillis());
                Timestamp ts2 = resultFromFriends.getTimestamp("dateTimeAddedDeleted");

                Instant in1 = ts1.toInstant();
                Instant in2 = ts2.toInstant();

                Duration timeSinceDeletion = Duration.between(in2, in1);
                if (timeSinceDeletion.toMinutes() < 5) {
                    System.out.println("The friend was deleted less than 5 minutes ago. Please try again later.");
                    return;
                }
            }
        }

        sql = "SELECT sentUserID AS user1, receivedUserID AS user2, status, dateTime FROM FriendRequests WHERE sentUserID = '"
                + loggedInUserID
                + "' AND receivedUserID = '"
                + friendID
                + "'"
                + " UNION "
                + "SELECT sentUserID AS user1, receivedUserID AS user2, status, dateTime FROM FriendRequests WHERE sentUserID = '"
                + friendID
                + "' AND receivedUserID = '"
                + loggedInUserID
                + "'";
        ResultSet resultFromFriendRequests = stmt.executeQuery(sql);

        int numRowsFromFriendRequests = SQLUtilities.numOfResults(resultFromFriendRequests);
        if (numRowsFromFriendRequests == 0) {
            System.out.println("There is no existing friend request with this user. Sending friend request...");
            sql = SQLUtilities.createInsertStatement("FriendRequests",
                    new String[] { "sentUserID", "receivedUserID" },
                    new String[] { Integer.toString(loggedInUserID), Integer.toString(friendID) });
            stmt.executeUpdate(sql);
            System.out.println("Friend request sent.");
        } else {
            resultFromFriendRequests.absolute(1);
            if (resultFromFriendRequests.getString("status").equals("pending")) {
                System.out.println("There is already an existing friend request with this user.");
            } else if (resultFromFriendRequests.getString("status").equals("rejected")) {
                Timestamp ts1 = new Timestamp(System.currentTimeMillis());
                Timestamp ts2 = resultFromFriendRequests.getTimestamp("dateTime");

                Instant in1 = ts1.toInstant();
                Instant in2 = ts2.toInstant();

                Duration timeSinceRejection = Duration.between(in2, in1);
                if (timeSinceRejection.toMinutes() >= 5) {
                    System.out.println(
                            "The friend request has been rejected previously over 5 minutes ago. Sending friend request again.");
                    sql = SQLUtilities.createInsertStatement("FriendRequests",
                            new String[] { "sentUserID", "receivedUserID" },
                            new String[] { Integer.toString(loggedInUserID), Integer.toString(friendID) });
                    stmt.executeUpdate(sql);
                    System.out.println("Friend request sent.");
                } else {
                    System.out.println(
                            "The friend request was rejected less than 5 minutes ago. Please try again later.");
                }
            }
        }
    }

    public static void viewFriendRequests(Statement stmt, int loggedInUserID) throws SQLException {
        String sql = "SELECT firstName, lastName, email, dateTime FROM FriendRequests JOIN Accounts ON FriendRequests.receivedUserID = Accounts.userID WHERE sentUserID = '"
                + loggedInUserID
                + "' AND status = 'pending'";
        ResultSet resultFromFriendRequests = stmt.executeQuery(sql);

        int numRowsFromFriendRequests = SQLUtilities.numOfResults(resultFromFriendRequests);
        if (numRowsFromFriendRequests == 0) {
            System.out.println("You did not send any friend requests that are pending.");
        } else {
            SQLUtilities.printTable(resultFromFriendRequests, "Active Friend Requests You Sent",
                    new String[] { "First Name", "Last Name", "Email", "Time" },
                    new String[] { "firstName", "lastName", "email", "dateTime" });
        }

        sql = "SELECT sentUserID, firstName, lastName, email, dateTime FROM FriendRequests JOIN Accounts ON FriendRequests.sentUserID = Accounts.userID WHERE receivedUserID = '"
                + loggedInUserID
                + "' AND status = 'pending'";
        resultFromFriendRequests = stmt.executeQuery(sql);

        numRowsFromFriendRequests = SQLUtilities.numOfResults(resultFromFriendRequests);
        if (numRowsFromFriendRequests == 0) {
            System.out.println("There are no friend requests sent to you.");
            return;
        }

        SQLUtilities.printTable(resultFromFriendRequests, "Active Friend Requests Sent to You",
                new String[] { "First Name", "Last Name", "Email", "Time" },
                new String[] { "firstName", "lastName", "email", "dateTime" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Accept a friend request",
                "Reject a friend request",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                int num = SQLUtilities.printPromptGetInput(
                        "Enter the number of the friend request you would like to accept", 1,
                        numRowsFromFriendRequests);

                resultFromFriendRequests.absolute(num);
                int sentUserID = resultFromFriendRequests.getInt("sentUserID");

                sql = "SELECT 1 FROM Friends WHERE ((user1 = '"
                        + loggedInUserID
                        + "' AND user2 = '"
                        + sentUserID
                        + "') OR (user2 = '"
                        + loggedInUserID
                        + "' AND user1 = '"
                        + sentUserID
                        + "')) AND status = 'deleted'";
                ResultSet resultFromFriends = stmt.executeQuery(sql);

                int numRowsFromFriends = SQLUtilities.numOfResults(resultFromFriends);
                if (numRowsFromFriends != 0) {
                    sql = "UPDATE Friends SET status = 'added', dateTimeAddedDeleted = CURRENT_TIMESTAMP WHERE ((user1 = '"
                            + loggedInUserID
                            + "' AND user2 = '"
                            + sentUserID
                            + "') OR (user2 = '"
                            + loggedInUserID
                            + "' AND user1 = '"
                            + sentUserID
                            + "')) AND status = 'deleted'";
                    stmt.executeUpdate(sql);
                } else {
                    sql = SQLUtilities.createInsertStatement("Friends", new String[] { "user1", "user2" },
                            new String[] { Integer.toString(loggedInUserID), Integer.toString(sentUserID) });
                    stmt.executeUpdate(sql);
                }

                sql = "DELETE FROM FriendRequests WHERE sentUserID = '"
                        + sentUserID
                        + "' AND receivedUserID = '"
                        + loggedInUserID
                        + "'";
                stmt.executeUpdate(sql);

                System.out.println("Friend request accepted.");
                break;
            case 2:
                num = SQLUtilities.printPromptGetInput(
                        "Enter the number of the friend request you would like to reject", 1,
                        numRowsFromFriendRequests);

                resultFromFriendRequests.absolute(num);
                sentUserID = resultFromFriendRequests.getInt("sentUserID");

                sql = "UPDATE FriendRequests SET status = 'rejected', dateTime = CURRENT_TIMESTAMP WHERE sentUserID = '"
                        + sentUserID
                        + "' AND receivedUserID = '"
                        + loggedInUserID + "'";
                stmt.executeUpdate(sql);

                System.out.println("Friend request rejected.");
                break;
            case 3:
                return;
        }
    }

    public static void viewFriends(Statement stmt, int loggedInUserID) throws SQLException {
        String sql = "(SELECT user1 AS friend, firstName, lastName, email FROM Accounts JOIN Friends ON Accounts.userID = Friends.user1 WHERE user2 = '"
                + loggedInUserID
                + "' AND status = 'added') UNION (SELECT user2 AS friend, firstName, lastName, email FROM Accounts JOIN Friends ON Accounts.userID = Friends.user2 WHERE user1 = '"
                + loggedInUserID
                + "' AND status = 'added')";
        ResultSet resultFromFriends = stmt.executeQuery(sql);

        int numRowsFromFriends = SQLUtilities.numOfResults(resultFromFriends);
        if (numRowsFromFriends == 0) {
            System.out.println("You have no friends added.");
            return;
        }

        SQLUtilities.printTable(resultFromFriends, "Friends", new String[] { "First Name", "Last Name", "Email" },
                new String[] { "firstName", "lastName", "email" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Delete a friend",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                int num = SQLUtilities.printPromptGetInput("Enter the number of the friend you would like to delete", 1,
                        numRowsFromFriends);

                resultFromFriends.absolute(num);
                int friendID = resultFromFriends.getInt("friend");

                sql = "UPDATE Friends SET status = 'deleted', dateTimeAddedDeleted = CURRENT_TIMESTAMP WHERE (user1 = '"
                        + friendID
                        + "' AND user2 = '"
                        + loggedInUserID + "') OR (user1 = '"
                        + loggedInUserID
                        + "' AND user2 = '"
                        + friendID + "')";
                stmt.executeUpdate(sql);

                System.out.println("Friend deleted.");
                break;
            case 2:
                return;
        }
    }

    public static boolean isFriend(Statement stmt, int loggedInUserID, int friendID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("Friends", new String[] { "user1", "user2" },
                "(user1 = '" + loggedInUserID + "' AND user2 = '" + friendID + "') OR " +
                        "(user2 = '" + loggedInUserID + "' AND user1 = '" + friendID + "')");
        ResultSet resultFromFriends = stmt.executeQuery(sql);
        return SQLUtilities.numOfResults(resultFromFriends) > 0;
    }

}
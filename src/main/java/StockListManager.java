package cs.toronto.edu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Scanner;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.sql.Timestamp;

import cs.toronto.edu.AccountManager;
import cs.toronto.edu.FriendManager;
import cs.toronto.edu.ReviewManager;
import cs.toronto.edu.SQLUtilities;

public class StockListManager {

    public static void createStockListWithInput(Statement stmt, int loggedInUserID) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        String stockListName = SQLUtilities.printPromptGetString("Enter stock list name (max 30 characters)", 30);

        String sql = SQLUtilities.createSelectStatement(
                "OwnsStockList NATURAL JOIN StockLists",
                new String[] { "1" },
                "stockListName = '" + stockListName + "' AND userID = " + loggedInUserID);
        ResultSet resultFromStockListsOwnsStockList = stmt.executeQuery(sql);

        if (SQLUtilities.numOfResults(resultFromStockListsOwnsStockList) > 0) {
            System.out.println("Stock list name already exists.");
            return;
        }

        createStockList(stmt, loggedInUserID, stockListName, "private");
    }

    public static int createStockList(Statement stmt, int loggedInUserID, String stockListName, String category)
            throws SQLException {
        String sql = "INSERT INTO StockLists(stockListName, category) VALUES('" +
                stockListName + "', '" + category + "') RETURNING stockListID";
        stmt.execute(sql);
        ResultSet resultFromStockLists = stmt.getResultSet();
        resultFromStockLists.next();
        int stockListID = resultFromStockLists.getInt(1);

        sql = SQLUtilities.createInsertStatement("OwnsStockList", new String[] { "userID", "stockListID" },
                new String[] { Integer.toString(loggedInUserID), Integer.toString(stockListID) });
        stmt.executeUpdate(sql);

        System.out.println("Stock list created successfully.");

        return stockListID;
    }

    // View stock lists
    public static void viewStockLists(Statement stmt, int loggedInUserID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("OwnsStockList NATURAL JOIN StockLists",
                new String[] { "stockListID", "stockListName", "category" },
                "userID = " + loggedInUserID + " AND category != 'linkedToPortfolio'");
        ResultSet resultFromStockListsOwnsStockList = stmt.executeQuery(sql);

        int numRowsFromStockListsOwnsStockList = SQLUtilities.numOfResults(resultFromStockListsOwnsStockList);
        if (numRowsFromStockListsOwnsStockList == 0) {
            System.out.println(
                    "You have no stock lists. Portfolio stock lists are not shown here. View portfolio stock lists under the corresponding portfolio.");
            return;
        }

        SQLUtilities.printTable(resultFromStockListsOwnsStockList, "Stock Lists",
                new String[] { "Stock List Name", "Category" },
                new String[] { "stockListName", "category" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Select a stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                int num = SQLUtilities.printPromptGetInput(
                        "Enter the number of the stock list you would like to select", 1,
                        numRowsFromStockListsOwnsStockList);
                resultFromStockListsOwnsStockList.absolute(num);
                int stockListID = resultFromStockListsOwnsStockList.getInt("stockListID");
                manageOwnStockList(stmt, loggedInUserID, stockListID);
                break;
            case 2:
                break;
        }
    }

    // Perform actions on existing stock lists
    public static void manageOwnStockList(Statement stmt, int loggedInUserID, int stockListID) throws SQLException {
        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Add stock to stock list",
                "Remove stock from stock list",
                "View stocks in stock list",
                "Change visibility status (category)",
                "View statistics",
                "Share stock list",
                "Request stock list review",
                "View reviews",
                "Delete stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1: // "Add stock to stock list"
                String stockSymbol = SQLUtilities
                        .printPromptGetString("Please enter the stock symbol of the stock you would like to add", 10);
                if (!stockExists(stmt, stockSymbol)) {
                    System.out.println("Stock does not exist.");
                    return;
                }
                int numShares = SQLUtilities.printPromptGetInput("Please enter the number of shares", 1,
                        Integer.MAX_VALUE);
                addStockToStockList(stmt, stockListID, stockSymbol, numShares);
                break;
            case 2: // "Remove stock from stock list"
                stockSymbol = SQLUtilities.printPromptGetString(
                        "Please enter the stock symbol of the stock you would like to delete", 10);
                if (!stockExistsInStockList(stmt, stockSymbol, stockListID)) {
                    System.out.println("Stock does not exist in stock list.");
                    return;
                }
                removeStockFromStockList(stmt, stockListID, stockSymbol);
                break;
            case 3: // "View stocks in stock list"
                viewStocksInStockList(stmt, stockListID);
                break;
            case 4: // "Change visibility status (category)"
                String newCategory = SQLUtilities.printPromptGetString("Please enter new category", 7);
                changeCategory(stmt, stockListID, newCategory);
                break;
            case 5: // "View statistics"
                Date startTime = SQLUtilities.printPromptGetDate("Please enter start date (yyyy-MM-dd) (inclusive)");
                Date endTime = SQLUtilities.printPromptGetDate("Please enter end date (yyyy-MM-dd) (inclusive)");
                viewStatistics(stmt, stockListID, startTime, endTime);
                break;
            case 6: // "Share stock list"
                String email = SQLUtilities.printPromptGetString(
                    "Enter the email address of the friend you would like to share the list with", 100);
                shareStockList(stmt, loggedInUserID, stockListID, email);
                break;
            case 7: // "Request stock list review"
                ReviewManager.requestStockListReview(stmt, loggedInUserID, stockListID);
                break;
            case 8: // "View reviews"
                ReviewManager.viewReviews(stmt, loggedInUserID, stockListID);
                break;
            case 9: // "Delete stock list"
                String sql = SQLUtilities.createDeleteStatement("StockLists",
                        new String[] { "stockListID" }, new Integer[] { stockListID });
                stmt.execute(sql);
                break;
            case 10: // "Return to main menu"
                return;
        }
    }

    public static void manageOtherUserStockList(Statement stmt, int loggedInUserID, int stockListID)
            throws SQLException {
        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "View stocks in stock list",
                "View statistics",
                "Add/update review",
                "View reviews",
                "Return to main menu"
        });
        switch (choice) {
            case 1: // "View stocks in stock list"
                viewStocksInStockList(stmt, stockListID);
                break;
            case 2: // "View statistics"
                Date startTime = SQLUtilities.printPromptGetDate("Please enter start date (yyyy-MM-dd) (inclusive)");
                Date endTime = SQLUtilities.printPromptGetDate("Please enter end date (yyyy-MM-dd) (inclusive)");
                viewStatistics(stmt, stockListID, startTime, endTime);
                break;
            case 3: // "Add/update review"
                ReviewManager.addOrUpdateReview(stmt, loggedInUserID, stockListID);
                break;
            case 4: // "View reviews"
                ReviewManager.viewReviews(stmt, loggedInUserID, stockListID);
                break;
            case 5: // "Return to main menu"
                return;
        }
    }

    public static boolean stockExists(Statement stmt, String stockSymbol) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("Stocks", new String[] { "1" },
                "symbol = '" + stockSymbol + "'");
        ResultSet resultFromStocks = stmt.executeQuery(sql);

        return SQLUtilities.numOfResults(resultFromStocks) > 0;
    }

    public static boolean stockExistsInStockList(Statement stmt, String stockSymbol, int stockListID)
            throws SQLException {
        String sql = SQLUtilities.createSelectStatement("StockListStocks", new String[] { "1" },
                "stockListID = " + stockListID + " AND symbol = '" + stockSymbol + "'");
        ResultSet resultFromStockListStocks = stmt.executeQuery(sql);

        return SQLUtilities.numOfResults(resultFromStockListStocks) > 0;
    }

    public static void addStockToStockList(Statement stmt, int stockListID, String stockSymbol,
            int numShares) throws SQLException {
        String sql = "INSERT INTO StockListStocks (stockListID, symbol, numShares) VALUES ('"
                + stockListID + "', '" + stockSymbol + "', '" + numShares
                + "') ON CONFLICT(stockListID, symbol) DO UPDATE SET numShares = StockListStocks.numShares + "
                + numShares;

        try {
            stmt.executeUpdate(sql);
            System.out.println("Stock " + stockSymbol + " added successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding stock to stock list: " + e.getMessage());
            throw e; // Re-throw the exception after logging the error
        }
    }

    public static void removeStockFromStockList(Statement stmt, int stockListID, String stockSymbol)
            throws SQLException {
        String sql = "DELETE FROM StockListStocks WHERE stockListID = " + stockListID +
                " AND symbol = '" + stockSymbol + "'";

        try {
            stmt.executeUpdate(sql);
            System.out.println("Stock " + stockSymbol + " removed successfully.");
        } catch (SQLException e) {
            System.err.println("Error removing stock from stock list: " + e.getMessage());
            throw e; // Re-throw the exception after logging the error
        }
    }

    public static void viewStocksInStockList(Statement stmt, int stockListID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("StockListStocks", new String[] { "symbol", "numShares" },
                "stockListID = " + stockListID);
        ResultSet resultFromStockListStocks = stmt.executeQuery(sql);
        int numRowsFromStockListStocks = SQLUtilities.numOfResults(resultFromStockListStocks);

        if (numRowsFromStockListStocks == 0) {
            System.out.println("There are no stocks.");
            return;
        }

        SQLUtilities.printTable(resultFromStockListStocks, "Stocks in Stock List",
                new String[] { "Symbol", "Number of Shares" },
                new String[] { "symbol", "numShares" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "View details of a stock",
                "View market value of stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1: // "View details of a stock"
                int num = SQLUtilities.printPromptGetInput(
                        "Enter the number of the stock you would like to select", 1,
                        numRowsFromStockListStocks);
                resultFromStockListStocks.absolute(num);
                String stockSymbol = resultFromStockListStocks.getString("symbol");
                Date startTime = SQLUtilities.printPromptGetDate("Please enter start date (yyyy-MM-dd) (inclusive)");
                Date endTime = SQLUtilities.printPromptGetDate("Please enter end date (yyyy-MM-dd) (inclusive)");
                StockManager.viewStockDetails(stmt, stockSymbol, startTime, endTime);
                break;
            case 2: // "View market value of stock list"
                double marketValue = getMarketValueOfStockList(stmt, stockListID);
                System.out.println("The market value is " + SQLUtilities.formatAsMoney(marketValue) + ".");
                break;
            case 3: // "Return to main menu"
                return;
        }
    }

    private static double getMarketValueOfStockList(Statement stmt, int stockListID) throws SQLException {
        String sql = "SELECT SUM(value) AS marketValueOfStockList FROM (SELECT ds.symbol, ds.close, numShares, ds.close * numShares AS value FROM (DailyStocks ds JOIN (SELECT symbol, MAX(timestamp) AS latest_timestamp FROM DailyStocks GROUP BY symbol) sub ON ds.symbol = sub.symbol AND ds.timestamp = sub.latest_timestamp) JOIN StockListStocks ON ds.symbol = StockListStocks.symbol WHERE stockListID = "
                + stockListID + ")";
        ResultSet result = stmt.executeQuery(sql);

        result.next();
        double marketValue = result.getDouble("marketValueOfStockList");

        return marketValue;
    }

    private static void changeCategory(Statement stmt, int stockListID, String newCategory)
            throws SQLException {
        if ("public".equals(newCategory) || "shared".equals(newCategory) || "private".equals(newCategory)) {
            String condition = "stockListID = " + stockListID;
            String sql = SQLUtilities.createUpdateStatement("StockLists", new String[] { "category" },
                    new String[] { newCategory }, condition);
            stmt.execute(sql);

            if (newCategory.equals("private")) {
                sql = SQLUtilities.createDeleteStatement("AccessibleBy", new String[] { "stockListID" },
                        new String[] { Integer.toString(stockListID) });
                stmt.execute(sql);
            }
        } else {
            System.out.println("Invalid category: " + newCategory);
        }
    }

    private static String getCategory(Statement stmt, int stockListID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("StockLists", new String[] { "category" },
                "stockListID = '" + stockListID + "'");
        ResultSet resultFromStockLists = stmt.executeQuery(sql);

        resultFromStockLists.absolute(1);
        return resultFromStockLists.getString("category");
    }

    public static void shareStockList(Statement stmt, int loggedInUserID, int stockListID, String email) throws SQLException {
        if (getCategory(stmt, stockListID) == "public") {
            System.out.println("The stock list is already shared with everyone.");
            return;
        }

        if (!AccountManager.emailExists(stmt, email)) {
            System.out.println("No user with the given email address exists.");
            return;
        }

        int friendID = AccountManager.getUserIDFromEmail(stmt, email);
        if (friendID == loggedInUserID) {
            System.out.println("You cannot share the list to yourself.");
            return;
        }

        // Check if friend
        if (!FriendManager.isFriend(stmt, loggedInUserID, friendID)) {
            System.out.println("You are not friends with the user.");
            return;
        }

        // Share list
        String sql = "INSERT INTO AccessibleBy(userID, stockListID) VALUES(" + Integer.toString(friendID) + ", "
                + Integer.toString(stockListID) + ") ON CONFLICT DO NOTHING";
        stmt.execute(sql);
        changeCategory(stmt, stockListID, "shared");

        System.out.println("Stock list shared successfully.");
    }

    public static void viewStockListsFromOtherUsers(Statement stmt, int loggedInUserID) throws SQLException {
        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "View public stock lists",
                "View stock lists shared with you",
                "Return to main menu"
        });
        switch (choice) {
            case 1: // "View public stock lists"
                viewPublicStockLists(stmt, loggedInUserID);
                break;
            case 2: // "View stock lists shared with you"
                viewStockListsSharedWithYou(stmt, loggedInUserID);
                break;
            case 3:
                return;
        }
    }

    private static void viewPublicStockLists(Statement stmt, int loggedInUserID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("OwnsStockList NATURAL JOIN StockLists NATURAL JOIN Accounts",
                new String[] { "stockListID", "stockListName", "firstName", "lastName", "email" },
                "category = 'public'");
        ResultSet resultFromStockListsOwnsStockListAccounts = stmt.executeQuery(sql);

        int numRowsFromStockListsOwnsStockListAccounts = SQLUtilities
                .numOfResults(resultFromStockListsOwnsStockListAccounts);
        if (numRowsFromStockListsOwnsStockListAccounts == 0) {
            System.out.println("There are no public stock lists.");
            return;
        }

        SQLUtilities.printTable(resultFromStockListsOwnsStockListAccounts, "Public Stock Lists",
                new String[] { "First Name", "Last Name", "Email", "Stock List Name" },
                new String[] { "firstName", "lastName", "email", "stockListName" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Select a stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                int num = SQLUtilities.printPromptGetInput(
                        "Enter the number of the stock list you would like to select", 1,
                        numRowsFromStockListsOwnsStockListAccounts);
                resultFromStockListsOwnsStockListAccounts.absolute(num);
                int stockListID = resultFromStockListsOwnsStockListAccounts.getInt("stockListID");
                manageOtherUserStockList(stmt, loggedInUserID, stockListID);
                break;
            case 2:
                return;
        }
    }

    private static void viewStockListsSharedWithYou(Statement stmt, int loggedInUserID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement(
                "OwnsStockList NATURAL JOIN StockLists NATURAL JOIN Accounts JOIN AccessibleBy ON StockLists.stockListID = AccessibleBy.stockListID",
                new String[] { "StockLists.stockListID", "stockListName", "firstName", "lastName", "email" },
                "AccessibleBy.userID = " + loggedInUserID);
        ResultSet resultFromAccessibleByStockListsAccountsAccessibleBy = stmt.executeQuery(sql);

        int numRowsFromAccessibleByStockListsAccountsAccessibleBy = SQLUtilities
                .numOfResults(resultFromAccessibleByStockListsAccountsAccessibleBy);
        if (numRowsFromAccessibleByStockListsAccountsAccessibleBy == 0) {
            System.out.println("You have no stock lists shared with you.");
            return;
        }

        SQLUtilities.printTable(resultFromAccessibleByStockListsAccountsAccessibleBy, "Stock Lists Shared With You",
                new String[] { "First Name", "Last Name", "Email", "Stock List Name" },
                new String[] { "firstName", "lastName", "email", "stockListName" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Select a stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                int num = SQLUtilities.printPromptGetInput(
                        "Enter the number of the stock list you would like to select", 1,
                        numRowsFromAccessibleByStockListsAccountsAccessibleBy);
                resultFromAccessibleByStockListsAccountsAccessibleBy.absolute(num);
                int stockListID = resultFromAccessibleByStockListsAccountsAccessibleBy.getInt("stockListID");
                manageOtherUserStockList(stmt, loggedInUserID, stockListID);
                break;
            case 2:
                return;
        }
    }

    // ***********************************************
    // Statistics
    // ***********************************************

    public static void viewStatistics(Statement stmt, int stockListID, Date startTime,
            Date endTime) throws SQLException {
        if (startTime.after(endTime)) {
            System.out.println("Error: start date must be before end date.");
            return;
        }

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "View statistics for individual stocks in stock list",
                "View matrix for entire stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                String sql = SQLUtilities.createSelectStatement("stockListStocks", new String[] { "symbol" },
                        "stockListID = " + stockListID);
                ResultSet resultFromStockListStocks = stmt.executeQuery(sql);

                String[][] table = new String[SQLUtilities.numOfResults(resultFromStockListStocks)][3];

                int i = 0;
                while (resultFromStockListStocks.next()) {
                    table[i][0] = resultFromStockListStocks.getString("symbol");
                    i++;
                }

                for (i = 0; i < table.length; i++) {
                    String symbol = table[i][0];
                    double[] curStockStats = getStockStatistics(stmt, symbol, startTime, endTime);
                    table[i][1] = String.format("%.2f", curStockStats[0]);
                    table[i][2] = String.format("%.2f", curStockStats[1]);
                }

                SQLUtilities.printTable(table, "Stock Statistics", new String[] { "Symbol", "Beta", "Coeff. of Var." });
                break;
            case 2:
                viewStockListStatistics(stmt, stockListID, startTime, endTime);
                break;
            case 3:
                return;
        }
    }

    private static double calculateCovariance(List<Double> list1, List<Double> list2) {
        int n = Math.min(list1.size(), list2.size());

        double mean1 = calculateMean(list1);
        double mean2 = calculateMean(list2);

        double sum = 0;

        for (int i = 0; i < n; i++) {
            sum += (list1.get(i) - mean1) * (list2.get(i) - mean2);
        }

        return sum / n;
    }

    private static double calculateVariance(List<Double> list) {
        int n = list.size();

        double mean = calculateMean(list);

        double sqDiff = 0;
        for (int i = 0; i < n; i++) {
            sqDiff += (list.get(i) - mean) * (list.get(i) - mean);
        }

        return sqDiff / n;
    }

    private static double calculateStdDev(List<Double> list) {
        return Math.sqrt(calculateVariance(list));
    }

    private static double calculateMean(List<Double> list) {
        double avg = 0;
        for (double num : list) {
            avg += num;
        }
        return avg / list.size();
    }

    private static List<Double> getStockReturnsOneStock(Statement stmt, String symbol, Date startTime, Date endTime)
            throws SQLException {
        // Fetch daily stock data for the given symbol within the date range
        String sql = "SELECT timestamp, close FROM DailyStocks WHERE symbol = '" + symbol + "' AND timestamp >= '"
                + startTime + "' AND timestamp <= '" + endTime + "' ORDER BY timestamp ASC";
        ResultSet resultForStock = stmt.executeQuery(sql);

        List<Double> stockReturnsDailyPercentChg = new ArrayList<Double>();

        double previousStockClose = -1;

        // Calculate daily returns
        while (resultForStock.next()) {
            double stockClose = (double) resultForStock.getFloat("close");

            if (previousStockClose != -1) {
                double stockReturnPercentChg = (stockClose / previousStockClose) - 1;

                stockReturnsDailyPercentChg.add(stockReturnPercentChg);
            }

            previousStockClose = stockClose;
        }

        return stockReturnsDailyPercentChg;
    }

    // Array returned contains beta in first index and coefficient of variation in
    // second index
    private static double[] getStockStatistics(Statement stmt, String symbol, Date startTime, Date endTime)
            throws SQLException {
        if (Main.cache.containsKey("stock" + symbol + startTime + endTime)) {
            System.out.println("Accessing cache.");
            return (double[]) Main.cache.get("stock" + symbol + startTime + endTime);
        }

        System.out.println("Cache does not contain the required data. Computing...");

        List<Double> stockReturnsDailyPercentChg = getStockReturnsOneStock(stmt, symbol, startTime, endTime);
        List<Double> marketReturnsDailyPercentChg = getStockReturnsOneStock(stmt, "SP500", startTime, endTime);

        // Calculate beta
        double covariance = calculateCovariance(stockReturnsDailyPercentChg, marketReturnsDailyPercentChg);
        double varianceMarketReturn = calculateVariance(marketReturnsDailyPercentChg);
        double beta = covariance / varianceMarketReturn;

        // Calculate coefficient of variation
        double stdDevStockReturn = calculateStdDev(stockReturnsDailyPercentChg);
        double meanStockReturn = calculateMean(stockReturnsDailyPercentChg);
        double coeffVar = stdDevStockReturn / meanStockReturn;

        double[] result = { beta, coeffVar };
        Main.cache.put("stock" + symbol + startTime + endTime, result);

        return result;
    }

    private static void viewStockListStatistics(Statement stmt, int stockListID, Date startTime, Date endTime)
            throws SQLException {
        String sql = SQLUtilities.createSelectStatement("StockListStocks", new String[] { "symbol" },
                "stockListID = " + stockListID);
        ResultSet stocks = stmt.executeQuery(sql);

        int numStocks = SQLUtilities.numOfResults(stocks);
        if (numStocks == 0) {
            System.out.println("No stocks found in stock list.");
            return;
        }

        // Convert stocks to array
        String[] symbols = new String[numStocks];
        int i = 0;
        while (stocks.next()) {
            symbols[i] = stocks.getString("symbol");
            i++;
        }

        double[][] covarianceMatrix;

        if (Main.cache.containsKey("covar-matrix" + stockListID + startTime + endTime)) {
            System.out.println("Accessing cache.");
            covarianceMatrix = (double[][]) Main.cache.get("covar-matrix" + stockListID + startTime + endTime);
        } else {
            System.out.println("Cache does not contain the covariance matrix. Computing...");

            covarianceMatrix = calculateCovarianceMatrix(stmt, symbols, startTime, endTime);
            Main.cache.put("covar-matrix" + stockListID + startTime + endTime, covarianceMatrix);
        }

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "View covariance matrix",
                "View correlation matrix"
        });
        switch (choice) {
            case 1: // "View covariance matrix"
                printMatrix(covarianceMatrix, symbols);
                break;
            case 2: // "View correlation matrix"
                double[][] correlationMatrix;

                if (Main.cache.containsKey("corr-matrix" + stockListID + startTime + endTime)) {
                    System.out.println("Accessing cache.");
                    correlationMatrix = (double[][]) Main.cache.get("corr-matrix" + stockListID + startTime + endTime);
                } else {
                    System.out.println("Cache does not contain the correlation matrix. Computing...");

                    correlationMatrix = calculateCorrelationMatrix(covarianceMatrix);
                    Main.cache.put("corr-matrix" + stockListID + startTime + endTime, correlationMatrix);
                }

                printMatrix(correlationMatrix, symbols);
                break;
        }

    }

    private static Map<String, List<Double>> getStockReturns(Statement stmt, String[] symbols, Date startTime,
            Date endTime) throws SQLException {
        Map<String, List<Double>> stockReturns = new HashMap<String, List<Double>>();

        for (String symbol : symbols) {
            List<Double> stockReturnsForSymbol = getStockReturnsOneStock(stmt, symbol, startTime, endTime);
            stockReturns.put(symbol, stockReturnsForSymbol);
        }

        return stockReturns;
    }

    private static double[][] calculateCovarianceMatrix(Statement stmt, String[] symbols, Date startTime, Date endTime)
            throws SQLException {
        int numStocks = symbols.length;

        double[][] covarianceMatrix = new double[numStocks][numStocks];

        Map<String, List<Double>> stockReturns = getStockReturns(stmt, symbols, startTime, endTime);

        for (int i = 0; i < numStocks; i++) {
            for (int j = 0; j < numStocks; j++) {
                covarianceMatrix[i][j] = calculateCovariance(stockReturns.get(symbols[i]),
                        stockReturns.get(symbols[j]));
            }
        }

        return covarianceMatrix;
    }

    private static double[][] calculateCorrelationMatrix(double[][] covarianceMatrix) {
        int size = covarianceMatrix.length;
        double[][] correlationMatrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                correlationMatrix[i][j] = covarianceMatrix[i][j]
                        / (Math.sqrt(covarianceMatrix[i][i]) * Math.sqrt(covarianceMatrix[j][j]));
            }
        }

        return correlationMatrix;
    }

    private static void printMatrix(double[][] matrix, String[] symbols) throws SQLException {
        SQLUtilities.printTable(symbols, "Numbers of Stocks", new String[] { "Symbol" });

        System.out.println(
                "Below is the requested matrix for the stocks in the stock list. The order of the stocks in the matrix is the same as the order above.");

        int i = 1;
        for (double[] row : matrix) {
            System.out.printf("%4d |", i);
            for (double val : row) {
                System.out.printf(" %10.8f", val);
            }
            System.out.println();
            i++;
        }

        System.out.println("      " + "-".repeat(11 * symbols.length));
        System.out.print("      ");
        for (i = 1; i <= symbols.length; i++) {
            System.out.printf("%11d", i);
        }
        System.out.println();
    }

}
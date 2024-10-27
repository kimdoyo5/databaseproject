package cs.toronto.edu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Scanner;

import cs.toronto.edu.SQLUtilities;
import cs.toronto.edu.StockListManager;

public class PortfolioManager {

    public static void createPortfolioWithInput(Statement stmt, int loggedInUserID) throws SQLException {
        String portfolioName = SQLUtilities.printPromptGetString("Enter portfolio name (max 30 characters)", 30);

        String sql = SQLUtilities.createSelectStatement("OwnsPortfolio NATURAL JOIN Portfolios", new String[] { "1" },
                "portfolioName = '" + portfolioName + "'");
        ResultSet resultFromPortfoliosOwnsPortfolio = stmt.executeQuery(sql);

        if (SQLUtilities.numOfResults(resultFromPortfoliosOwnsPortfolio) > 0) {
            System.out.println("Portfolio name already exists.");
            return;
        }

        createPortfolio(stmt, loggedInUserID, portfolioName);
    }

    public static void createPortfolio(Statement stmt, int loggedInUserID, String portfolioName) throws SQLException {
        String sql = "INSERT INTO Portfolios(portfolioName) VALUES('" + portfolioName + "') RETURNING portfolioID";
        stmt.execute(sql);
        ResultSet resultFromPortfolios = stmt.getResultSet();
        resultFromPortfolios.next();
        int portfolioID = resultFromPortfolios.getInt(1);

        sql = SQLUtilities.createInsertStatement("OwnsPortfolio", new String[] { "userID", "portfolioID" },
                new String[] { Integer.toString(loggedInUserID), Integer.toString(portfolioID) });
        stmt.executeUpdate(sql);

        int stockListID = StockListManager.createStockList(stmt, loggedInUserID, "Portfolio Stock List",
                "linkedToPortfolio");

        sql = SQLUtilities.createInsertStatement("PortfolioStocks", new String[] { "portfolioID", "stockListID" },
                new String[] { Integer.toString(portfolioID), Integer.toString(stockListID) });
        stmt.executeUpdate(sql);

        System.out.println("Portfolio created successfully.");
    }

    public static void viewPortfolios(Statement stmt, int loggedInUserID) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        String sql = SQLUtilities.createSelectStatement("OwnsPortfolio NATURAL JOIN Portfolios",
                new String[] { "portfolioID", "portfolioName", "cashAvailable" }, "userID = " + loggedInUserID);
        ResultSet resultFromPortfoliosOwnsPortfolio = stmt.executeQuery(sql);

        int numRowsFromPortfoliosOwnsPortfolio = SQLUtilities.numOfResults(resultFromPortfoliosOwnsPortfolio);
        if (numRowsFromPortfoliosOwnsPortfolio == 0) {
            System.out.println("You have no portfolios.");
            return;
        }

        SQLUtilities.printTable(resultFromPortfoliosOwnsPortfolio, "Portfolios",
                new String[] { "Portfolio Name", "Cash Available" }, new String[] { "portfolioName", "cashAvailable" });

        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Select a portfolio",
                "Return to main menu"
        });
        int num;
        int portfolioID;

        switch (choice) {
            case 1:
                num = SQLUtilities.printPromptGetInput("Enter the number of the portfolio you would like to select", 1,
                        numRowsFromPortfoliosOwnsPortfolio);

                resultFromPortfoliosOwnsPortfolio.absolute(num);
                portfolioID = resultFromPortfoliosOwnsPortfolio.getInt("portfolioID");
                managePortfolio(stmt, loggedInUserID, portfolioID);
                break;
            case 2:
                break;
        }
    }

    private static void managePortfolio(Statement stmt, int loggedInUserID, int portfolioID) throws SQLException {
        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Deposit into cash account",
                "Withdraw from cash account",
                "Check cash account balance",
                "Manage stock list",
                "Return to main menu"
        });
        switch (choice) {
            case 1:
                depositIntoCashAccount(stmt, portfolioID);
                break;
            case 2:
                withdrawFromCashAccount(stmt, portfolioID);
                break;
            case 3:
                float balance = getCashAvailable(stmt, portfolioID);
                System.out.println("Your cash account balance is: " + SQLUtilities.formatAsMoney(balance));
                break;
            case 4:
                managePortfolioStockList(stmt, loggedInUserID, portfolioID);
                break;
            case 5:
                return;
        }
    }

    private static void withdrawFromCashAccount(Statement stmt, int portfolioID)
            throws SQLException {
        float cashAvailable = getCashAvailable(stmt, portfolioID);
        if (cashAvailable <= 0) {
            System.out.println("You have no cash available to withdraw.");
            return;
        }

        float amountToWithdraw = SQLUtilities
                .printPromptGetInput("Enter the amount you would like to withdraw (current amount: "
                        + SQLUtilities.formatAsMoney(cashAvailable) + ")", 0, Float.MAX_VALUE);

        if (amountToWithdraw > cashAvailable) {
            System.out.println("You do not have enough cash available to withdraw that amount.");
            return;
        }

        cashAvailable = cashAvailable - amountToWithdraw;
        updateCashAccount(stmt, portfolioID, cashAvailable);
    }

    private static void depositIntoCashAccount(Statement stmt, int portfolioID)
            throws SQLException {
        float cashAvailable = getCashAvailable(stmt, portfolioID);
        float amountToDeposit = SQLUtilities.printPromptGetInput(
                "Enter the amount you would like to deposit (current amount: "
                        + SQLUtilities.formatAsMoney(cashAvailable) + ")",
                0, Float.MAX_VALUE);

        cashAvailable = cashAvailable + amountToDeposit;
        updateCashAccount(stmt, portfolioID, cashAvailable);
    }

    private static void managePortfolioStockList(Statement stmt, int loggedInUserID, int portfolioID)
            throws SQLException {
        int choice = SQLUtilities.printMenuGetInput(new String[] {
                "Add stock to stock list",
                "Remove stock from stock list",
                "View stocks in stock list",
                "View statistics",
                "Return to main menu"
        });
        switch (choice) {
            case 1: // "Add stock to stock list"
                String stockSymbol = SQLUtilities
                        .printPromptGetString("Please enter the stock symbol of the stock you would like to add", 10);
                if (!StockListManager.stockExists(stmt, stockSymbol)) {
                    System.out.println("Stock does not exist.");
                    return;
                }
                int numShares = SQLUtilities.printPromptGetInput("Please enter the number of shares", 1,
                        Integer.MAX_VALUE);
                addStockToPortfolioStockList(stmt, loggedInUserID, portfolioID, stockSymbol, numShares);
                break;
            case 2: // "Remove stock from stock list"
                stockSymbol = SQLUtilities.printPromptGetString(
                        "Please enter the stock symbol of the stock you would like to delete", 10);
                if (!StockListManager.stockExistsInStockList(stmt, stockSymbol,
                        getStockListIDFromPortfolioID(stmt, portfolioID))) {
                    System.out.println("Stock does not exist in stock list.");
                    return;
                }
                removeStockFromPortfolioStockList(stmt, loggedInUserID, portfolioID, stockSymbol);
                break;
            case 3: // "View stocks in stock list"
                StockListManager.viewStocksInStockList(stmt, getStockListIDFromPortfolioID(stmt, portfolioID));
                break;
            case 4: // "View statistics"
                Date startTime = SQLUtilities.printPromptGetDate("Please enter start date (yyyy-MM-dd) (inclusive)");
                Date endTime = SQLUtilities.printPromptGetDate("Please enter end date (yyyy-MM-dd) (inclusive)");

                String sql = SQLUtilities.createSelectStatement("PortfolioStocks", new String[] { "stockListID" },
                        "portfolioID = " + portfolioID);
                ResultSet resultFromPortfolioStocks = stmt.executeQuery(sql);

                resultFromPortfolioStocks.absolute(1);
                int stockListID = resultFromPortfolioStocks.getInt(1);

                StockListManager.viewStatistics(stmt, stockListID, startTime, endTime);
                break;
            case 5: // "Return to main menu"
                return;
        }
    }

    private static float getCashAvailable(Statement stmt, int portfolioID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("Portfolios", new String[] { "cashAvailable" },
                "portfolioID = " + portfolioID);
        ResultSet resultFromPortfolios = stmt.executeQuery(sql);

        resultFromPortfolios.absolute(1);

        float cashAvailable = resultFromPortfolios.getFloat("cashAvailable");

        return cashAvailable;
    }

    private static void updateCashAccount(Statement stmt, int portfolioID, float cashAvailable)
            throws SQLException {
        String sql = "UPDATE Portfolios SET cashAvailable = " + cashAvailable + " WHERE portfolioID = " + portfolioID;
        stmt.executeUpdate(sql);

        System.out.println("Transaction successful. New amount: " + SQLUtilities.formatAsMoney(cashAvailable));
    }

    private static int getStockListIDFromPortfolioID(Statement stmt, int portfolioID) throws SQLException {
        String sql = SQLUtilities.createSelectStatement("PortfolioStocks", new String[] { "stockListID" },
                "portfolioID = " + portfolioID);
        ResultSet resultFromPortfolioStocks = stmt.executeQuery(sql);

        resultFromPortfolioStocks.next();
        int stockListID = resultFromPortfolioStocks.getInt("stockListID");

        return stockListID;
    }

    private static void addStockToPortfolioStockList(Statement stmt, int loggedInUserID, int portfolioID,
            String stockSymbol, int numShares) throws SQLException {
        float cashAvailable = getCashAvailable(stmt, portfolioID);
        float latestClosePrice = StockManager.getLatestClosePrice(stmt, stockSymbol);
        float amountToWithdraw = latestClosePrice * numShares;

        if (amountToWithdraw > cashAvailable) {
            System.out.println("You do not have enough cash available to purchase " + numShares + " shares of "
                    + stockSymbol + ".");
            return;
        }

        cashAvailable = cashAvailable - amountToWithdraw;
        updateCashAccount(stmt, portfolioID, cashAvailable);

        int stockListID = getStockListIDFromPortfolioID(stmt, portfolioID);
        StockListManager.addStockToStockList(stmt, stockListID, stockSymbol, numShares);
    }

    private static int getNumSharesOfStockInStockList(Statement stmt, int stockListID, String stockSymbol)
            throws SQLException {
        String sql = SQLUtilities.createSelectStatement("StockListStocks", new String[] { "numShares" },
                "stockListID = " + stockListID + " AND symbol = '" + stockSymbol + "'");
        ResultSet resultFromStockListStocks = stmt.executeQuery(sql);

        resultFromStockListStocks.next();
        int numShares = resultFromStockListStocks.getInt("numShares");

        return numShares;
    }

    private static void removeStockFromPortfolioStockList(Statement stmt, int loggedInUserID, int portfolioID,
            String stockSymbol) throws SQLException {
        int stockListID = getStockListIDFromPortfolioID(stmt, portfolioID);

        float cashAvailable = getCashAvailable(stmt, portfolioID);
        float latestClosePrice = StockManager.getLatestClosePrice(stmt, stockSymbol);
        int numSharesOfStockInStockList = getNumSharesOfStockInStockList(stmt, stockListID, stockSymbol);
        int numSharesOfStockToSell = SQLUtilities
                .printPromptGetInput(
                        "Please enter the number of shares that you would like to sell for stock " + stockSymbol
                                + ". You have " + numSharesOfStockInStockList + " share(s) of this stock",
                        1, numSharesOfStockInStockList);

        float amountToDeposit = latestClosePrice * numSharesOfStockToSell;

        cashAvailable = cashAvailable + amountToDeposit;
        updateCashAccount(stmt, portfolioID, cashAvailable);

        StockListManager.removeStockFromStockList(stmt, stockListID, stockSymbol);
    }
}
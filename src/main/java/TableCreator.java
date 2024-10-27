package cs.toronto.edu;

import java.sql.SQLException;
import java.sql.Statement;

public class TableCreator {

    // Creates all tables in the database
    public static void createTables(Statement stmt) throws SQLException {
        // stmt.executeUpdate("DROP TABLE IF EXISTS Accounts CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS StockLists CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS Reviews CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS Portfolios CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS Stocks CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS DailyStocks CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS ReviewRequests CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS Friends CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS FriendRequests CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS OwnsPortfolio CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS OwnsStockList CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS AccessibleBy CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS StockListStocks CASCADE");
        // stmt.executeUpdate("DROP TABLE IF EXISTS PortfolioStocks CASCADE");

        createAccountsTable(stmt);
        createStockListsTable(stmt);
        createReviewsTable(stmt);
        createPortfoliosTable(stmt);
        createStocksTable(stmt);
        createDailyStocksTable(stmt);
        createReviewRequestsTable(stmt);
        createFriendsTable(stmt);
        createFriendRequestsTable(stmt);
        createOwnsPortfolioTable(stmt);
        createOwnsStockListTable(stmt);
        createAccessibleByTable(stmt);
        createStockListStocksTable(stmt);
        createPortfolioStocksTable(stmt);
    }

    // Creates Accounts table
    // FD: userID → firstName, lastName, email, password
    // FD: email → userID, firstName, lastName, password
    public static void createAccountsTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS Accounts("
                + "userID SERIAL PRIMARY KEY,"
                + "firstName VARCHAR(30),"
                + "lastName VARCHAR(30),"
                + "email VARCHAR(100) CHECK(email LIKE '_%@_%.__%') UNIQUE,"
                + "password VARCHAR(100)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates StockLists table
    // FD: stockListID → stockListName, category
    public static void createStockListsTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS StockLists("
                + "stockListID SERIAL PRIMARY KEY,"
                + "stockListName VARCHAR(30),"
                + "category VARCHAR(17),"
                + "CHECK (category IN ('private', 'shared', 'public', 'linkedToPortfolio'))"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates Reviews table
    // FD: userID, stockListID → timestamp, response
    public static void createReviewsTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS Reviews("
                + "userID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "stockListID INT REFERENCES StockLists(stockListID) ON DELETE CASCADE,"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "response VARCHAR(4000),"
                + "PRIMARY KEY(userID, stockListID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates Portfolios table
    // FD: portfolioID → portfolioName, cashAvailable
    public static void createPortfoliosTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS Portfolios("
                + "portfolioID SERIAL PRIMARY KEY,"
                + "portfolioName VARCHAR(30),"
                + "cashAvailable FLOAT DEFAULT 0"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates Stocks table
    // FD: symbol → stockName
    public static void createStocksTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS Stocks("
                + "symbol VARCHAR(10) PRIMARY KEY,"
                + "stockName VARCHAR(30)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates DailyStocks table
    // FD: symbol, timestamp → open, close, high, low, volume
    public static void createDailyStocksTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS DailyStocks("
                + "symbol VARCHAR(10) REFERENCES Stocks(symbol),"
                + "timestamp DATE,"
                + "open FLOAT,"
                + "close FLOAT,"
                + "high FLOAT,"
                + "low FLOAT,"
                + "volume INT,"
                + "PRIMARY KEY(symbol, timestamp)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates ReviewRequests table
    // FD: sent, received, stockListID → dateTime
    public static void createReviewRequestsTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS ReviewRequests("
                + "sentUserID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "receivedUserID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "stockListID INT REFERENCES StockLists(stockListID) ON DELETE CASCADE,"
                + "dateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY(sentUserID, receivedUserID, stockListID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates Friends table
    // FD: user1, user2 → status, dateTimeAddedDeleted
    public static void createFriendsTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS Friends("
                + "user1 INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "user2 INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "status VARCHAR(30) DEFAULT 'added',"
                + "dateTimeAddedDeleted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY(user1, user2)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates FriendRequests Table
    // FD: sent, received → status, dateTime
    public static void createFriendRequestsTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS FriendRequests("
                + "sentUserID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "receivedUserID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "status VARCHAR(30) DEFAULT 'pending',"
                + "dateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (sentUserID, receivedUserID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates OwnsPortfolio Table
    // FD: portfolioID → userID
    public static void createOwnsPortfolioTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS OwnsPortfolio("
                + "userID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "portfolioID INT REFERENCES Portfolios(portfolioID) ON DELETE CASCADE,"
                + "PRIMARY KEY(portfolioID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates OwnsStockList Table
    // FD: stockListID → userID
    public static void createOwnsStockListTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS OwnsStockList("
                + "userID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "stockListID INT REFERENCES StockLists(stockListID) ON DELETE CASCADE,"
                + "PRIMARY KEY(stockListID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates AccessibleBy Table
    // No FDs
    public static void createAccessibleByTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS AccessibleBy("
                + "userID INT REFERENCES Accounts(userID) ON DELETE CASCADE,"
                + "stockListID INT REFERENCES StockLists(stockListID) ON DELETE CASCADE,"
                + "PRIMARY KEY(userID, stockListID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates StockListStocks Table
    // FD: stockListID, symbol → numShares
    public static void createStockListStocksTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS StockListStocks("
                + "stockListID INT REFERENCES StockLists(stockListID) ON DELETE CASCADE,"
                + "symbol VARCHAR(10) REFERENCES Stocks(symbol) ON DELETE CASCADE,"
                + "numShares INT,"
                + "PRIMARY KEY(stockListID, symbol)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

    // Creates PortfolioStocks Table
    // FD: portfolioID → stockListID
    public static void createPortfolioStocksTable(Statement stmt) throws SQLException {
        String sqlInsert = "CREATE TABLE IF NOT EXISTS PortfolioStocks("
                + "portfolioID INT REFERENCES Portfolios(portfolioID) ON DELETE CASCADE,"
                + "stockListID INT REFERENCES StockLists(stockListID) ON DELETE CASCADE,"
                + "PRIMARY KEY(portfolioID, stockListID)"
                + ");";
        stmt.executeUpdate(sqlInsert);
    }

}
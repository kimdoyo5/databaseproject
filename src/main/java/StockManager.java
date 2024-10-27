package cs.toronto.edu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

import cs.toronto.edu.SQLUtilities;

public class StockManager {

    public static void viewStockDetails(Statement stmt, String stockSymbol, Date startTime, Date endTime)
            throws SQLException {
        if (startTime.after(endTime)) {
            System.out.println("Error: start date must be before end date.");
            return;
        }

        String sql = "SELECT timestamp, open, close, high, low, volume FROM DailyStocks WHERE symbol = '" + stockSymbol
                + "' AND timestamp >= '" + startTime + "' AND timestamp <= '" + endTime + "' ORDER BY timestamp ASC";
        ResultSet resultFromDailyStocks = stmt.executeQuery(sql);

        SQLUtilities.printTable(resultFromDailyStocks, "Information for " + stockSymbol,
                new String[] { "Date", "Open", "Close", "High", "Low", "Volume" },
                new String[] { "timestamp", "open", "close", "high", "low", "volume" });

        sql = "SELECT close FROM DailyStocks WHERE symbol = '" + stockSymbol + "' AND timestamp >= '" + startTime
                + "' AND timestamp <= '" + endTime + "' ORDER BY timestamp ASC";
        resultFromDailyStocks = stmt.executeQuery(sql);
        int numRowsFromDailyStocks = SQLUtilities.numOfResults(resultFromDailyStocks);

        // Convert close prices to array
        double[] closePrices = new double[numRowsFromDailyStocks + 2];
        int i = 0;
        while (resultFromDailyStocks.next()) {
            closePrices[i] = resultFromDailyStocks.getFloat("close");
            i++;
        }

        double[] futurePredictions = getFuturePredictionOfStock(stmt, stockSymbol, endTime);
        closePrices[numRowsFromDailyStocks] = futurePredictions[0];
        closePrices[numRowsFromDailyStocks + 1] = futurePredictions[1];

        SQLUtilities.printGraph(closePrices, startTime, endTime, "Graph for " + stockSymbol + "s Closing Prices");

        System.out.println("The last two plots represent the future prediction of the stock.");
    }

    private static double[] getFuturePredictionOfStock(Statement stmt, String stockSymbol, Date endTimeOfKnownPrice)
            throws SQLException {
        String sql = "SELECT close FROM DailyStocks WHERE symbol = '" + stockSymbol
                + "' AND timestamp <= '" + endTimeOfKnownPrice + "' ORDER BY timestamp DESC LIMIT 2";
        ResultSet resultFromDailyStocks = stmt.executeQuery(sql);

        resultFromDailyStocks.next();
        double latestClosePrice = resultFromDailyStocks.getFloat("close");
        resultFromDailyStocks.next();
        double secondLatestClosePrice = resultFromDailyStocks.getFloat("close");

        double predictionDay1 = latestClosePrice + (latestClosePrice - secondLatestClosePrice);
        double predictionDay2 = predictionDay1 + (predictionDay1 - latestClosePrice);
        // 5 + (5 - 3) = 7
        // 3 + (3 - 5) = 1

        return new double[] { Math.max(predictionDay1, 0), Math.max(predictionDay2, 0) };
    }

    public static void addStockData(Statement stmt) throws SQLException {
        String stockSymbol = SQLUtilities.printPromptGetString("Enter stock symbol", 10);
        Date timestamp = SQLUtilities.printPromptGetDate("Enter date (yyyy-MM-dd)");

        String sql = SQLUtilities.createSelectStatement("DailyStocks", new String[] { "1" },
                "symbol = '" + stockSymbol + "' AND timestamp = '" + timestamp + "' LIMIT(1)");
        ResultSet resultFromStocks = stmt.executeQuery(sql);

        if (SQLUtilities.numOfResults(resultFromStocks) != 0) {
            System.out.println("Error: stock symbol and date combination already exists.");
            return;
        }

        float open = SQLUtilities.printPromptGetInput("Enter open price", 0, Float.MAX_VALUE);
        float close = SQLUtilities.printPromptGetInput("Enter close price", 0, Float.MAX_VALUE);
        float high = SQLUtilities.printPromptGetInput("Enter high price", 0, Float.MAX_VALUE);
        float low = SQLUtilities.printPromptGetInput("Enter low price", 0, Float.MAX_VALUE);
        int volume = SQLUtilities.printPromptGetInput("Enter volume", 0, Integer.MAX_VALUE);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        sql = SQLUtilities.createInsertStatement("DailyStocks",
                new String[] { "symbol", "timestamp", "open", "close", "high", "low", "volume" },
                new String[] { stockSymbol, dateFormat.format(timestamp), Float.toString(open), Float.toString(close),
                        Float.toString(high), Float.toString(low), Integer.toString(volume) });
        stmt.executeUpdate(sql);

        System.out.println("Stock data added successfully.");
    }

    public static float getLatestClosePrice(Statement stmt, String stockSymbol) throws SQLException {
        String sql = "SELECT close FROM DailyStocks WHERE symbol = '" + stockSymbol
                + "' and timestamp >= ALL(SELECT timestamp FROM DailyStocks WHERE symbol = '" + stockSymbol + "')";
        ResultSet resultFromDailyStocks = stmt.executeQuery(sql);

        resultFromDailyStocks.next();
        float latestClosePrice = resultFromDailyStocks.getFloat("close");

        return latestClosePrice;
    }

}
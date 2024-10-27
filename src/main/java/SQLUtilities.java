package cs.toronto.edu;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;

public class SQLUtilities {

    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    // Delete entry from table
    // DELETE FROM tableName WHERE columns[i] = values[i] AND ...
    public static String createDeleteStatement(String tableName, String[] columns, Object[] values)
            throws SQLException {
        if (columns.length != values.length) {
            throw new IllegalArgumentException("The number of columns and values must match.");
        }

        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableName).append(" WHERE ");

        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]).append(" = ");
            if (values[i] instanceof String) {
                sql.append("'").append(values[i]).append("'");
            } else {
                sql.append(values[i]);
            }
            if (i != columns.length - 1) {
                sql.append(" AND ");
            }
        }

        sql.append(";");
        return sql.toString();
    }

    // Creates an SQL INSERT statement of this form:
    // INSERT INTO tableName(columns) VALUES(values) ON CONFLICT DO NOTHING;
    public static String createInsertStatement(String tableName, String[] columns, String[] values)
            throws SQLException {
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Columns and values must have the same length");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append("(");

        // Append column names
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]);
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }

        sql.append(") VALUES(");

        // Append values
        for (int i = 0; i < values.length; i++) {
            sql.append("'").append(values[i]).append("'");
            if (i < values.length - 1) {
                sql.append(", ");
            }
        }

        sql.append(") ON CONFLICT DO NOTHING;");

        return sql.toString();
    }

    // Creates an SQL SELECT statement of this form:
    // SELECT columns FROM tableName WHERE condition
    public static String createSelectStatement(String tableName, String[] columns, String condition)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ");

        // Append column names
        if (columns == null || columns.length == 0) {
            sql.append("*");
        } else {
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]);
                if (i < columns.length - 1) {
                    sql.append(", ");
                }
            }
        }

        sql.append(" FROM ").append(tableName);

        // Append condition if provided
        if (condition != null && !condition.isEmpty()) {
            sql.append(" WHERE ").append(condition);
        }

        sql.append(";");

        return sql.toString();
    }

    // Creates an SQL UPDATE statement of this form:
    // UPDATE tableName SET columns[i] = values[i], ... WHERE condition
    public static String createUpdateStatement(String tableName, String[] columns, Object[] values, String condition)
            throws SQLException {
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Columns and values must have the same length");
        }

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]);
            sql.append(" = ");
            if (values[i] instanceof String) {
                sql.append("'").append(values[i]).append("'");
            } else {
                sql.append(values[i]);
            }
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }

        // Append condition if provided
        if (condition != null && !condition.isEmpty()) {
            sql.append(" WHERE ").append(condition);
        }

        sql.append(";");
        return sql.toString();
    }

    public static int numOfResults(ResultSet result) throws SQLException {
        result.absolute(0);
        int numRows = 0;
        while (result.next()) {
            numRows++;
        }
        result.absolute(0);
        return numRows;
    }

    private static void printDashes(int numDashes) {
        for (int i = 0; i < numDashes; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    final static int columnSize = 26;

    public static void printTable(ResultSet result, String title, String[] columnNamesHeader,
            String[] columnNamesFromSQL) throws SQLException {
        int numColumns = columnNamesHeader.length + 1;

        printTitle(title, numColumns * (columnSize + 3) + 1);

        // Print column names
        System.out.printf("| %-" + columnSize + "s ", "Number");
        for (int i = 0; i < numColumns - 1; i++) {
            System.out.printf("| %-" + columnSize + "s ", columnNamesHeader[i]);
        }
        System.out.printf("|%n");

        printDashes(numColumns * (columnSize + 3) + 1);

        // Print table rows
        int rowNum = 1;
        while (result.next()) {
            System.out.printf("| %-" + columnSize + "s ", rowNum);
            for (int i = 0; i < numColumns - 1; i++) {
                System.out.printf("| %-" + columnSize + "s ", result.getString(columnNamesFromSQL[i]));
            }
            System.out.printf("|%n");
            rowNum++;
        }

        printDashes(numColumns * (columnSize + 3) + 1);

        result.absolute(0);
    }

    public static void printTableLastColumnSeparateRow(ResultSet result, String title, String[] columnNamesHeader,
            String[] columnNamesFromSQL) throws SQLException {
        int numColumns = columnNamesHeader.length;

        printTitle(title, numColumns * (columnSize + 3) + 1);

        // Print column names
        System.out.printf("| %-" + columnSize + "s ", "Number");
        for (int i = 0; i < numColumns - 1; i++) {
            System.out.printf("| %-" + columnSize + "s ", columnNamesHeader[i]);
        }
        System.out.printf("|%n");

        printDashes(numColumns * (columnSize + 3) + 1);

        // Print table rows
        int rowNum = 1;
        while (result.next()) {
            System.out.printf("| %-" + columnSize + "s ", rowNum);
            for (int i = 0; i < numColumns - 1; i++) {
                System.out.printf("| %-" + columnSize + "s ", result.getString(columnNamesFromSQL[i]));
            }
            System.out.printf("|%n");
            System.out.printf("  %-50s%n",
                    columnNamesHeader[numColumns - 1] + ":\n  " + result.getString(columnNamesFromSQL[numColumns - 1]));
            rowNum++;
        }

        printDashes(numColumns * (columnSize + 3) + 1);

        result.absolute(0);
    }

    public static void printTable(String[][] result, String title, String[] columnNamesHeader) throws SQLException {
        int numColumns = columnNamesHeader.length + 1;

        printTitle(title, numColumns * (columnSize + 3) + 1);

        // Print column names
        System.out.printf("| %-" + columnSize + "s ", "Number");
        for (int i = 0; i < numColumns - 1; i++) {
            System.out.printf("| %-" + columnSize + "s ", columnNamesHeader[i]);
        }
        System.out.printf("|%n");

        printDashes(numColumns * (columnSize + 3) + 1);

        // Print table rows
        for (int i = 0; i < result.length; i++) {
            System.out.printf("| %-" + columnSize + "s ", i + 1);
            for (int j = 0; j < result[i].length; j++) {
                System.out.printf("| %-" + columnSize + "s ", result[i][j]);
            }
            System.out.printf("|%n");
        }

        printDashes(numColumns * (columnSize + 3) + 1);
    }

    public static void printTable(String[] result, String title, String[] columnNamesHeader) throws SQLException {
        int numColumns = columnNamesHeader.length + 1;

        printTitle(title, numColumns * (columnSize + 3) + 1);

        // Print column names
        System.out.printf("| %-" + columnSize + "s ", "Number");
        for (int i = 0; i < numColumns - 1; i++) {
            System.out.printf("| %-" + columnSize + "s ", columnNamesHeader[i]);
        }
        System.out.printf("|%n");

        printDashes(numColumns * (columnSize + 3) + 1);

        // Print table rows
        for (int i = 0; i < result.length; i++) {
            System.out.printf("| %-" + columnSize + "s ", i + 1);
            System.out.printf("| %-" + columnSize + "s ", result[i]);
            System.out.printf("|%n");
        }

        printDashes(numColumns * (columnSize + 3) + 1);
    }

    private static void printTitle(String title, int dashLength) {
        System.out.println();
        printDashes(dashLength);
        System.out.println("  " + Colours.ANSI_BLUEGREEN + Colours.ANSI_BOLD + title + Colours.ANSI_RESET);
        printDashes(dashLength);
    }

    private static void printTitle(String title) {
        System.out.println();
        printDashes(30);
        System.out.println("  " + Colours.ANSI_BLUEGREEN + Colours.ANSI_BOLD + title + Colours.ANSI_RESET);
        printDashes(30);
    }

    // Prints menu from which user selects an option
    public static int printMenuGetInput(String[] menuOptions) {
        printTitle("Options");

        for (int i = 0; i < menuOptions.length; i++) {
            System.out.printf("| %-2d | %-30s%n", i + 1,
                    Colours.ANSI_BLUEGREEN + Colours.ANSI_BOLD + menuOptions[i] + Colours.ANSI_RESET);
        }

        printDashes(30);

        int choice = printPromptGetInput("Enter your choice", 1, menuOptions.length);

        return choice;
    }

    // Prompts user to enter a number
    public static int printPromptGetInput(String prompt, int min, int max) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(Colours.ANSI_BLUEGRAY + Colours.ANSI_BOLD + prompt + ":" + Colours.ANSI_RESET);

        while (true) {
            int input = scanner.nextInt();
            if (input >= min && input <= max) {
                return input;
            } else {
                System.out.println("Enter a valid number.");
            }
        }
    }

    public static float printPromptGetInput(String prompt, float min, float max) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(Colours.ANSI_BLUEGRAY + Colours.ANSI_BOLD + prompt + ":" + Colours.ANSI_RESET);

        while (true) {
            float input = scanner.nextFloat();
            if (input >= min && input <= max) {
                return input;
            } else {
                System.out.println("Enter a valid number.");
            }
        }
    }

    // Prompts user to enter a string
    public static String printPromptGetString(String prompt, int max) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(Colours.ANSI_BLUEGRAY + Colours.ANSI_BOLD + prompt + ":" + Colours.ANSI_RESET);

        while (true) {
            String input = scanner.nextLine();
            if (input.length() <= max) {
                return input;
            } else {
                System.out.println("The string is too long.");
            }
        }
    }

    // Prompts user to enter a date
    public static Date printPromptGetDate(String prompt) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(Colours.ANSI_BLUEGRAY + Colours.ANSI_BOLD + prompt + ":" + Colours.ANSI_RESET);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // Ensure strict date parsing

        while (true) {
            String dateStr = scanner.nextLine();
            try {
                Date date = dateFormat.parse(dateStr);
                return date;
            } catch (ParseException e) {
                System.out.println("Not a valid date. Please enter in the format yyyy-MM-dd.");
            }
        }
    }

    public static void printGraph(double[] closePrices, Date startTime, Date endTime, String title) {
        // *************
        // Resource:
        // https://stackoverflow.com/questions/73666050/plot-graph-in-console-by-printing-special-character-say-and-spaces-using-matri
        // *************

        printTitle(title);

        int numRows = 20;

        double maxY = Arrays.stream(closePrices).max().getAsDouble();
        double minY = Arrays.stream(closePrices).min().getAsDouble();

        double minYToShow = minY;
        double yTick = (maxY - minYToShow) / numRows;
        // minYToShow = minY >= yTick ? yTick : minY;

        double[] yAxis = new double[numRows + 1];
        yAxis[0] = minYToShow;
        for (int i = 1; i < numRows + 1; i++) {
            yAxis[i] = minYToShow + (yTick * i);
        }

        for (int i = yAxis.length - 1; i >= 0; i--) {
            System.out.printf("%6.2f | ", yAxis[i]);
            for (double val : closePrices) {
                if (i != yAxis.length - 1 && val >= yAxis[i] && val < yAxis[i + 1]) {
                    System.out.print(Colours.ANSI_CYAN + " * " + Colours.ANSI_RESET);
                } else if (i == yAxis.length - 1 && val == yAxis[i]) {
                    System.out.print(Colours.ANSI_CYAN + " * " + Colours.ANSI_RESET);
                } else {
                    System.out.print("   ");
                }
            }
            System.out.println();
        }

        System.out.println("        " + "-".repeat(3 * closePrices.length));
        System.out.print("        ");
        for (int i = 1; i <= closePrices.length; i++) {
            System.out.printf("%3d", i);
        }
        System.out.println();
    }

    public static String formatAsMoney(double money) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(money);
    }

}
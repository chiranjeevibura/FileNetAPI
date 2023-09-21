import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import java.util.HashSet;
import java.util.Set;

public class DatabaseCSVComparison {

    private static final String ORACLE_JDBC_URL = "jdbc:oracle:thin:@//hostname:port/servicename";
    private static final String ORACLE_USERNAME = "your_oracle_username";
    private static final String ORACLE_PASSWORD = "your_oracle_password";

    private static final String SQL_JDBC_URL = "jdbc:sqlserver://hostname:port;databaseName=dbname";
    private static final String SQL_USERNAME = "your_sql_username";
    private static final String SQL_PASSWORD = "your_sql_password";

    public static void main(String[] args) {
        DatabaseConnector connector = new DatabaseConnector();
        try {
            // Establish Oracle and SQL Server connections
            Connection oracleConnection = connector.getOracleConnection();
            Connection sqlConnection = connector.getSqlConnection();

            // Execute SQL queries
            QueryExecutor queryExecutor = new QueryExecutor();

            // Example Oracle query
            String oracleQuery = "SELECT loan_number, column2, column3 FROM oracle_table";
            ResultSet oracleResultSet = queryExecutor.executeQuery(oracleConnection, oracleQuery);

            // Example SQL Server query
            String sqlQuery = "SELECT loan_number, column2, column3 FROM sql_table";
            ResultSet sqlResultSet = queryExecutor.executeQuery(sqlConnection, sqlQuery);

            // Write Oracle ResultSet to CSV
            CSVWriter csvWriter = new CSVWriter();
            csvWriter.writeResultSetToCSV(oracleResultSet, "oracle_output.csv");

            // Write SQL ResultSet to CSV
            csvWriter.writeResultSetToCSV(sqlResultSet, "sql_output.csv");

            // Compare CSV column values and generate a CSV with unique records
            CSVComparator csvComparator = new CSVComparator();
            csvComparator.compareAndWriteUniqueRecords("oracle_output.csv", "sql_output.csv", "unique_records.csv");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    static class DatabaseConnector {
        private Connection oracleConnection;
        private Connection sqlConnection;

        public DatabaseConnector() {
            try {
                // Initialize Oracle connection
                Class.forName("oracle.jdbc.driver.OracleDriver");
                oracleConnection = DriverManager.getConnection(ORACLE_JDBC_URL, ORACLE_USERNAME, ORACLE_PASSWORD);

                // Initialize SQL Server connection
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                sqlConnection = DriverManager.getConnection(SQL_JDBC_URL, SQL_USERNAME, SQL_PASSWORD);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }

        public Connection getOracleConnection() {
            return oracleConnection;
        }

        public Connection getSqlConnection() {
            return sqlConnection;
        }
    }

    static class QueryExecutor {
        public ResultSet executeQuery(Connection connection, String sqlQuery) throws SQLException {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            return preparedStatement.executeQuery();
        }
    }

    static class CSVWriter {
        public void writeResultSetToCSV(ResultSet resultSet, String fileName) throws IOException, SQLException {
            FileWriter fileWriter = new FileWriter(fileName);
            try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {
                // Write column headers
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    csvPrinter.print(resultSet.getMetaData().getColumnName(i));
                }
                csvPrinter.println();

                // Write data
                while (resultSet.next()) {
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        csvPrinter.print(resultSet.getString(i));
                    }
                    csvPrinter.println();
                }
            }
        }
    }

    static class CSVComparator {
        public void compareAndWriteUniqueRecords(String inputFileName1, String inputFileName2, String outputFileName) throws IOException {
            Set<String> uniqueRecords = new HashSet<>();

            try (CSVParser csvParser1 = CSVParser.parse(new FileReader(inputFileName1), CSVFormat.DEFAULT);
                 CSVParser csvParser2 = CSVParser.parse(new FileReader(inputFileName2), CSVFormat.DEFAULT);
                 FileWriter fileWriter = new FileWriter(outputFileName);
                 CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {

                for (var record : csvParser1) {
                    String loanNumber = record.get(0); // Assuming the loan number is in the first column
                    if (!uniqueRecords.contains(loanNumber)) {
                        uniqueRecords.add(loanNumber);
                        csvPrinter.printRecord(record);
                    }
                }

                for (var record : csvParser2) {
                    String loanNumber = record.get(0); // Assuming the loan number is in the first column
                    if (!uniqueRecords.contains(loanNumber)) {
                        uniqueRecords.add(loanNumber);
                        csvPrinter.printRecord(record);
                    }
                }
            }
        }
    }
}

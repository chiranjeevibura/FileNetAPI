import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class QuerypostgreSQL {

    private static final String DB_URL = "jdbc:postgresql://localhost:5433/os1db";
    private static final String USERNAME = " ";
    private static final String PASSWORD = " ";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();

            // Example queries
            String[] queries = {
                    "select count(d.object_id) from docversion d, classdefinition c where c.object_id = d.object_class_id and c.symbolic_name = 'IS_1' ",
                    "select count(d.object_id) from docversion d, classdefinition c where c.object_id = d.object_class_id and c.symbolic_name = 'P8_1' "

            };

            for (String query : queries) {
                ResultSet resultSet = statement.executeQuery(query);
                String csvFilePath = "C:\\Work\\postgresql_query_results.csv";
                writeResultSetToCSV(resultSet, csvFilePath);
                //System.out.println("Query results for '" + query + "' written to: " + csvFilePath);
                int count = resultSet.getInt(1);
                //System.out.println("Query result count: " + count);

                System.out.println("Query results for '" + query + "is" + count + "' written to: " + csvFilePath);
                resultSet.close();
            }

            statement.close();
            connection.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeResultSetToCSV(ResultSet resultSet, String filePath) throws SQLException, IOException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        try (FileWriter writer = new FileWriter(filePath)) {
            // Write the column headers
            for (int i = 1; i <= columnCount; i++) {
                writer.append(metaData.getColumnName(i));
                if (i < columnCount) {
                    writer.append(",");
                }
            }
            writer.append("\n");

            // Write the data rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(resultSet.getString(i));
                    if (i < columnCount) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
        }
    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileNetDBQueryMT {
    public static void main(String[] args) {
		String connURL = "jdbc:oracle:thin:@fsfsd:1529:AVSPfasdfsd8CPT";
		String connUName = "fsadfsd";
		String connPwd = "fasdfsdf";


        // Prepare the SQL queries
        String[] queries = {
            "SELECT COUNT(*) FROM DocVersion dv, ClassDefinition cd " +
            "WHERE dv.object_class_Id = cd.object_Id AND cd.SYMBOLIC_NAME = 'IS' " +
            "AND dv.create_date >= TO_DATE('2010-03-01 18:30:00', 'YYYY-MM-DD HH24:MI:SS') " +
            "AND dv.create_date <= TO_DATE('2023-05-31 18:15:00', 'YYYY-MM-DD HH24:MI:SS')",
            // Add more queries here
            "SELECT COUNT(*) FROM DocVersion dv, ClassDefinition cd " +
            "WHERE dv.object_class_Id = cd.object_Id AND cd.SYMBOLIC_NAME = 'IS' " +
            "AND dv.create_date >= TO_DATE('2010-03-01 18:30:00', 'YYYY-MM-DD HH24:MI:SS') " +
            "AND dv.create_date <= TO_DATE('2023-05-31 18:15:00', 'YYYY-MM-DD HH24:MI:SS')",
            "SELECT COUNT(*) FROM DocVersion dv, ClassDefinition cd " +
            "WHERE dv.object_class_Id = cd.object_Id AND cd.SYMBOLIC_NAME = 'IS' " +
            "AND dv.create_date >= TO_DATE('2010-03-01 18:30:00', 'YYYY-MM-DD HH24:MI:SS') " +
            "AND dv.create_date <= TO_DATE('2023-05-31 18:15:00', 'YYYY-MM-DD HH24:MI:SS')"
        };

        // Create an array to store the threads
        Thread[] threads = new Thread[queries.length];

        try (Connection connection = DriverManager.getConnection(connURL, connUName, connPwd)) {
            // Execute each query in a separate thread
            for (int i = 0; i < queries.length; i++) {
                final int index = i;
                final String sqlQuery = queries[index];

                // Create a new thread for each query
                threads[index] = new Thread(() -> {
                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            int count = resultSet.getInt(1);
                            System.out.println("Count of IDs for Query " + (sqlQuery) + ": " + count);
                        }

                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                // Start the thread
                threads[index].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

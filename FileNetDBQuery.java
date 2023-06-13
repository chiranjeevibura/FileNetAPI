import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileNetDBQuery {
    public static void main(String[] args) {

		String connURL = "jdbc:oracle:thin:@fsafsd:1529:fdafds";
		String connUName = "fsdaf";
		String connPwd = "fasdfsd";

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

        try (Connection connection = DriverManager.getConnection(connURL, connUName, connPwd)) {

            for (int i = 0; i < queries.length; i++) {
                String sqlQuery = queries[i];

                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    System.out.println("Count of IDs for Query " + (i + 1) + ": " + count);
                }

                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
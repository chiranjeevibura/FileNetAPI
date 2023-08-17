import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class QueryPostgreSQLMT {

    private static final String DB_URL = "jdbc:postgresql://localhost:5433/os1db";
    private static final String USERNAME = "ceuserp";
    private static final String PASSWORD = "^@396v^J0Rpt";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();

            // Example queries
            String[] queries = {
            		/*"select 'IS_DC_APCHECKS' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_APCHECKS' ",
                    "select 'IS_DC_CASH' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CASH' ",
					"select 'IS_DC_CASHB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CASHB' ",
                    "select 'IS_DC_CASHP' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CASHP' ",
					"select 'IS_DC_CHARGE' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CHARGE' ",
                    "select 'IS_DC_CHARGEB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CHARGEB' ",
					"select 'IS_DC_CRA_ABG' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CRA_ABG' ",
					"select 'IS_DC_CRA_B' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CRA_B' ",
					"select 'IS_DC_CREDITT' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_CREDITT' ",
                    "select 'IS_DC_ECB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_ECB' ",
					"select 'IS_DC_ERECEIPT_A' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_ERECEIPT_A' ",
                    "select 'IS_DC_ERECEIPT_B' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_ERECEIPT_B' ",
					"select 'IS_DC_ESig_A' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_ESig_A' ",
                    "select 'IS_DC_ESig_B' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_ESig_B' ",
					"select 'IS_DC_MRA' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MRA' ",
                    "select 'IS_DC_MRAB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MRAB' ",
					"select 'IS_DC_MVAV' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MVAV' ",
                    "select 'IS_DC_MVAVDCP' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MVAVDCP' ",*/
					"select 'IS_DC_MVAVDCT' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MVAVDCT' ",
                    "select 'IS_DC_SHARED' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_SHARED' ",
					"select 'IS_DC_MVAVDC' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MVAVDC' ",
                    "select 'IS_DC_MVAVDCB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'IS_DC_MVAVDCB' ",
					"select 'P8_CREDITCLUB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_CREDITCLUB' ",
                    "select 'P8_ESIGMRA' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_ESIGMRA' ",
					"select 'P8_ESIGMRAB' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_ESIGMRAB' ",
                    "select 'P8_ESig_P' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_ESig_P' ",
					"select 'P8_Fax' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_Fax' ",
                    "select 'P8_RA_BTRUCK' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_RA_BTRUCK' ",
					"select 'P8_RA_BTWREPORT' as query, count(d.object_id) as count " + "from docversion d, classdefinition c " + "where c.object_id = d.object_class_id and c.symbolic_name = 'P8_RA_BTWREPORT' "
					
            };

            String csvFilePath = "C:\\Work\\postgresql_query_results_8152023_2.csv";
            writeQueriesToCSV(queries, statement, csvFilePath);

            statement.close();
            connection.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeQueriesToCSV(String[] queries, Statement statement, String filePath) throws SQLException, IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Query,Count\n");

            for (String query : queries) {
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    String queryName = resultSet.getString("query");
                    int count = resultSet.getInt("count");
                    writer.append(queryName).append(",").append(String.valueOf(count)).append("\n");
                    System.out.println("Query results for '" + queryName + "' written to: " + filePath);
                }
                resultSet.close();
            }
        }
    }
}

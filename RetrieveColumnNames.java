import com.filenet.api.core.*;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.filenet.api.admin.*;
import com.filenet.api.collection.*;
import com.filenet.api.util.*;


public class RetrieveColumnNames {
    public static void main(String[] args) {
        String uri = " ";
        String username =  
        String password =  
 
		
		String sourceObjectStoreName = " ";

        try {
        	// Connect to the source domain
			Connection sourceConnection = Factory.Connection.getConnection(uri);
			UserContext sourceUserContext = UserContext.get();
			sourceUserContext.pushSubject(
					UserContext.createSubject(sourceConnection, username, password, "FileNetP8WSI"));

			// Get the source domain
			Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

			// Print the source domain connection status
			System.out.println("Source Domain connected: True");
			System.out.println("Source Domain name: " + sourceDomain.get_Name());

			// Get the source object store
			ObjectStore sourceObjectStore = Factory.ObjectStore.fetchInstance(sourceDomain, sourceObjectStoreName, null);
			System.out.println("Source Object Store: " + sourceObjectStore.get_DisplayName());
            
            // Get the ClassDefinition for "DocVersion"
            String classDefName = "DocVersion";
            ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(sourceObjectStore, classDefName, null);

            // Retrieve the PropertyDefinitionList for "DocVersion"
            PropertyDefinitionList propDefList = classDef.get_PropertyDefinitions();

            // Retrieve the database column names for each property
            for (Object propDefObj : propDefList) {
                PropertyDefinition propDef = (PropertyDefinition) propDefObj;
                String columnName = getDatabaseColumnName(propDef);
                System.out.println("Column name: " + columnName);
            }

            // Disconnect from the FileNet domain
            UserContext.get().popSubject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDatabaseColumnName(PropertyDefinition propDef) {
        String columnName = null;
        try {
            // Establish a JDBC connection to the FileNet database
   /*         String jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE";
            String jdbcUsername = "your_db_username";
            String jdbcPassword = "your_db_password";*/
            
    		String jdbcUrl = "jdbc:oracle:thin:@161.178.235.166:1529:AVSP8CPT";
    		String jdbcUsername = "CPEABGCS01";
    		String jdbcPassword = "avsP9dba";
    		
            java.sql.Connection dbConnection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);

            // Retrieve the database column name using the property symbol
            String propertySymbol = propDef.get_SymbolicName();
            String query = "SELECT COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'DOCVERSION' AND COMMENTS = '" + propertySymbol + "'";
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Get the column name from the result set
            if (resultSet.next()) {
                columnName = resultSet.getString("COLUMN_NAME");
            }

            // Close the database resources
            resultSet.close();
            statement.close();
           // dbConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnName;
    }
}

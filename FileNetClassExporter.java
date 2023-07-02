import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.security.auth.Subject;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.collection.ClassDefinitionSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

public class FileNetClassExporter {

    public static void main(String[] args) {
        // Connection parameters
    	String uri = " ";
		String username = " ";
		String password = " ";
		String objectStoreName = " ";

        // CSV file path
        String csvFilePath = "classes.csv";

        try {
  /*          // Connect to FileNet domain
            Connection connection = Factory.Connection.getConnection(uri);
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);*/

            // Connect to the source domain
            Connection connection = Factory.Connection.getConnection(uri);
            Subject subject = UserContext.createSubject(connection, username, password, null);
            UserContext.get().pushSubject(subject);

            // Get the source domain
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);

            // Print the source domain connection status
            System.out.println("Source Domain connected: True");
            System.out.println("Source Domain name: " + domain.get_Name());

            // Get the source object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
            System.out.println("Source Object Store: " + objectStore.get_DisplayName());

         
            // Retrieve document classes
            SearchScope searchScope = new SearchScope(objectStore);
            String sql = "SELECT [Id] FROM [Document]";
            SearchSQL searchSQL = new SearchSQL(sql);
            ClassDefinitionSet classDefinitionSet = (ClassDefinitionSet) searchScope.fetchObjects(searchSQL, null, null,
                    Boolean.FALSE);

            // Write classes to CSV file
            writeClassesToCSV(classDefinitionSet, csvFilePath);

            // Disconnect from FileNet domain
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeClassesToCSV(ClassDefinitionSet classDefinitionSet, String csvFilePath) throws IOException {
        FileWriter writer = new FileWriter(csvFilePath);

        // Write header
        writer.append("SymbolicName,GUID\n");

/*        // Iterate over classes
        Iterator<?> iterator = classDefinitionSet.iterator();
        while (iterator.hasNext()) {
            RepositoryRow row = (RepositoryRow) iterator.next();
            Properties properties = row.getProperties();
            String symbolicName = properties.getStringValue("SymbolicName");
            String guid = row.getProperties().getIdValue("Id").toString();

            // Write row
            writer.append(symbolicName)
                    .append(",")
                    .append(guid)
                    .append("\n");
        }*/
        // Iterate over classes
        Iterator iterator = classDefinitionSet.iterator();
        while (iterator.hasNext()) {
            com.filenet.api.admin.ClassDefinition classDefinition = (com.filenet.api.admin.ClassDefinition) iterator.next();
            String symbolicName = classDefinition.get_SymbolicName();
            String guid = classDefinition.get_Id().toString();

            // Write row
            writer.append(symbolicName)
                    .append(",")
                    .append(guid)
                    .append("\n");
        }
        writer.flush();
        writer.close();

        System.out.println("Classes exported to CSV file: " + csvFilePath);
    }
}

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.security.auth.Subject;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;
import com.filenet.api.core.*;
import com.filenet.api.admin.*;


public class extractPropDefDataType {

	public static void main(String[] args) {
		String uri = "https:// /wsi/FNCEWS40MTOM/";
		String username = " ";
		String password = " ";
		String objectStoreName = " ";
		String documentClassName = " ";
		String csvFilePath = "C:\\Work\\propdata.csv";

		try {
			// Connect to the source domain
			Connection sourceConnection = Factory.Connection.getConnection(uri);
			Subject subject = UserContext.createSubject(sourceConnection, username, password, null);
			UserContext.get().pushSubject(subject);

			// Get the source domain
			Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

			// Print the source domain connection status
			System.out.println("Source Domain connected: True");
			System.out.println("Source Domain name: " + sourceDomain.get_Name());

			// Get the source object store
			ObjectStore objectStore = Factory.ObjectStore.fetchInstance(sourceDomain, objectStoreName, null);
			System.out.println("Source Object Store: " + objectStore.get_DisplayName());

			// Retrieve the document class
			ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(objectStore, documentClassName, null);

			// Retrieve the property definitions for the document class
			PropertyDefinitionList propDefList = classDef.get_PropertyDefinitions();

			// Create the CSV file and write the headers
			FileWriter csvWriter = new FileWriter(csvFilePath);
			csvWriter.append("Property Name,Data Type,Fast2Import\n");

			// Iterate over property definitions and add them to the CSV
			for (Iterator<?> it = propDefList.iterator(); it.hasNext();) {
				PropertyDefinition propDef = (PropertyDefinition) it.next();
				csvWriter.append(propDef.get_SymbolicName()).append(",").append(propDef.get_DataType().toString()).append("\n");
		}
			csvWriter.flush();
			csvWriter.close();

			System.out.println("Property export completed successfully.");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			UserContext.get().popSubject();
		}
	}
}

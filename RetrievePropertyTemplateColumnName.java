import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.filenet.api.core.*;
import com.filenet.api.admin.*;
import com.filenet.api.collection.*;
import com.filenet.api.util.*;
import com.filenet.api.*;


public class RetrievePropertyTemplateColumnName {
	public static void main(String[] args) {
		// Assuming you have the ObjectStore and PropertyTemplate ID available
		String sourceUsername = " ";
		String sourcePassword = " ";
		String sourceURL = " ";
		String sourceObjectStoreName = " ";
		String propertyTemplateId = "{8E938B00-0301-4223-8EC6-2EA6D4060959}";
		/*
		 * Property Template not present in Target Object Store:
		 * EmsTemplateLocale Property Template not present in Target Object
		 * Store: {8E938B00-0301-4223-8EC6-2EA6D4060959} Property Template not
		 * present in Target Object Store: Template Locale
		 */

		try {
        	// Connect to the source domain
			Connection sourceConnection = Factory.Connection.getConnection(sourceURL);
			UserContext sourceUserContext = UserContext.get();
			sourceUserContext.pushSubject(
					UserContext.createSubject(sourceConnection, sourceUsername, sourcePassword, "FileNetP8WSI"));

			// Get the source domain
			Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

			// Print the source domain connection status
			System.out.println("Source Domain connected: True");
			System.out.println("Source Domain name: " + sourceDomain.get_Name());

			// Get the source object store
			ObjectStore sourceObjectStore = Factory.ObjectStore.fetchInstance(sourceDomain, sourceObjectStoreName, null);
			System.out.println("Source Object Store: " + sourceObjectStore.get_DisplayName());

			// Get the PropertyTemplate by ID
			Id propertyTemplateIdObj = new Id(propertyTemplateId);
			PropertyTemplate propertyTemplate = Factory.PropertyTemplate.fetchInstance(sourceObjectStore,
					propertyTemplateIdObj, null);

			// Get the column name
            PropertyDescription propertyDescription = propertyTemplate.pro
            String columnName = propertyDescription.get_ColumnName();
			   
			System.out.println("Column Name: " + columnName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

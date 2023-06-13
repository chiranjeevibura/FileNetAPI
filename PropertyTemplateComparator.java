import com.filenet.api.core.*;
import com.filenet.api.admin.*;
import com.filenet.api.collection.*;
import com.filenet.api.util.*;

import java.util.Iterator;

public class PropertyTemplateComparator {

    public static void main(String[] args) {
        String sourceUsername = " ";
		String sourcePassword = " ";
 
		String sourceURL = "https:// /wsi/FNCEWS40MTOM/";
		String targetUsername = " ";
		String targetPassword = " ";
 
		String targetURL = "https:// /wsi/FNCEWS40MTOM/";
		String sourceObjectStoreName = " ";
		String targetObjectStoreName = " ";
        
       /* // Connect to FileNet
        Connection conn = Factory.Connection.getConnection("yourP8ConnectionURI");
        UserContext.get().pushSubject(UserContext.createSubject(conn, "username", "password", "yourJAASStanza"));
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

			// Connect to the target domain
			Connection targetConnection = Factory.Connection.getConnection(targetURL);
			UserContext targetUserContext = UserContext.get();
			targetUserContext.pushSubject(
					UserContext.createSubject(targetConnection, targetUsername, targetPassword, "FileNetP8WSI"));

			// Get the target domain
			Domain targetDomain = Factory.Domain.fetchInstance(targetConnection, null, null);

			// Print the target domain connection status
			System.out.println("Target Domain connected: True");
			System.out.println("Target Domain name: " + targetDomain.get_Name());

			// Get the target object store
			ObjectStore targetObjectStore = Factory.ObjectStore.fetchInstance(targetDomain, targetObjectStoreName, null);
			System.out.println("Target Object Store: " + targetObjectStore.get_DisplayName());
			
            // Fetch property templates in Object Store 1
            PropertyTemplateSet propertyTemplateSet1 = sourceObjectStore.get_PropertyTemplates();

            // Fetch property templates in Object Store 2
            PropertyTemplateSet propertyTemplateSet2 = targetObjectStore.get_PropertyTemplates();

         // Get an iterator for property templates in Object Store 1
            Iterator<?> propertyTemplateIterator = propertyTemplateSet1.iterator();
            int missingCount = 0;
         // Iterate over property templates in Object Store 1
            while (propertyTemplateIterator.hasNext()) {
                PropertyTemplate propertyTemplate = (PropertyTemplate) propertyTemplateIterator.next();

                // Check if the property template is present in Object Store 2
                boolean isPropertyTemplatePresent = isPropertyTemplatePresent(propertyTemplateSet2, propertyTemplate);
                
                if (!isPropertyTemplatePresent) {
                	missingCount ++;
                    System.out.println("Property Template not present in Target Object Store: " + propertyTemplate.get_SymbolicName());
                    System.out.println("Property Template not present in Target Object Store: " + propertyTemplate.get_Id());
                    System.out.println("Property Template not present in Target Object Store: " + propertyTemplate.get_DisplayName());
                }
                
            }
            System.out.println("Count of Property Template not present in Target Object Store: " + missingCount);
        } finally {
            UserContext.get().popSubject();
            //conn.close();
        }
    }
    
    private static boolean isPropertyTemplatePresent(PropertyTemplateSet propertyTemplateSet, PropertyTemplate propertyTemplate) {
        Iterator<?> propertyTemplateIterator = propertyTemplateSet.iterator();

        while (propertyTemplateIterator.hasNext()) {
            PropertyTemplate template = (PropertyTemplate) propertyTemplateIterator.next();
            if (template.get_SymbolicName().equals(propertyTemplate.get_SymbolicName())) {
                return true;
            }
        }

        return false;
    }
}

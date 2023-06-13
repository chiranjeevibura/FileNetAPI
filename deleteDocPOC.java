import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AccessLevel;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.security.AccessPermission;

@SuppressWarnings("deprecation")
public class deleteDocPOC {

    private static StringBuilder reconciliationReport;

    public static void main(String[] args) {

    	String sourceUsername = " ";
		String sourcePassword = " ";
		String sourceURL = "https:// /wsi/FNCEWS40MTOM/";

		String targetUsername = " ";
		String targetPassword = " ";
		String targetURL = "https:// /wsi/FNCEWS40MTOM/";

		String sourceObjectStoreName = " ";
		String targetObjectStoreName = " ";


        String filePath = "C:\\Work\\MissingIDs.txt";

        reconciliationReport = new StringBuilder();

        // Create a connection to the source FileNet domain
        try {
            // Connect to the source domain
            Connection sourceConnection = Factory.Connection.getConnection(sourceURL);
            UserContext sourceUserContext = UserContext.get();
            sourceUserContext.pushSubject(
                    UserContext.createSubject(sourceConnection, sourceUsername, sourcePassword, "FileNetP8WSI"));

            // Get the source domain
            Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

            // Print the source domain connection status
            reconciliationReport.append("Source Domain connected: True\n");
            reconciliationReport.append("Source Domain name: ").append(sourceDomain.get_Name()).append("\n");

            // Get the source object store
            ObjectStore sourceObjectStore = Factory.ObjectStore.fetchInstance(sourceDomain, sourceObjectStoreName,
                    null);
            reconciliationReport.append("Source Object Store: ").append(sourceObjectStore.get_DisplayName())
                    .append("\n");

            // Connect to the target domain
            Connection targetConnection = Factory.Connection.getConnection(targetURL);
            UserContext targetUserContext = UserContext.get();
            targetUserContext.pushSubject(
                    UserContext.createSubject(targetConnection, targetUsername, targetPassword, "FileNetP8WSI"));

            // Get the target domain
            Domain targetDomain = Factory.Domain.fetchInstance(targetConnection, null, null);

            // Print the target domain connection status
            reconciliationReport.append("Target Domain connected: True\n");
            reconciliationReport.append("Target Domain name: ").append(targetDomain.get_Name()).append("\n");

            // Get the target object store
            ObjectStore targetObjectStore = Factory.ObjectStore.fetchInstance(targetDomain, targetObjectStoreName,
                    null);
            reconciliationReport.append("Target Object Store: ").append(targetObjectStore.get_DisplayName())
                    .append("\n");

            // Compare document IDs and set permissions on missing IDs in the target environment
            compareAndSetPermissions(sourceObjectStore, targetObjectStore, filePath);

            // Pop the user context from the target domain
            targetUserContext.popSubject();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Compare document IDs and set permissions on missing IDs in the target environment
    private static void compareAndSetPermissions(ObjectStore sourceObjectStore, ObjectStore targetObjectStore, String filePath) {
        try {
            // Create a search scope to search for documents in the source object store
            SearchScope searchScope = new SearchScope(sourceObjectStore);

            // Create a searchSQL to retrieve all document IDs in the source object store
            String searchSQL = "SELECT * FROM P8_Recon_Test";

            // Create the search object using the search scope and search SQL
            SearchSQL search = new SearchSQL(searchSQL);
            RepositoryRowSet rowSet = searchScope.fetchRows(search, null, null, true);

            // Iterate through the search results and compare the document IDs in the target object store
            Iterator<?> iterator = rowSet.iterator();
            int missingCount = 0;
            while (iterator.hasNext()) {
                RepositoryRow row = (RepositoryRow) iterator.next();
                Id documentId = row.getProperties().getIdValue("Id");

                // Check if the document ID exists in the target object store
                if (!documentExistsInTarget(targetObjectStore, documentId)) {
                    // Document ID is missing in the target environment
                    missingCount++;
                    // Write the missing document ID to the file
                    writeMissingDocumentIdToFile(filePath, documentId);
                    // Set permissions for the missing document ID in the target environment
                    setPermissionsOnMissingDocument(sourceObjectStore, documentId);
                }
            }

            // Print the missing document IDs count
            System.out.println("Missing Document IDs count: " + missingCount);

        } catch (Exception e) {
            System.out.println("An error occurred during document ID comparison and permission setting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Check if a document ID exists in the target object store
    private static boolean documentExistsInTarget(ObjectStore targetObjectStore, Id documentId) {
        try {
            // Fetch the document from the target object store
            Document targetDoc = Factory.Document.fetchInstance(targetObjectStore, documentId, null);
            return true;
        } catch (Exception e) {
            // Document not found
            return false;
        }
    }

    // Write missing document ID to a file
    private static void writeMissingDocumentIdToFile(String filePath, Id documentId) {
        try {
            FileWriter writer = new FileWriter(filePath, true);
            writer.write(documentId.toString() + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing missing document ID to the file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Set permissions on a missing document in the target object store
    @SuppressWarnings("unchecked")
	private static void setPermissionsOnMissingDocument(ObjectStore sourceObjectStore, Id documentId) {
        try {
            // Fetch the missing document from the target object store
            Document sourceDocument = Factory.Document.fetchInstance(sourceObjectStore, documentId, null);

            // Remove existing permissions
            AccessPermissionList permissionList = sourceDocument.get_Permissions();
            permissionList.clear();

            // Set permissions for the admin group
            AccessPermission permission = Factory.AccessPermission.createInstance();
            permission.set_GranteeName("p8padmin");
            permission.set_AccessType(AccessType.ALLOW);
            permission.set_InheritableDepth(0);
            //permission.set_InheritableDepth(new Integer(0)); // this object only
            //permission.set_InheritableDepth(new Integer(-1));this object and all children
            //permission.set_InheritableDepth(new Integer(1)); this object and immediate children

            
            /* if (permission.equalsIgnoreCase("FULL_CONTROL")) {
                permission.set_AccessMask(new Integer(AccessLevel.FULL_CONTROL_DOCUMENT_AS_INT));
                //permission.set_AccessMask(AccessRight.MAJOR_VERSION_AS_INT);
            }
            if (permission.equalsIgnoreCase("READ_ONLY")) {
                permission.set_AccessMask(new Integer(AccessLevel.VIEW_AS_INT));
            }
            if (strArray[1].equalsIgnoreCase("MODIFY_PROPERTIES")) {
                permission.set_AccessMask(new Integer(AccessLevel.WRITE_DOCUMENT_AS_INT));
            }
            if (strArray[1].equalsIgnoreCase("MAJOR_VERSIONING")) {
                permission.set_AccessMask(new Integer(AccessLevel.MAJOR_VERSION_DOCUMENT_AS_INT));
            }
*/
            // Set the access mask value
            //permission.set_AccessMask(new Integer(AccessLevel.VIEW_AS_INT));
            permission.set_AccessMask(new Integer(AccessLevel.FULL_CONTROL_DOCUMENT_AS_INT));

            permissionList.add(permission);
            sourceDocument.save(RefreshMode.REFRESH);
            
        } catch (Exception e) {
            System.out.println("An error occurred while setting permissions on missing document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

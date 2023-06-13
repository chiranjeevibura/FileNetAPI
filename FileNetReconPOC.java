import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.security.AccessPermission;

public class FileNetReconPOC {

    private static StringBuilder reconciliationReport;

    public static void main(String[] args) {
        String sourceUsername = " ";
        String sourcePassword = " ";
        String sourceURL = " ";

        String targetUsername = " ";
        String targetPassword = " ";
        String targetURL = "";

        String sourceObjectStoreName = " ";
        String targetObjectStoreName = " ";

        String filePath = "C:\\Work\\ReconciliationReport.txt";

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

            // Perform validations
            validateData(sourceObjectStore, targetObjectStore);
            validatePermissions(sourceObjectStore, targetObjectStore);

            // Generate reconciliation report
            generateReconciliationReport(filePath);

            // Pop the user context from the target domain
            targetUserContext.popSubject();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Perform validations
    private static void validateData(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
        System.out.println("Performing data validation...");

        // Create a search scope to search for documents in the source object store
        SearchScope searchScope = new SearchScope(sourceObjectStore);

        // Create a searchSQL to retrieve all documents in the source object store
        String searchSQL = "SELECT * FROM P8_Recon_Test";

        // Create the search object using the search scope and search SQL
        SearchSQL search = new SearchSQL(searchSQL);
        RepositoryRowSet rowSet = searchScope.fetchRows(search, null, null, true);

        // Iterate through the search results and compare the document content in the target object store
        Iterator<?> iterator = rowSet.iterator();
        while (iterator.hasNext()) {
            RepositoryRow row = (RepositoryRow) iterator.next();
            Properties properties = row.getProperties();
            Document sourceDoc = (Document) row.getProperties().getObjectValue("This");
            System.out.println("Data validation for document: " + sourceDoc.get_Id());
            // Check if the document exists in the target object store
            if (documentExistsInTarget(targetObjectStore, sourceDoc.get_Id())) {
                // Fetch the corresponding document from the target object store
                Document targetDoc = Factory.Document.fetchInstance(targetObjectStore, sourceDoc.get_Id(), null);

                // Compare the document security
                compareAndRemoveSecurity(sourceDoc, targetDoc);
            } else {
                System.out.println("Document not found in the target object store: " + sourceDoc.get_Id());
                // Perform necessary actions when the document is not found in the target object store
            }
        }
    }

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

    // Compare and remove security on differential or additional GUIDs in the target document
    private static void compareAndRemoveSecurity(Document sourceDoc, Document targetDoc) {
        try {
            // Get the source document's GUID
            String sourceGUID = sourceDoc.get_Id().toString();

            // Get the target document's GUID
            String targetGUID = targetDoc.get_Id().toString();

            if (!sourceGUID.equals(targetGUID)) {
                System.out.println("Document GUID mismatch for document: " + sourceGUID);
                // Perform necessary actions when the GUIDs do not match

                // Remove all security on the target document
                AccessPermissionList targetPermissions = targetDoc.get_Permissions();
                targetPermissions.clear();
                targetDoc.save(RefreshMode.REFRESH);

                // Set only admin group for permission on the target document
                AccessPermission adminPermission = Factory.AccessPermission.createInstance();
                adminPermission.set_GranteeName("admin");
                adminPermission.set_AccessMask(AccessRight.WRITE.getValue() | AccessRight.READ.getValue());
                adminPermission.set_InheritableDepth(1);

                targetPermissions.add(adminPermission);
                targetDoc.save(RefreshMode.REFRESH);
       
                
                System.out.println("Security updated for document: " + sourceGUID);
            }
        } catch (Exception e) {
            System.out.println("Error occurred while comparing and removing security: " + e.getMessage());
        }
    }

    // Validate permissions for documents
    private static void validatePermissions(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
        reconciliationReport.append("\nPermissions Validation:\n");

        // Get the total document count in the object store
        SearchSQL sql = new SearchSQL();
        sql.setQueryString("SELECT Id FROM P8_Recon_Test");

        SearchScope searchScope = new SearchScope(sourceObjectStore);
        RepositoryRowSet rowSet = searchScope.fetchRows(sql, null, null, new Boolean(true));

        int mismatchCount = 0;

        Iterator<RepositoryRow> iterator = rowSet.iterator();

        while (iterator.hasNext()) {
            RepositoryRow row = iterator.next();
            Id documentId = row.getProperties().getIdValue("Id");

            // Get the document by ID from the source object store
            Document sourceDocument = Factory.Document.fetchInstance(sourceObjectStore, documentId, null);

            // Get the document by ID from the target object store
            Document targetDocument = Factory.Document.fetchInstance(targetObjectStore, documentId, null);

            // Compare the access permissions of the documents
            if (!comparePermissions(sourceDocument.get_Permissions(), targetDocument.get_Permissions())) {
                reconciliationReport.append("Permissions mismatch for Document: ").append(documentId).append("\n");
                mismatchCount++;
            }
        }

        if (mismatchCount == 0) {
            reconciliationReport.append("Permissions validation successful. The access permissions match in both object stores for all documents.\n");
        } else {
            reconciliationReport.append("Permissions validation failed. The access permissions do not match in both object stores for ").append(mismatchCount).append(" document(s).\n");
        }
    }

    // Compare access permissions of two documents
    private static boolean comparePermissions(AccessPermissionList sourcePermissions, AccessPermissionList targetPermissions) {
        // Compare the number of permissions
        if (sourcePermissions.size() != targetPermissions.size()) {
            return false;
        }

        Iterator<?> sourceIterator = sourcePermissions.iterator();

        while (sourceIterator.hasNext()) {
            AccessPermission sourcePermission = (AccessPermission) sourceIterator.next();

            // Check if the permission exists in the target permissions list
            if (!targetPermissions.contains(sourcePermission)) {
                return false;
            }
        }

        return true;
    }

    // Generate the reconciliation report and write it to a file
    private static void generateReconciliationReport(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(reconciliationReport.toString());
            writer.close();

            System.out.println("Reconciliation report generated and saved to: " + filePath);
        } catch (IOException e) {
            System.out.println("Error occurred while generating the reconciliation report: " + e.getMessage());
        }
    }
}

//The code provided above modifies the `validateData` method to include the comparison and removal of security for documents with differential or additional GUIDs in the target environment. The `compareAndRemoveSecurity` method is introduced to handle this functionality. The security is removed from the target document, and only the admin group is set for permissions. The code also includes the necessary method calls and modifications to the existing code to incorporate these changes.

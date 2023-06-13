import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

import com.filenet.api.util.*;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.security.AccessPermission;

public class FileNetReconciliationMT {

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
		
		String filePath = "C:\\Work\\ReconciliationReport.txt";


        reconciliationReport = new StringBuilder();

        // Create a connection to the source FileNet domain
        try {
            // Connect to the source domain
            Connection sourceConnection = Factory.Connection.getConnection(sourceURL);
            UserContext sourceUserContext = UserContext.get();
            sourceUserContext.pushSubject(UserContext.createSubject(sourceConnection, sourceUsername, sourcePassword, "FileNetP8WSI"));

            // Get the source domain
            Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

            // Print the source domain connection status
            reconciliationReport.append("Source Domain connected: True\n");
            reconciliationReport.append("Source Domain name: ").append(sourceDomain.get_Name()).append("\n");

            // Get the source object store
            ObjectStore sourceObjectStore = Factory.ObjectStore.fetchInstance(sourceDomain, sourceObjectStoreName, null);
            reconciliationReport.append("Source Object Store: ").append(sourceObjectStore.get_DisplayName()).append("\n");

            // Connect to the target domain
            Connection targetConnection = Factory.Connection.getConnection(targetURL);
            UserContext targetUserContext = UserContext.get();
            targetUserContext.pushSubject(UserContext.createSubject(targetConnection, targetUsername, targetPassword, "FileNetP8WSI"));

            // Get the target domain
            Domain targetDomain = Factory.Domain.fetchInstance(targetConnection, null, null);

            // Print the target domain connection status
            reconciliationReport.append("Target Domain connected: True\n");
            reconciliationReport.append("Target Domain name: ").append(targetDomain.get_Name()).append("\n");

            // Get the target object store
            ObjectStore targetObjectStore = Factory.ObjectStore.fetchInstance(targetDomain, targetObjectStoreName, null);
            reconciliationReport.append("Target Object Store: ").append(targetObjectStore.get_DisplayName()).append("\n");

            // Perform validations
           // validateData(sourceObjectStore, targetObjectStore);
            validateMetadata(sourceObjectStore, targetObjectStore);
           // performSearch(sourceObjectStore, targetObjectStore);
           // validatePermissions(sourceObjectStore, targetObjectStore);

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
            reconciliationReport.append("Data Validation:\n");

            // Get the total document count in the source object store
            int sourceDocumentCount = getDocumentCount(sourceObjectStore);
            reconciliationReport.append("Source Document Count: ").append(sourceDocumentCount).append("\n");

            // Get the total document count in the target object store
            int targetDocumentCount = getDocumentCount(targetObjectStore);
            reconciliationReport.append("Target Document Count: ").append(targetDocumentCount).append("\n");

            if (sourceDocumentCount == targetDocumentCount) {
                reconciliationReport.append("Data validation successful. The document count matches in both object stores.\n");
            } else {
                reconciliationReport.append("Data validation failed. The document count does not match in both object stores.\n");
            }
        }

        // Get the total document count in the object store
        private static int getDocumentCount(ObjectStore objectStore) {
            SearchSQL sql = new SearchSQL();
            sql.setQueryString("SELECT * FROM IS_DC_CASHP");

            SearchScope searchScope = new SearchScope(objectStore);
/*            private static void printSearchResults(RepositoryRowSet rowSet) {
                Iterator<RepositoryRow> iterator = rowSet.iterator();

                while (iterator.hasNext()) {
                    RepositoryRow row = iterator.next();
                    Id documentId = row.getProperties().getIdValue("Id");
                    String documentName = row.getProperties().getStringValue("Name");

                    reconciliationReport.append("Document ID: ").append(documentId).append(", Name: ").append(documentName).append("\n");
                }
            }*/

            RepositoryRowSet rowSet = searchScope.fetchRows(sql, null, null, new Boolean(true));
            Iterator<RepositoryRow> iterator = rowSet.iterator();
            if (iterator.hasNext()) {
                RepositoryRow row = iterator.next();
                Properties properties = row.getProperties();
                Integer count = (Integer) properties.get("Count").getObjectValue();
                return count.intValue();
            }

            return 0;
        }

        // Validate metadata of documents
        private static void validateMetadata(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
            reconciliationReport.append("\nMetadata Validation:\n");

            // Get the total document count in the object store
            //int documentCount = getDocumentCount(sourceObjectStore);

            SearchSQL sql = new SearchSQL();
            sql.setQueryString("SELECT Id FROM IS_DC_CASHP");

            SearchScope searchScope = new SearchScope(sourceObjectStore);
            RepositoryRowSet rowSet = searchScope.fetchRows(sql, null, null, new Boolean(true));
            Iterator<RepositoryRow> iterator = rowSet.iterator();
            int mismatchCount = 0;

            while (iterator.hasNext()) {
                RepositoryRow row = iterator.next();
                Id documentId = row.getProperties().getIdValue("Id");

                // Get the document by ID from the source object store
                Document sourceDocument = Factory.Document.fetchInstance(sourceObjectStore, documentId, null);

                // Get the document by ID from the target object store
                Document targetDocument = Factory.Document.fetchInstance(targetObjectStore, documentId, null);

                // Compare the metadata properties of the documents
                if (!compareMetadataProperties(sourceDocument.getProperties(), targetDocument.getProperties())) {
                    reconciliationReport.append("Metadata mismatch for Document: ").append(documentId).append("\n");
                    mismatchCount++;
                }
            }

            if (mismatchCount == 0) {
                reconciliationReport.append("Metadata validation successful. The metadata properties match in both object stores for all documents.\n");
            } else {
                reconciliationReport.append("Metadata validation failed. The metadata properties do not match in both object stores for ").append(mismatchCount).append(" document(s).\n");
            }
        }

        // Compare metadata properties of two documents
        private static boolean compareMetadataProperties(Properties sourceProperties, Properties targetProperties) {
            Iterator<?> sourceIterator = sourceProperties.iterator();

            while (sourceIterator.hasNext()) {
                Property sourceProperty = (Property) sourceIterator.next();

                if (!sourceProperty.getPropertyName().equals("Id")) {
                    Property targetProperty = targetProperties.get(sourceProperty.getPropertyName());

                    if (targetProperty == null || !sourceProperty.getObjectValue().equals(targetProperty.getObjectValue())) {
                        return false;
                    }
                }
            }

            return true;
        }
    

    // Perform search for documents in the object stores
    private static void performSearch(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
        reconciliationReport.append("\nSearch Results:\n");

        // Set the search criteria
        SearchSQL sql = new SearchSQL();
        sql.setQueryString("SELECT Id, Name FROM Document WHERE PropertyName = 'PropertyValue'");

        // Perform the search in the source object store
        SearchScope sourceSearchScope = new SearchScope(sourceObjectStore);
        RepositoryRowSet sourceRowSet = sourceSearchScope.fetchRows(sql, null, null, new Boolean(true));

        // Perform the search in the target object store
        SearchScope targetSearchScope = new SearchScope(targetObjectStore);
        RepositoryRowSet targetRowSet = targetSearchScope.fetchRows(sql, null, null, new Boolean(true));

        reconciliationReport.append("Documents in Source Object Store:\n");
        printSearchResults(sourceRowSet);

        reconciliationReport.append("\nDocuments in Target Object Store:\n");
        printSearchResults(targetRowSet);
    }

    // Print the search results
/*    private static void printSearchResults(RepositoryRowSet rowSet) {
        while (rowSet.hasNext()) {
            RepositoryRow row = rowSet.next();
            Id documentId = row.getProperties().getIdValue("Id");
            String documentName = row.getProperties().getStringValue("Name");

            reconciliationReport.append("Document ID: ").append(documentId).append(", Name: ").append(documentName).append("\n");
        }
    }*/
    
 // Print the search results
    private static void printSearchResults(RepositoryRowSet rowSet) {
        Iterator<RepositoryRow> iterator = rowSet.iterator();

        while (iterator.hasNext()) {
            RepositoryRow row = iterator.next();
            Id documentId = row.getProperties().getIdValue("Id");
            String documentName = row.getProperties().getStringValue("Name");

            reconciliationReport.append("Document ID: ").append(documentId).append(", Name: ").append(documentName).append("\n");
        }
    }


    // Validate permissions for documents
    /*private static void validatePermissions(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
        reconciliationReport.append("\nPermissions Validation:\n");

        // Get the total document count in the object store
        int documentCount = getDocumentCount(sourceObjectStore);

        SearchSQL sql = new SearchSQL();
        sql.setQueryString("SELECT Id FROM Document");

        SearchScope searchScope = new SearchScope(sourceObjectStore);
        RepositoryRowSet rowSet = searchScope.fetchRows(sql, null, null, new Boolean(true));

        int mismatchCount = 0;

        while (rowSet.hasNext()) {
            RepositoryRow row = rowSet.next();
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
    }*/
 // Validate permissions for documents
    private static void validatePermissions(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
        reconciliationReport.append("\nPermissions Validation:\n");

        // Get the total document count in the object store
        int documentCount = getDocumentCount(sourceObjectStore);

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
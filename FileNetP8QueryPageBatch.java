import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

import java.util.Iterator;

import javax.security.auth.Subject;

public class FileNetP8QueryPageBatch {
	
    public static void main(String[] args) {

        
    	String connURL = "https://server.abc.com:9443/wsi/FNCEWS40MTOM/";
        String connUName = "admin";
        String connPwd = "fdsafsd";
        String objectStoreName = "ABOS01";


        // Prepare the queries
        String[] queries = {

        		"SELECT [This],[Id] from IS_ABC WHERE [DateCreated] >= 20210301T183000Z AND [DateCreated] <= 20230228T181500Z AND [Fast2Import] is null"

        		};

        Connection connection = Factory.Connection.getConnection(connURL);
        Subject subject = UserContext.createSubject(connection, connUName, connPwd, null);
        UserContext.get().pushSubject(subject);

        try {
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

            Boolean continuable = new Boolean(true);
            Integer pageSize = new Integer(1000); // Adjust the page size based on your system's capacity

            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];

                SearchSQL searchSQL = new SearchSQL(query);
                SearchScope searchScope = new SearchScope(objectStore);

                int batchCount = 0;
                RepositoryRowSet rowSet;
                do {
                    // Fetch the document objects using the search in batches
                    rowSet = searchScope.fetchRows(searchSQL, pageSize, null, continuable);
                    long count = 0;
                    Iterator<?> iterator = rowSet.iterator();
                    while (iterator.hasNext()) {
                        iterator.next();
                        count++;
                    }
                    System.out.println("Batch " + batchCount + " Count: " + count);
                    batchCount++;
                } while (rowSet.hasNextPage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }
}

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

public class FileNetP8Query {
    public static void main(String[] args) {

        String connURL = " ";
        String connUName = " ";
        String connPwd = " ";
        String objectStoreName = " ";

        // Prepare the queries
        String[] queries = {
                "SELECT This, Id FROM IS_DC_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230531T181500Z",
                // Add more queries here
                "SELECT This, Id FROM IS_DC_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230531T181500Z",
                "SELECT This, Id FROM IS_DC_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230511T181500Z"
        };

        Connection connection = Factory.Connection.getConnection(connURL);
        Subject subject = UserContext.createSubject(connection, connUName, connPwd, null);
        UserContext.get().pushSubject(subject);

        try {
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
            
            Boolean continuable = new Boolean(true);
            Integer pageSize = new Integer(10000);
            
            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];

                SearchSQL searchSQL = new SearchSQL(query);
                SearchScope searchScope = new SearchScope(objectStore);
              
                
             // Fetch the document objects using the search
                RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, pageSize, null, continuable);

        		long count = 0;
    			Iterator <?> iterator = rowSet.iterator();
    			while (iterator.hasNext()) {
    				iterator.next();
    				count++;
    			}
         
    			System.out.println("Count of IDs for Query " + (i + 1) + ": " + count);

            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }
}

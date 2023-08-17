import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.security.auth.Subject;

public class FileNetP8QueryReconcilation {
    public static void main(String[] args) {

    	String connURL1 = "https://ui.prod.filenet.abg.cloud/wsi/FNCEWS40MTOM/";
    	String connURL2 = "https://b3psavp8cp01.avisbudget.com:9443/wsi/FNCEWS40MTOM/";
        //String connURL2 = "https://ui.prod.filenet.abg.cloud/wsi/FNCEWS40MTOM/";
        String connUName = "p8padmin";
        String connPwd = "p8fnadm";
        String objectStoreName = "ABGPRODCS01";
        
        
/*        String connURL1 = "https://b3usavp8cp01.avisbudget.com:9443/wsi/FNCEWS40MTOM/";
        String connURL2 = "https://ui.preprod.filenet.abg.cloud/wsi/FNCEWS40MTOM/";
        String connUName = "p8padmin";
        String connPwd = "p8fnadm";
        String objectStoreName = "ABGUATCS01";*/

        // Prepare the queries
        String[] queries = {
                "SELECT [This],[Id] from IS_DC_APCHECKS WHERE [DateCreated] >= 20210301T183000Z AND [DateCreated] < 20210401T181500Z",
                "SELECT [This],[Id] from IS_DC_CASH WHERE [DateCreated] >= 20210301T183000Z AND [DateCreated] <= 20210401T181500Z"
        };

        try {
            FileWriter csvWriter = new FileWriter("output_prod.csv");
            csvWriter.append("Query,Connection 1,Connection 2,Comparison\n");

            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];

                Connection connection1 = Factory.Connection.getConnection(connURL1);
                Connection connection2 = Factory.Connection.getConnection(connURL2);

                Subject subject1 = UserContext.createSubject(connection1, connUName, connPwd, null);
                Subject subject2 = UserContext.createSubject(connection2, connUName, connPwd, null);

                UserContext.get().pushSubject(subject1);
                UserContext.get().pushSubject(subject2);

                Domain domain1 = Factory.Domain.fetchInstance(connection1, null, null);
                // Print the domain connection status
                System.out.println("Domain connected: true");
                System.out.println("Domain name: " + domain1.get_Name());
                
                Domain domain2 = Factory.Domain.fetchInstance(connection2, null, null);
                // Print the domain connection status
                System.out.println("Domain connected: true");
                System.out.println("Domain name: " + domain2.get_Name());
                
                ObjectStore objectStore1 = Factory.ObjectStore.fetchInstance(domain1, objectStoreName, null);
                ObjectStore objectStore2 = Factory.ObjectStore.fetchInstance(domain2, objectStoreName, null);

                Boolean continuable = new Boolean(true);
                Integer pageSize = new Integer(10000);

                SearchSQL searchSQL = new SearchSQL(query);
                SearchScope searchScope1 = new SearchScope(objectStore1);
                SearchScope searchScope2 = new SearchScope(objectStore2);

                RepositoryRowSet rowSet1 = searchScope1.fetchRows(searchSQL, pageSize, null, continuable);
                RepositoryRowSet rowSet2 = searchScope2.fetchRows(searchSQL, pageSize, null, continuable);

                long count1 = 0;
                long count2 = 0;

                Iterator<?> iterator1 = rowSet1.iterator();
                while (iterator1.hasNext()) {
                    iterator1.next();
                    count1++;
                }
                System.out.println("Number of documents Before deletion: " + count1);
                
                Iterator<?> iterator2 = rowSet2.iterator();
                while (iterator2.hasNext()) {
                    iterator2.next();
                    count2++;
                }
                System.out.println("Number of documents Before deletion: " + count2);

                String comparison = count1 == count2 ? "Matched" : "Difference: " + (count2 - count1);

                csvWriter.append(query + "," + count1 + "," + count2 + "," + comparison + "\n");

                UserContext.get().popSubject();
                UserContext.get().popSubject();

          /*      connection1.close();
                connection2.close();*/
            }

            csvWriter.flush();
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

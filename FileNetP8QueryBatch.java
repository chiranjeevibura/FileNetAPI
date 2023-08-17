import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

import javax.security.auth.Subject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Iterator;

public class FileNetP8QueryBatch {
    public static void main(String[] args) {

    	String connURL = "https:// /wsi/FNCEWS40MTOM/";
        String connUName = " ";
        String connPwd = " ";
        String objectStoreName = " ";
        
        // Prepare the queries
        String[] queries = {
                "SELECT [This],[Id] from IS_1 WHERE [Fast2Import] is null"
        };

        Connection connection = Factory.Connection.getConnection(connURL);
        Subject subject = UserContext.createSubject(connection, connUName, connPwd, null);
        UserContext.get().pushSubject(subject);

        try {
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

            Boolean continuable = true;
            Integer pageSize = 10000;

            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];

                // Split the date range into smaller batches
                String startTimestamp = "20220901T183000Z"; // Replace with actual start timestamp
                String endTimestamp = "20230301T181500Z"; // Replace with actual end timestamp

                long batchSize = 86400000L; // 24 hours in milliseconds

                long start = parseTimestamp(startTimestamp);
                long end = parseTimestamp(endTimestamp);

                for (long batchStart = start; batchStart < end; batchStart += batchSize) {
                    long batchEnd = Math.min(batchStart + batchSize, end);

                    String formattedQuery = query + " AND [DateCreated] >= '" + formatTimestamp(batchStart) + "' AND [DateCreated] < '" + formatTimestamp(batchEnd) + "'";

                    SearchSQL searchSQL = new SearchSQL(formattedQuery);
                    SearchScope searchScope = new SearchScope(objectStore);

                    // Fetch the document objects using the search
                    RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, pageSize, null, continuable);

                    long count = 0;
                    Iterator<?> iterator = rowSet.iterator();
                    while (iterator.hasNext()) {
                        iterator.next();
                        count++;
                    }

                    System.out.println("Count of IDs for Query " + (i + 1) + ": " + formattedQuery + ">>>>>:>>>>> " + count);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

    // Helper method to format the timestamp as required by your database
    private static String formatTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(timestamp));
    }

    // Helper method to parse the timestamp string into a long value
    private static long parseTimestamp(String timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = dateFormat.parse(timestamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

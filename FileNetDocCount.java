import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.filenet.apiimpl.jdbc.PreparedStatement;
import com.filenet.apiimpl.jdbc.ResultSet;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.core.Connection;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.Subject;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileNetDocCount {


    public static void main(String[] args) throws ParseException {


        // the filenet username and password
        String username = " ";
        String password = " ";

        // FileNet connection properties
        String url = "https:///wsi/FNCEWS40MTOM/";
        String documentClass = "P8_Test"; // FileNet document class
        String OSName = " "; // FileNet object store name
        
 
        
        //Retrieving the count for a date range:
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-31");

 

        // Perform FileNet API query
        int fileNetCount = performFileNetQuery(username, password, url, documentClass, OSName, startDate, endDate);

        // Perform direct Oracle database query
        int oracleCount = performOracleQuery(startDate, endDate);

        // Display the counts
        System.out.println("FileNet API Query Count: " + fileNetCount);
        System.out.println("Oracle SQL Query Count: " + oracleCount);
    }


    // Perform FileNet API query and return the count
    private static int performFileNetQuery(String username, String password, String url, String documentClass,
                                           String OSName, Date startDate, Date endDate) {
        //int count = 0;
        String certificatePath = "C:\\Work\\fnuat.crt";

        // Create a connection to the FileNet domain
        try {
            // Load the SSL certificate
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream fileInputStream = new FileInputStream(certificatePath);
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
            fileInputStream.close();

            // Create a KeyStore and add the certificate
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("fileNetCertificate", certificate);

            // Create a TrustManager that trusts the certificate
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // Create an SSL context with the TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            Connection connection = Factory.Connection.getConnection(url);
            try {
                // Establish the user context
                Subject subject = UserContext.createSubject(connection, username, password, null);
                UserContext.get().pushSubject(subject);

                // Get the default domain
                Domain domain = Factory.Domain.fetchInstance(connection, null, null);

                // Get the object store
                ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, OSName, null);

                // Format the dates for the query


                // Define the search query with the formatted dates
                String searchSQL = "SELECT * FROM YourDocumentClass WHERE DateProperty >= '" + startDate + "' AND DateProperty <= '" + endDate + "'";
                SearchScope searchScope = new SearchScope(objectStore);
                SearchSQL searchSQLObject = new SearchSQL(searchSQL);
                RepositoryRowSet rowSet = searchScope.fetchRows(searchSQLObject, null, null, false);

             // Get the count of the search results 
                int count = 0;
                for (RepositoryRow row : rowSet) { 
                    count++;
                }
                
                System.out.println("Number of documents found: " + count);

                UserContext.get().popSubject();
            } catch (EngineRuntimeException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    // Perform direct Oracle database query and return the count
    private static int performOracleQuery(Date startDate, Date endDate) {
    	int count = 0;

        // JDBC connection parameters
        String jdbcUrl = "jdbc:oracle:thin:@//161.178.235.166:1529/AVSP8CPT"; // Update with your Oracle database URL
        String username = "CPEABGCS01"; // Update with your Oracle database username
        String password = "avsP9dba"; // Update with your Oracle database password

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
        	 // Connect to the Oracle database
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Prepare the SQL query
            String sqlQuery = "SELECT COUNT(*) FROM your_table WHERE date_created >= ? AND date_created <= ?";
            statement = connection.prepareStatement(sqlQuery);
            statement.setTimestamp(1, new Timestamp(startDate.getTime()));
            statement.setTimestamp(2, new Timestamp(endDate.getTime()));

            // Execute the query
            resultSet = statement.executeQuery();

            // Retrieve the count from the result set
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the database resources
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }
}

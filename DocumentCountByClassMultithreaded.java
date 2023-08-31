import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.filenet.api.admin.ClassDefinition;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

public class DocumentCountByClassMultithreaded {

    public static void main(String[] args) {
        String uri = "https://your_p8_server/wsi/FNCEWS40MTOM/";
        String username = "your_username";
        String password = "your_password";
        String objectStoreName = "your_objectstore";
        List<String> classNames = new ArrayList<>();
        classNames.add("Class1");
        classNames.add("Class2");
        // Add more class names as needed
        
        String csvFilePath = "C:\\Work\\document_count_per_class.csv";

        Connection connection = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(connection, username, password, null);
        UserContext.get().pushSubject(subject);

        try {
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

            FileWriter csvWriter = new FileWriter(csvFilePath);
            csvWriter.append("Domain,ObjectStore,Class,Count\n");

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (String className : classNames) {
                executor.submit(() -> processClass(className, objectStore, csvWriter));
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            csvWriter.flush();
            csvWriter.close();

            System.out.println("Document count per class export completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

    private static void processClass(String className, ObjectStore objectStore, FileWriter csvWriter) {
        try {
            ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(objectStore, className, null);

            String query = "SELECT COUNT(*) FROM " + className + " WHERE [RecordTriggerDate] IS NULL";
            SearchSQL searchSQL = new SearchSQL(query);
            SearchScope searchScope = new SearchScope(objectStore);

            int pageSize = 1000;
            int totalDocumentCount = 0;

            RepositoryRowSet rowSet;
            do {
                rowSet = searchScope.fetchRows(searchSQL, pageSize, null, Boolean.TRUE);
                totalDocumentCount += rowSet.size();
            } while (rowSet.hasNextPage());

            synchronized (csvWriter) {
                csvWriter.append(objectStore.get_Domain().get_Name()).append(",");
                csvWriter.append(objectStore.get_DisplayName()).append(",");
                csvWriter.append(className).append(",");
                csvWriter.append(String.valueOf(totalDocumentCount)).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

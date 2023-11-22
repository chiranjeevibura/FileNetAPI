import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileNetUpdater {

    public static void main(String[] args) {
        Properties config = loadConfigProperties();
        String directoryPath = config.getProperty("directoryPath");
        int threadPoolSize = Integer.parseInt(config.getProperty("threadPoolSize"));

        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        try {
            while (true) {
                File directory = new File(directoryPath);
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") && !name.endsWith("_completed.txt"));

                if (files != null && files.length > 0) {
                    for (File file : files) {
                        executorService.submit(() -> processFile(file, config));
                    }
                }

                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private static void processFile(File file, Properties config) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] accountNumbers = line.split(" ");
                String oldAccountNumber = accountNumbers[0];
                String newAccountNumber = accountNumbers[1];

                Connection connection = Factory.Connection.getConnection(config.getProperty("connectionURI"));
                Domain domain = Factory.Domain.getInstance(connection, null);
                ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, config.getProperty("objectStore"), null);

                String query = "SELECT Id FROM " + config.getProperty("documentClass") + " WHERE " + config.getProperty("propertyToUpdate") + " = '" + oldAccountNumber + "'";
                SearchSQL searchSQL = new SearchSQL(query);
                SearchScope searchScope = new SearchScope(objectStore);

                RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, Boolean.FALSE);

                while (rowSet.next()) {
                    IndependentObject obj = rowSet.getIndependentObjectValue(0);
                    Properties properties = obj.getProperties();
                    properties.putValue(config.getProperty("propertyToUpdate"), newAccountNumber);
                    obj.save(RefreshMode.REFRESH);
                }
            }

            String completedFilePath = file.getAbsolutePath().replace(".txt", "_completed.txt");
            File completedFile = new File(completedFilePath);
            file.renameTo(completedFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties loadConfigProperties() {
        Properties properties = new Properties();
        try {
            properties.load(FileNetUpdater.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}

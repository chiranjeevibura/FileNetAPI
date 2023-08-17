import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Domain;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.Document;
import com.filenet.api.property.Properties;
import com.filenet.api.util.UserContext;
import com.filenet.api.core.Factory;

public class FileNetMetadataExtractor {

    public static void main(String[] args) {
/*        String uri = "https://abc.com:9443/wsi/FNCEWS40MTOM/";
        String username = "admin";
        String password = "admin";
        String objectStoreName = "ABCS01";
        String inputFilePath = "C:\\Work\\guids.txt";
        String outputFilePath = "C:\\Work\\metadata.csv";*/
        
    	String uri = "https:// /wsi/FNCEWS40MTOM/";
		String username = " ";
		String password = " ";
		String objectStoreName = " ";
		//String documentClassName = "P8_Test";
        String outputFilePath = "C:\\Work\\propdata_uat_1.csv";
        String inputFilePath = "C:\\Work\\guids.txt"; // Path to the input text file containing GUIDs


        try {
            // Connect to the source domain
            Connection sourceConnection = Factory.Connection.getConnection(uri);
            Subject subject = UserContext.createSubject(sourceConnection, username, password, null);
            UserContext.get().pushSubject(subject);

            // Get the source domain
            Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

            // Print the source domain connection status
            System.out.println("Source Domain connected: True");
            System.out.println("Source Domain name: " + sourceDomain.get_Name());

            // Get the source object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(sourceDomain, objectStoreName, null);
            System.out.println("Source Object Store: " + objectStore.get_DisplayName());

            // Read GUIDs from input file
            List<String> guids = readGuidsFromFile(inputFilePath);

            // Prepare the CSV writer
            FileWriter csvWriter = new FileWriter(outputFilePath);
            writeCSVHeader(csvWriter);

            // Iterate over each GUID
            for (String guid : guids) {
                Document document = Factory.Document.fetchInstance(objectStore, guid, null);
                Properties properties = document.getProperties();
                writeCSVRow(csvWriter, document, properties);
            }

            csvWriter.flush();
            csvWriter.close();

            System.out.println("Metadata export completed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

 /*   private static List<String> readGuidsFromFile(String filePath) {
        List<String> guids = new ArrayList<>();
        // Implement the code to read the GUIDs from the input file into the guids list
        // For example, you can use BufferedReader to read the file line by line and add each line (GUID) to the list
        // Make sure to handle any exceptions that may occur during file reading
        return guids;
    }*/


    private static List<String> readGuidsFromFile(String filePath) {
        List<String> guids = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                guids.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return guids;
    }

    
    private static void writeCSVHeader(FileWriter csvWriter) throws IOException {
        csvWriter.append("Id,Amount,Batch Number,Check Number,Lockbox,Document type,DocumentTitle,DateLastModified,DateCreated,CheckInDate,Creator,VersionSeries,ContentSize,mimeType\n");
    }

    private static void writeCSVRow(FileWriter csvWriter, Document document, Properties properties) throws IOException {
        csvWriter.append(properties.getIdValue("Id").toString()).append(",");
        csvWriter.append(properties.getFloat64Value("AMOUNT").toString()).append(",");
        csvWriter.append(properties.getStringValue("BatchNumber")).append(",");
        csvWriter.append(properties.getStringValue("CheckNumber")).append(",");
        csvWriter.append(properties.getStringValue("Lockbox")).append(",");
        csvWriter.append(properties.getStringValue("DocumentType")).append(",");
        csvWriter.append(properties.getStringValue("DocumentTitle")).append(",");
        csvWriter.append(properties.getDateTimeValue("DateLastModified").toString()).append(",");
        csvWriter.append(properties.getDateTimeValue("DateCreated").toString()).append(",");
        csvWriter.append(properties.getDateTimeValue("CheckInDate").toString()).append(",");
        csvWriter.append(properties.getStringValue("Creator")).append(",");
        csvWriter.append(document.get_VersionSeries().toString()).append(",");
        csvWriter.append(document.get_ContentSize().toString()).append(",");
        csvWriter.append(document.get_MimeType()).append("\n");
    }
}

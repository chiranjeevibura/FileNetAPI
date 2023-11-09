import com.aspose.pdf.Document;
import com.aspose.pdf.TextFragment;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {

    public static void main(String[] args) {
        // Define the number of PDFs to generate
        int numberOfPDFs = 10;

        // Output directory where PDFs and the manifest file will be saved
        String outputDirectory = "path/to/outputDirectory";

        // Generate manifest file
        generateManifest(numberOfPDFs, outputDirectory);

        // Generate PDF files
        generatePDFs(numberOfPDFs, outputDirectory);
    }

    private static void generateManifest(int numberOfPDFs, String outputDirectory) {
        String manifestFilePath = outputDirectory + "/manifest.txt";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(manifestFilePath))) {
            writer.write("Account Number|EXRefNum|Doc Name|DocID|Date\n");

            for (int i = 0; i < numberOfPDFs; i++) {
                int randomNum = new Random().nextInt(900000000) + 100000000;
                String exRefNum = String.valueOf(randomNum);
                String docName = "PDF_" + (i + 1) + ".pdf";
                String docID = "65M17M62";
                String date = sdf.format(new Date());

                writer.write("|" + exRefNum + "|" + docName + "|" + docID + "|" + date + "\n");
            }
            System.out.println("Manifest file generated successfully at: " + manifestFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generatePDFs(int numberOfPDFs, String outputDirectory) {
        try {
            for (int i = 0; i < numberOfPDFs; i++) {
                Document pdfDocument = new Document();
                TextFragment textFragment = new TextFragment("Sample PDF content.");
                pdfDocument.getPages().add().getParagraphs().add(textFragment);

                String pdfPath = outputDirectory + "/PDF_" + (i + 1) + ".pdf";
                pdfDocument.save(pdfPath);

                System.out.println("PDF " + (i + 1) + " generated successfully at: " + pdfPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

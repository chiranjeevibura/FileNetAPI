import com.aspose.pdf.Document;
import com.aspose.pdf.Page;
import com.aspose.pdf.TextFragment;
import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PDFGenerator {

    public static void main(String[] args) {
        String inputDir = "path/to/input_directory";
        int numberOfPDFs = 10; // Define the number of PDF files to generate

        generatePDFs(inputDir, numberOfPDFs);
        generateManifestCSV(inputDir, numberOfPDFs);
    }

    private static void generatePDFs(String inputDir, int numberOfPDFs) {
        try {
            for (int i = 1; i <= numberOfPDFs; i++) {
                Document doc = new Document();
                Page page = doc.getPages().add();
                TextFragment textFragment = new TextFragment("Content for PDF " + i);
                page.getParagraphs().add(textFragment);
                doc.save(new FileOutputStream(new File(inputDir, "Doc" + i + ".pdf")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateManifestCSV(String inputDir, int numberOfPDFs) {
        Workbook workbook = new Workbook();
        Worksheet worksheet = workbook.getWorksheets().get(0);

        // Set the headers in the first row
        Cells cells = worksheet.getCells();
        cells.get("A1").setValue("Account Number");
        cells.get("B1").setValue("EXRefNum");
        cells.get("C1").setValue("Doc Name");
        cells.get("D1").setValue("DocID");
        cells.get("E1").setValue("Date");

        // Generate random number for EXRefNum
        Random random = new Random();
        for (int i = 1; i <= numberOfPDFs; i++) {
            cells.get("A" + (i + 1)).setValue(""); // Empty Account Number
            cells.get("B" + (i + 1)).setValue(String.valueOf(random.nextInt(900000000) + 100000000));
            cells.get("C" + (i + 1)).setValue("Doc" + i + ".pdf");
            cells.get("D" + (i + 1)).setValue("xxxxx");

            // Get the current date and set it in the "Date" column
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            cells.get("E" + (i + 1)).setValue(dateFormat.format(new Date()));
        }

        try {
            workbook.save(new FileOutputStream(new File(inputDir, "Manifest.csv")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

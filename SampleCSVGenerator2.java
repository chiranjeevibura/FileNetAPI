import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleCSVGenerator2 {
    public static void main(String[] args) {
        int totalRecords = 10;
        int matchingRecords = 10; // Adjust as needed

        List<String> matchingLoanNumbers = generateUniqueLoanNumbers(matchingRecords);
        generateSampleCSV("C:\\Work\\input1.csv", matchingLoanNumbers, totalRecords - matchingRecords);
        generateSampleCSV("C:\\Work\\input2.csv", matchingLoanNumbers, totalRecords - matchingRecords);
    }

    public static void generateSampleCSV(String filename, List<String> matchingLoanNumbers, int nonMatchingRecords) {
        System.out.println("Generating CSV file: " + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Create the header
            writer.write("loan_number,column2,column3");
            writer.newLine();

            Random random = new Random();
            for (int i = 0; i < matchingLoanNumbers.size() + nonMatchingRecords; i++) {
                String loanNumber = i < matchingLoanNumbers.size() ? matchingLoanNumbers.get(i) : generateUniqueLoanNumber(matchingLoanNumbers);
                String column2 = "Data2-" + i;
                String column3 = "Data3-" + i;

                // Write the data row
                writer.write(loanNumber + "," + column2 + "," + column3);
                writer.newLine();
            }

            System.out.println("CSV file generated successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error while generating CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<String> generateUniqueLoanNumbers(int count) {
        List<String> loanNumbers = new ArrayList<>();
        Random random = new Random();
        while (loanNumbers.size() < count) {
            String loanNumber = String.format("%06d", random.nextInt(1000000));
            if (!loanNumbers.contains(loanNumber)) {
                loanNumbers.add(loanNumber);
            }
        }
        return loanNumbers;
    }

    public static String generateUniqueLoanNumber(List<String> existingLoanNumbers) {
        Random random = new Random();
        while (true) {
            String loanNumber = String.format("%06d", random.nextInt(1000000));
            if (!existingLoanNumbers.contains(loanNumber)) {
                return loanNumber;
            }
        }
    }
}

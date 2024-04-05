/* Written by Frances Belleza **************************************************************************
*
*       *OBJECTIVE*
*             This program extracts Devanagari && its English
*             translation specifically from a txt file of
*             the Tantrasāra of Abhinava Gupta. This txt file
*             was extracted using Zorg's data collection
*             program, "transcribe-pdftree."
*
*       *THINGS TO CHANGE*
*               [x] needs to print into two seperate txt files
*               [x] need to bring back the dashes '-'
*                   [] if dashes are connecting a word -> keep dashes
*                       - can I tokenize all dashed words
*                   [] if dashes are seperating a word because of end split sentences -> connect the word
*               [] check if there is any java library that can create seperate lines for each end sentences
*                   - Apache openNLP : we need to download OpenNLP into Maven onto IntelliJ
*                          -> can detect end of sentences
*
********************************************************************************************************/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class sanskritTextProcessor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask for the text file path
        System.out.println("Enter the path to the text file to be extracted:");
        String extractedTextFilePath = scanner.nextLine();

        // Ask for the Sanskrit output text file
        System.out.println("Enter the name for the Sanskrit output text file:");
        String sanskritOutputFileName = scanner.nextLine();

        // Ask for the English output file name
        System.out.println("Enter the name for the English output text file:");
        String englishOutputFileName = scanner.nextLine();

        // Ensure that both output file has a .txt extension
        String sanskritOutputFilePath = sanskritOutputFileName.endsWith(".txt") ? sanskritOutputFileName : sanskritOutputFileName + ".txt";
        String englishOutputFilePath = englishOutputFileName.endsWith(".txt") ? englishOutputFileName : englishOutputFileName + ".txt";

        processExtractedText(extractedTextFilePath, sanskritOutputFilePath, englishOutputFilePath);
    }

    // Pattern to identify Sanskrit (Devanagari script) characters
    private static final Pattern SANSKRIT_PATTERN = Pattern.compile("[\\u0900-\\u097F]+");

    // FB: this didn't really work...Pattern to identify book titles in all caps and numbers
    // private static final Pattern CAPS_AND_NUMBERS_PATTERN = Pattern.compile("\\b[A-Z0-9]+\\b");

    public static void processExtractedText(String filePath, String sanskritOutputFilePath, String englishOutputFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             PrintWriter sanskritWriter = new PrintWriter(sanskritOutputFilePath);
             PrintWriter englishWriter = new PrintWriter(englishOutputFilePath)) {
            String line;
            boolean isDevanagariSection = false; //FB: flags
            boolean isEnglishSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(":Notes")) {
                    break; // FB: exits the loop when :Notes pop up
                    }

                if (SANSKRIT_PATTERN.matcher(line).find()) {
                    line = line.replaceAll("\\d+", ""); // Remove all numbers from the line
                    line = line.replaceAll("\\d*\\s*TANTRASĀRA\\s*\\d*", ""); // FB: removes #'s before book title
                    line = line.replaceAll("\\b\\w*TANTRASĀRA\\w*\\b", ""); // FB: was having instances of conjoint useful words with title ie "theTANTRASARA"
                    line = line.replaceAll("TANTRASARA", ""); // FB: I noticed there were instances without the 'Ā'
                    // line = line.replaceAll("\\p{Punct}", ""); // FB: removes all punctuations
                    line = line.replaceAll("H. N. ChakravartyTantrasāra of Abhinavagupta", "");
                    // Sanskrit text found, print it and mark the start of a new section
                    sanskritWriter.println(line);
                    isDevanagariSection = true;
                    isEnglishSection = false; // Reset English section flag
                } else if (isDevanagariSection) {
                    // Check for end of Sanskrit section and start of English translation
                    if (!line.isEmpty()) {
                        isDevanagariSection = false; // Sanskrit section ends when a non-empty line is found
                        isEnglishSection = true; // Start of English section
                    }
                }

                if (isEnglishSection) {
                    // Check for termination conditions for English text
                    if (!line.isEmpty() && !line.startsWith("Exposition:")) {

                        line = line.replaceAll("\\d+", "");
                        line = line.replaceAll("\\d*\\s*TANTRASĀRA\\s*\\d*", "");
                        line = line.replaceAll("\\b\\w*TANTRASĀRA\\w*\\b", "");
                        line = line.replaceAll("TANTRASARA", "");
                        //line = line.replaceAll("\\p{Punct}", "");

                        englishWriter.println(line);
                    } else {
                        isEnglishSection = false; // End of English section
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


/* Written by Frances Belleza - Sanskrit Text Processor *************************************************
 *
 *       *OBJECTIVE*
 *             This program extracts Devanagari && its English
 *             translation specifically from a txt file of
 *             the Tantrasāra of Abhinava Gupta. This txt file
 *             was extracted using Zorg's data collection
 *             program, "transcribe-pdftree."
 *
 ********************************************************************************************************/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.*;

public class sanskritTextProcessorFinal {

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
        StringBuilder sentenceBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             PrintWriter sanskritWriter = new PrintWriter(sanskritOutputFilePath);
             PrintWriter englishWriter = new PrintWriter(englishOutputFilePath)) {
            String line;
            boolean isDevanagariSection = false; //FB: flags
            boolean isEnglishSection = false;
            boolean previousLineEndedWithDash = false; //FB: flag to check for dashes + new line

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Check and process if the previous line ended with a dash
                if (previousLineEndedWithDash) {
                    line = sentenceBuilder.toString().trim() + line.replaceAll("^\\s+", "");  // Remove leading spaces from the current line
                    sentenceBuilder.setLength(0);
                    previousLineEndedWithDash = false;  // Reset the flag after adjusting the line
                }

                // Remove dashes at the end of the current line and set the flag
                if (line.endsWith("-")) {
                    line = line.substring(0, line.length() - 1);
                    previousLineEndedWithDash = true;
                }

                if (line.contains(":Notes")) {
                    break; // FB: exits the loop when :Notes pop up
                }

                if (SANSKRIT_PATTERN.matcher(line).find()) {
                    line = preprocessLine(line);
                    // Adjust the regex to split lines ensuring that all specified delimiters are kept with the sentence
                    String[] parts = line.split("(?<=।)(?=[^।१२३५६७])|(?<=।।३।।)|(?<=।।५।।)|(?<=।।२।।)|(?<=।।६।।)|(?<=।।७।।)|(?<=।१।।)|(?<=।।)(?=[^१२३५६७])");
                    for (int i = 0; i < parts.length; i++) {
                        String part = parts[i].trim();
                        if (!part.isEmpty()) {
                            sanskritWriter.println(part);  // Print each sentence on a new line
                            // Add a blank line if it ends with any of the specified paragraph markers
                            if (part.endsWith("।।३।।") || part.endsWith("।।५।।") || part.endsWith("।।२।।") || part.endsWith("।।७।।")
                                    || part.endsWith("।१।।") || part.endsWith("।।") || part.endsWith("।") || part.endsWith("।।६।।")) {
                                sanskritWriter.println(); // Print a blank line after paragraphs
                            }
                        }
                    }
                    isDevanagariSection = true;
                    isEnglishSection = false; // Reset English section flag
                } else if (isDevanagariSection) {
                    if (!line.isEmpty()) {
                        isDevanagariSection = false; // Sanskrit section ends when a non-empty line is found
                        isEnglishSection = true; // Start of English section
                    }
                }


//                if (SANSKRIT_PATTERN.matcher(line).find()) {
//                    // Process the line for Sanskrit text, removing unnecessary punctuation but keeping specific Sanskrit delimiters
//                    line = preprocessLine(line);
//                    // Split lines based on Sanskrit sentence delimiters while keeping the delimiters
//                    String[] parts = line.split("(?<=।)(?=[^।१२३५])|(?<=।।३।।)|(?<=।।५।।)|(?<=।।२।।)|(?<=।१।।)|(?<=।।)(?=[^१२३५])");                    for (int i = 0; i < parts.length; i++) {
//                        String part = parts[i].trim();
//                        if (!part.isEmpty()) {
//                            sanskritWriter.println(part);  // Print each sentence on a new line
//                            // Add a blank line if it ends with paragraph markers (।।३।। or ।।)
//                            if (part.endsWith("।।३।।") || part.endsWith("।।") || part.endsWith("।")) {
//                                sanskritWriter.println(); // Print a blank line after paragraphs
//                            }
//                        }
//                    }
//                    isDevanagariSection = true;
//                    isEnglishSection = false; // Reset English section flag
//                } else if (isDevanagariSection) {
//                    if (!line.isEmpty()) {
//                        isDevanagariSection = false; // Sanskrit section ends when a non-empty line is found
//                        isEnglishSection = true; // Start of English section
//                    }
//                }

//                if (SANSKRIT_PATTERN.matcher(line).find()) {
//                    line = preprocessLine(line); // Process line for Sanskrit text
//                    sanskritWriter.println(line);
//                    isDevanagariSection = true;
//                    isEnglishSection = false; // Reset English section flag
//                } else if (isDevanagariSection) {
//                    if (!line.isEmpty()) {
//                        isDevanagariSection = false; // Sanskrit section ends when a non-empty line is found
//                        isEnglishSection = true; // Start of English section
//                    }
//                }

                if (isEnglishSection) {
                    // Check for termination conditions for English text
                    if (!line.isEmpty() && !line.startsWith("Exposition:")) {
                        sentenceBuilder.append(line).append(" ");  // Accumulate the line

                        // Check for a sentence boundary
                        Matcher matcher = Pattern.compile(".*?\\.\\s").matcher(sentenceBuilder);
                        if (matcher.find()) {
                            String sentence = processSentence(sentenceBuilder, matcher);
                            englishWriter.println(sentence);  // Print the processed sentence
                            englishWriter.println();  // Add a blank line after the sentence
                        }
                    } else {
                        isEnglishSection = false; // End of English section
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String preprocessLine(String line) {
        line = line.replaceAll("\\d+", ""); // Remove all numbers from the line
        line = line.replaceAll("\\d*\\s*TANTRASĀRA\\s*\\d*", ""); // FB: removes #'s before book title
        line = line.replaceAll("\\b\\w*TANTRASĀRA\\w*\\b", ""); // FB: was having instances of conjoint useful words with title ie "theTANTRASARA"
        line = line.replaceAll("TANTRASARA", ""); // FB: I noticed there were instances without the 'Ā'
        line = line.replaceAll("[\\p{Punct}&&[^-]]", ""); // FB: removes all punctuations except dashes
        line = line.replaceAll("[\\p{IsLatin}]", ""); // Removes all Latin characters (i.e., English and other Western characters)
        line = line.replaceAll("\\d+", ""); // Remove digits
        return line;
    }

    private static String processSentence(StringBuilder sentenceBuilder, Matcher matcher) {
        String sentence = sentenceBuilder.substring(0, matcher.end() - 1).trim();
        sentence = sentence.replaceAll("[\\p{Punct}&&[^-\\.]]", "");  // Keep periods and dashes
        sentence = sentence.replaceAll("\\d+", "");  // Remove digits
        sentence = sentence.replaceAll("\\d*\\s*TANTRASĀRA\\s*\\d*", "");  // Remove 'TANTRASĀRA' with digits
        sentence = sentence.replaceAll("\\b\\w*TANTRASĀRA\\w*\\b", "");  // Remove 'TANTRASĀRA' within words
        sentence = sentence.replaceAll("TANTRASARA", "");  // Remove 'TANTRASARA'
        sentenceBuilder.delete(0, matcher.end());  // Clear the processed part from the StringBuilder
        return sentence;
    }

}
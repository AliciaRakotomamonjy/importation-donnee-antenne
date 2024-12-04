package org.example.importation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CombineSQLFiles {

    public static void main(String[] args) throws IOException {
        // Path of the directory containing the SQL files
        String directoryPath = "src/main/resources/sql"; // Directory with .sql files
        // Output file where the combined SQL content will be saved
        String outputFilePath = "src/main/resources/combined_sql.sql"; // Combined SQL output file

        // Combine all the SQL files from the directory into one
        combineSQLFiles(directoryPath, outputFilePath);

        System.out.println("All SQL files have been combined into: " + outputFilePath);
    }

    /**
     * Combines all the SQL files in the specified directory into a single output file.
     *
     * @param directoryPath the directory containing the .sql files
     * @param outputFilePath the output file where the combined SQL will be saved
     * @throws IOException if an I/O error occurs
     */
    private static void combineSQLFiles(String directoryPath, String outputFilePath) throws IOException {
        File folder = new File(directoryPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".sql")); // Filter only .sql files

        if (files == null || files.length == 0) {
            System.out.println("No SQL files found in the directory.");
            return;
        }

        // Create or overwrite the output file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, false))) {
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println("Processing file: " + file.getName());
                    // Read and append each SQL file content to the output file
                    appendSQLContent(file, writer);
                }
            }
        }
    }

    /**
     * Reads the content of a SQL file and appends it to the output writer.
     *
     * @param file the SQL file to read from
     * @param writer the writer to append the content to
     * @throws IOException if an I/O error occurs
     */
    private static void appendSQLContent(File file, BufferedWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            writer.newLine(); // Add a blank line between files
        }
    }
}

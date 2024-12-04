package org.example.importation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GenerateSQLFromIds {

    public static void main(String[] args) throws IOException {
        // Chemin du fichier contenant les IDs
        String inputFilePath = "src/main/resources/all_ids.csv";
        // Fichier de sortie contenant les requêtes SQL générées
        String outputFilePath = "src/main/resources/update_queries.sql";

        // Lire les IDs depuis le fichier CSV
        List<String> ids = readIdsFromCSV(inputFilePath);

        // Générer les requêtes SQL UPDATE
        List<String> updateQueries = generateUpdateQueries(ids);

        // Écrire les requêtes SQL dans un fichier
        writeQueriesToFile(updateQueries, outputFilePath);

        System.out.println("Les requêtes SQL ont été générées et sauvegardées dans : " + outputFilePath);
    }

    // Méthode pour lire les IDs depuis le fichier CSV
    private static List<String> readIdsFromCSV(String filePath) throws IOException {
        List<String> ids = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true; // pour ignorer l'entête

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Ignorer l'entête
                    continue;
                }
                // Supposons que le CSV contient un ID par ligne
                String[] columns = line.split(",");
                if (columns.length > 0) {
                    String id = columns[0].trim(); // récupérer l'ID
                    ids.add(id);
                }
            }
        }

        return ids;
    }

    // Méthode pour générer des requêtes SQL UPDATE
    private static List<String> generateUpdateQueries(List<String> ids) {
        List<String> queries = new ArrayList<>();
        String updateTemplate = "UPDATE personne SET estverifie = 1 WHERE id = '%s';";

        for (String id : ids) {
            String query = String.format(updateTemplate, id);
            queries.add(query);
        }

        return queries;
    }

    // Méthode pour écrire les requêtes SQL dans un fichier
    private static void writeQueriesToFile(List<String> queries, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String query : queries) {
                writer.write(query);
                writer.newLine();
            }
        }
    }
}

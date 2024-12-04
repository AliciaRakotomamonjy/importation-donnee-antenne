package org.example.importation;


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class CSVIdGatherer {

    public static void main(String[] args) throws IOException {
        // Répertoire contenant les fichiers CSV
        String directoryPath = "src/main/resources/idfotsiny"; // Répertoire des fichiers CSV
        String outputFilePath = "src/main/resources/all_ids.csv"; // Fichier de sortie contenant tous les IDs

        // Collecter tous les ID
        Set<String> allIds = new HashSet<>();

        // Lire tous les fichiers CSV du répertoire
        File folder = new File(directoryPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));

        if (files == null || files.length == 0) {
            System.out.println("Aucun fichier CSV trouvé dans le répertoire.");
            return;
        }

        for (File file : files) {
            // Lire le fichier CSV
            System.out.println("Traitement du fichier : " + file.getName());
            processCSVFile(file, allIds);
        }

        // Écrire tous les IDs dans le fichier de sortie
        writeIdsToCSV(allIds, outputFilePath);
        System.out.println("Tous les IDs ont été collectés dans : " + outputFilePath);
    }

    /**
     * Traite un fichier CSV pour extraire les IDs (et les bureaux de vote si présents)
     *
     * @param file     Le fichier CSV à traiter
     * @param allIds   L'ensemble pour collecter tous les IDs
     */
    private static void processCSVFile(File file, Set<String> allIds) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        // Ignore la première ligne (en-tête)
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(","); // Suppose que les colonnes sont séparées par des virgules

            if (columns.length >= 1) {
                String id = columns[0].trim(); // Première colonne est l'ID

                // Ajouter l'ID à l'ensemble, si l'ID n'est pas déjà présent
                allIds.add(id);
            }

            // Si la deuxième colonne existe (bureau de vote), vous pouvez l'ajouter aussi
            if (columns.length > 1) {
                // Vous pouvez traiter le `bureau_de_vote` ici si nécessaire
                String bureauDeVote = columns[1].trim();
                // Vous pouvez effectuer d'autres actions avec bureauDeVote si nécessaire
            }
        }

        reader.close();
    }

    /**
     * Écrit tous les IDs collectés dans un fichier CSV
     *
     * @param allIds         L'ensemble de tous les IDs
     * @param outputFilePath Le chemin du fichier de sortie
     * @throws IOException Si une erreur d'écriture se produit
     */
    private static void writeIdsToCSV(Set<String> allIds, String outputFilePath) throws IOException {
        // Créer le fichier de sortie s'il n'existe pas
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

        // Écrire l'en-tête
        writer.write("id\n");

        // Écrire chaque ID dans le fichier de sortie
        for (String id : allIds) {
            writer.write(id + "\n");
        }

        writer.close();
    }
}

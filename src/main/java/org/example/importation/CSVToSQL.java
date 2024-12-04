package org.example.importation;


import bean.CGenUtil;
import map.BureauVote;
import org.example.db.DatabaseConnection;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CSVToSQL {

    public static void main(String[] args) throws Exception{

        Connection connection = null;

        try {
            // Essaye d'établir la connexion
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            System.out.println("Connexion réussie à PostgreSQL !");

            // Vérifie si la connexion est valide
            if (DatabaseConnection.isConnectionValid(connection)) {
                System.out.println("La connexion est valide.");
            }

            // Chemin des fichiers
            String csvFilePath = "src/main/resources/bureauvote-combined.csv"; // Fichier CSV d'entrée
            String outputSqlFilePath = "src/main/resources/bureauvote-combined-script.sql"; // Fichier SQL généré
            String errorFilePath = "src/main/resources/bureauvote-combined-echoue.csv"; // Fichier CSV des erreurs
            List<String> sqlUpdates = new ArrayList<>();
            List<String[]> errorLines = new ArrayList<>();

            processCSV(csvFilePath, sqlUpdates, errorLines, connection);

            // Écrire les requêtes SQL dans un fichier (écrase le fichier s'il existe)
            writeSQLToFile(sqlUpdates, outputSqlFilePath);

            // Écrire les lignes d'erreur dans un fichier CSV (écrase le fichier s'il existe)
            writeErrorsToCSV(errorLines, errorFilePath);

            System.out.println("Le script SQL a été généré avec succès dans : " + outputSqlFilePath);
            System.out.println("Les lignes avec erreurs ont été enregistrées dans : " + errorFilePath);

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
            connection.rollback();
        } catch (Exception e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            // Ferme la connexion
            DatabaseConnection.closeConnection(connection);
        }



    }

    /**
     * Traite un fichier CSV pour générer des requêtes SQL et capturer les lignes avec erreurs.
     *
     * @param csvFilePath Chemin du fichier CSV d'entrée
     * @param sqlUpdates Liste des requêtes SQL générées
     * @param errorLines Liste des lignes ayant des erreurs
     * @throws IOException En cas de problème de lecture du fichier
     */
    public static void processCSV(String csvFilePath, List<String> sqlUpdates, List<String[]> errorLines, Connection con) throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int ligne = 1;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Ignore la première ligne (en-tête)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                try{
                    // Séparer les colonnes du CSV
                    String[] values = line.split(",");

                    // Vérifier la validité des données
                    if (values.length < 2 || values[0].trim().isEmpty() || values[1].trim().isEmpty()) {
                        // Ligne invalide : ajouter à la liste des erreurs
                        errorLines.add(values);
                    } else {
                        // Générer la requête SQL pour les lignes valides
                        String id = values[0].trim();
                        String bureauDeVote = values[1].trim();
                        String idquartier = "";
                        BureauVote bureauVote = new BureauVote();
                        BureauVote[] bureauVotes = (BureauVote[]) CGenUtil.rechercher(bureauVote, null, null, con, String.format(" and TRIM(UPPER(desce)) = TRIM(UPPER('%s'))",bureauDeVote.replaceAll("'","''")));
                        if(bureauVotes.length > 0){
                            bureauVote = bureauVotes[0];
                            bureauDeVote = bureauVote.getId();
                            idquartier = bureauVote.getIdQuartier();
                        }else{
                            errorLines.add(values);
                            String updateQuery = String.format(
                                    "UPDATE personne SET etat = 7, presenceListeCeni = '0' WHERE id = '%s';",
                                    id
                            );
                            sqlUpdates.add(updateQuery);
                            throw new Exception(" Bureau de vote non existant");
                        }

                        String updateQuery = String.format(
                                "UPDATE personne SET idBureauVote = '%s', idQuartier='%s', etat = 7, presenceListeCeni = '1' WHERE id = '%s';",
                                bureauDeVote, // Échapper les apostrophes
                                idquartier,
                                id
                        );
                        sqlUpdates.add(updateQuery);
                    }
                    ligne++;
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("Erreur sur la ligne "+ligne+" : "+e.getMessage());
                }

            }
        }
    }

    /**
     * Écrit une liste de commandes SQL dans un fichier (écrase le fichier s'il existe).
     *
     * @param sqlUpdates Liste des commandes SQL
     * @param outputFilePath Chemin du fichier de sortie
     * @throws IOException En cas de problème d'écriture
     */
    public static void writeSQLToFile(List<String> sqlUpdates, String outputFilePath) throws IOException {
        // Ouvrir le fichier en mode écriture (écrasement)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, false))) {
            for (String sql : sqlUpdates) {
                writer.write(sql);
                writer.newLine();
            }
        }
    }

    /**
     * Écrit les lignes avec erreurs dans un fichier CSV (écrase le fichier s'il existe).
     *
     * @param errorLines Liste des lignes avec erreurs
     * @param errorFilePath Chemin du fichier CSV des erreurs
     * @throws IOException En cas de problème d'écriture
     */
    public static void writeErrorsToCSV(List<String[]> errorLines, String errorFilePath) throws IOException {
        // Ouvrir le fichier en mode écriture (écrasement)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(errorFilePath, false))) {
            // Écrire les en-têtes
            writer.write("id,bureau_de_vote");
            writer.newLine();

            // Écrire les lignes d'erreurs
            for (String[] errorLine : errorLines) {
                writer.write(String.join(",", errorLine));
                writer.newLine();
            }
        }
    }
}


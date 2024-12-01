package org.example;

import org.example.db.DatabaseConnection;
import org.example.importation.ExcelImporter;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
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

            String excelFilePath = "src/main/resources/saisie-ind2.xlsx";
            String excelOutputFilePath = "src/main/resources/script.sql";
            String errorOutputFilePath = "src/main/resources/saisie-echoue.xlsx";
            ExcelImporter importer = new ExcelImporter();
            importer.importDataFromExcel(excelFilePath, excelOutputFilePath,errorOutputFilePath, connection);
            System.out.println("Importation des données réussie !");
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
}
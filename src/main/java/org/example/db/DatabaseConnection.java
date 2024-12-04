package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static String URL_BASE = "jdbc:postgresql://206.189.123.41:2003/antenne";
    private static String USER_BASE = "postgres";
    private static String PASSWORD_BASE = "root";

    /**
     * Établit la connexion à la base de données PostgreSQL.
     *
     * @return une connexion à la base de données
     * @throws SQLException si la connexion échoue
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL_BASE, USER_BASE, PASSWORD_BASE);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la connexion à la base de données PostgreSQL.", e);
        }
    }

    /**
     * Vérifie si la connexion est valide.
     *
     * @param connection la connexion à tester
     * @return true si la connexion est valide, false sinon
     */
    public static boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Ferme proprement la connexion à la base de données.
     *
     * @param connection la connexion à fermer
     */
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
}

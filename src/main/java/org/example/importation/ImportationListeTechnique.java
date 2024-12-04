package org.example.importation;

import bean.CGenUtil;
import bean.TypeObjet;
import map.BureauVote;
import map.PersonneResponsable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.data.*;
import org.example.db.DatabaseConnection;
import utilitaire.Utilitaire;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ImportationListeTechnique {

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

            String excelFilePath = "src/main/resources/liste-technique.xlsx";
            String excelOutputFilePath = "src/main/resources/liste-technique-script.sql";
            String errorOutputFilePath = "src/main/resources/liste-technique-echoue.xlsx";
            ImportationListeTechnique importer = new ImportationListeTechnique();
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

    private static String INSERT_SQL_TEMPLATE = "INSERT INTO personne (id, nom, prenom, cin, delivreLe, delivreA, telephone, idArrondissement, idQuartier, idBureauVote, idNiveauEtude, idSource, score, presenceListeCeni, etatInfo,qualification, idCommuneDefaut, estimporte, etat) " +
            "VALUES (%s, %s, %s, %s, %s, %s,%s, %s, %s,%s,%s, %s, %d, %s, %s, %s, %s,  1, 7);";


    /**
     * Copie une ligne (sans formater les cellules).
     *
     * @param sourceRow la ligne source à copier
     * @param targetRow la ligne cible dans laquelle copier
     */
    private void copyRow(Row sourceRow, Row targetRow) {
        for (int i = 0; i < sourceRow.getPhysicalNumberOfCells(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = targetRow.createCell(i);
            targetCell.setCellValue(sourceCell.toString()); // Ne pas formater, juste copier la valeur
        }
    }

    /**
     * Importe les données d'un fichier Excel dans la base de données.
     *
     * @param filePath chemin vers le fichier Excel
     * @param connection la connexion à la base de données
     * @throws Exception en cas d'erreur pendant l'importation
     */
    public void importDataFromExcel(String filePath, String outputFilePath,String errorFilePath, Connection connection) throws Exception {
        FileInputStream fis = null;
        Workbook workbook = null;
        BufferedWriter writer = null;
        Workbook errorWorkbook = new XSSFWorkbook();
        Sheet errorSheet = errorWorkbook.createSheet("Importation echoues");
        List<Row> failedRows = new ArrayList<>();




        try {
            // Charger le fichier Excel d'entrée
            fis = new FileInputStream(new File(filePath));
            workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            List<PersonneResponsable> personnes = new ArrayList<>();

//            Row headerRow = sheet.getRow(0); // Première ligne (header)
//            Row errorHeaderRow = errorSheet.createRow(0);
//            copyRow(headerRow, errorHeaderRow);

            File sqlFile = new File(outputFilePath);
            writer = new BufferedWriter(new FileWriter(sqlFile));

            // Parcours des lignes du fichier Excel
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Ignore la première ligne (headers)
                if (row.getRowNum() == 0) {
                    continue;
                }
                try{

                    String idDefaut = "'PERSIMP00' || getseqpersonne()";
                    // Récupérer les valeurs des colonnes du fichier Excel
                    String id = Utilitaire.champNull(getCellStringValueWithDefault(row, 12,idDefaut)).trim();
                    if(id.equalsIgnoreCase("0") || id.equalsIgnoreCase("-") || id.equalsIgnoreCase(idDefaut)){
                        id = idDefaut;
                    }else{
                        id = addQuotesIfNotNull(id);
                    }
                    String nom = Utilitaire.champNull(getCellStringValueWithDefault(row, 13,"NA")).trim();
                    if(nom.equalsIgnoreCase("-")||nom.equalsIgnoreCase("0")){
                        nom = "NA";
                    }
                    String prenom = Utilitaire.champNull(getCellStringValueWithDefault(row, 14,"NA")).trim();
                    if(prenom.equalsIgnoreCase("-")||prenom.equalsIgnoreCase("0")){
                        prenom = "NA";
                    }
                    String cin = Utilitaire.champNull(getCellStringValueWithDefault(row, 15,"NA")).trim();
                    if(cin.equalsIgnoreCase("-")||cin.equalsIgnoreCase("0")){
                        cin = "NA";
                    }
                    String delivreA = Utilitaire.champNull(getCellStringValueWithDefault(row, 17,"NA")).trim();
                    if(delivreA.equalsIgnoreCase("-")||delivreA.equalsIgnoreCase("0")){
                        delivreA = "NA";
                    }
                    int delivreLeInt = getCellIntValue(row, 16);

                    String delivreLe = null;
                    if (delivreLeInt == 0) {
                        delivreLe = null;
                    } else {
                        // Si la valeur est un nombre valide, on la convertit en date
                        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(delivreLeInt);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        delivreLe = sdf.format(date); // Format SQL 'yyyy-MM-dd'
                    }


                    String arrondissement = getCellStringValue(row,5);
                    String quartier = "";
                    String bureaudevote = getCellStringValue(row,10);

                    String niveauEtude = getCellStringValue(row,20);
                    String telephone = Utilitaire.champNull(getCellStringValueWithDefault(row, 18,"NA")).trim();
                    if(telephone.equalsIgnoreCase("-")||telephone.equalsIgnoreCase("0")){
                        telephone = "NA";
                    }
                    String idCommuneDefaut = "COM1";
                    String presenceListeCENI = "1";
                    String etatInfo = "1";
                    String qualification = "1";
                    int score = getCellIntValue(row, 21);

                    arrondissement = DataArrondissement.getData(arrondissement);
                    niveauEtude = DataNiveauEtude.getData(niveauEtude);
                    presenceListeCENI =  "1";
                    String idSource = "SRC0002";


                    cin = Utilitaire.champNull(cin).trim();
                    if(!cin.equalsIgnoreCase("NA")){
                        PersonneResponsable pers = new PersonneResponsable();
                        pers.setNomTable("personne");
                        pers.setCin(cin);
                        PersonneResponsable[] personnees = ( PersonneResponsable[])CGenUtil.rechercher(pers, null, null, connection, "");
                        if(personnees.length>0){
                            throw new Exception("CIN déjà existant");
                        }
                    }


                    BureauVote lieuvote = new BureauVote();
                    bureaudevote = Utilitaire.champNull(bureaudevote).trim().replaceAll("\n","").replaceAll("'", "''");
                    BureauVote[] bureauVotes = (BureauVote[]) CGenUtil.rechercher(lieuvote, null,null, connection, String.format(" and TRIM(UPPER(DESCE)) = TRIM(UPPER('%s'))",bureaudevote));
                    if(bureauVotes.length>0){
                        lieuvote = bureauVotes[0];
                        quartier = lieuvote.getIdQuartier();
                        bureaudevote = lieuvote.getId();
                    }else{
                        throw new Exception("tsy misy bureau de vote "+ bureaudevote+ " =============");
                    }


                    // Préparation des valeurs pour l'INSERT SQL
                    String idQuoted = id;
                    String nomQuoted = addQuotesIfNotNull(nom);
                    String prenomQuoted = addQuotesIfNotNull(prenom);
                    String cinQuoted = addQuotesIfNotNull(cin);
                    String delivreLeQuoted = addQuotesIfNotNull(delivreLe);
                    String delivreAQuoted = addQuotesIfNotNull(delivreA);
                    String telephoneQuoted = addQuotesIfNotNull(telephone);
                    String arrondissementQuoted = addQuotesIfNotNull(arrondissement);
                    String quartierQuoted = addQuotesIfNotNull(quartier);
                    String bureaudevoteQuoted = addQuotesIfNotNull(bureaudevote);
                    String niveauEtudeQuoted = addQuotesIfNotNull(niveauEtude);
                    String idSourceQuoted = addQuotesIfNotNull(idSource);

                    String presenceListeCENIQuoted = addQuotesIfNotNull(presenceListeCENI);
                    String etatInfoQuoted = addQuotesIfNotNull(etatInfo);
                    String qualificationQuoted = addQuotesIfNotNull(qualification);
                    String idCommuneDefautQuoted = addQuotesIfNotNull(idCommuneDefaut);

// Préparer la requête SQL avec les valeurs formatées
                    String sql = String.format(INSERT_SQL_TEMPLATE, idQuoted, nomQuoted, prenomQuoted, cinQuoted, delivreLeQuoted, delivreAQuoted, telephoneQuoted, arrondissementQuoted, quartierQuoted, bureaudevoteQuoted, niveauEtudeQuoted, idSourceQuoted, score, presenceListeCENIQuoted, etatInfoQuoted,qualificationQuoted, idCommuneDefautQuoted);


                    // Écrire la requête SQL dans le fichier
                    writer.write(sql);
                    writer.newLine();
                }catch (Exception ex){
                    failedRows.add(row);
//                    ex.printStackTrace();
                    // Log de l'erreur dans un fichier
                    System.err.println("Erreur lors du traitement de la ligne " + row.getRowNum()  + ": " + ex.getMessage());
                }



            }

            if (!failedRows.isEmpty()) {
                int rowIndex = 0;
                for (Row failedRow : failedRows) {
                    Row newRow = errorSheet.createRow(rowIndex++);
                    for (int i = 0; i < failedRow.getPhysicalNumberOfCells(); i++) {
                        Cell newCell = newRow.createCell(i);
                        newCell.setCellValue(getCellValue(failedRow, i));
                    }
                }

                // Sauvegarder le fichier Excel des erreurs
                try (FileOutputStream fileOut = new FileOutputStream(errorFilePath)) {
                    errorWorkbook.write(fileOut);
                }
            }





        } catch (Exception e) {
            throw new Exception("Erreur lors de l'importation des données depuis Excel", e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (fis != null) {
                fis.close();
            }

            if (writer != null) {
                writer.close();
            }
        }
    }

    private String addQuotesIfNotNull(String value) {
        if (value != null) {
            return "'" + value.replace("'", "''") + "'"; // Remplace les apostrophes simples par deux apostrophes pour éviter des erreurs SQL
        } else {
            return "NULL";  // Si la valeur est null ou vide, renvoie "NULL"
        }
    }

    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;  // Retourne null si la cellule est vide
        }
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        } else {
            return null;  // Retourne null si la cellule n'est pas de type String
        }
    }

    private int getCellIntValueDDR(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return 0;  // Retourne 0 si la cellule est vide
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return (int) cell.getNumericCellValue();
        }else if(cell.getCellType() == Cell.CELL_TYPE_STRING){
            String duree = cell.getStringCellValue();
            Integer dureeDuree = DataDureeResidence.getData(Utilitaire.champNull(duree).trim());
            return dureeDuree != null ? dureeDuree.intValue() : 0;
        }
        else {
            return 0;  // Retourne 0 si la cellule n'est pas de type Numeric
        }
    }

    private int getCellIntValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return 0;  // Retourne 0 si la cellule est vide
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else {
            return 0;  // Retourne 0 si la cellule n'est pas de type Numeric
        }
    }

    private String getCellStringValueWithDefault(Row row, int cellIndex, String defaultValue) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return defaultValue;  // Retourne la valeur par défaut si la cellule est vide
        }
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        } else {
            return defaultValue;  // Retourne la valeur par défaut si la cellule n'est pas de type String
        }
    }

    private String getCellStringValueOrNumeric(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;  // Retourne null si la cellule est vide
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return "NA";  // Retourne null pour tout autre type
        }
    }

    /**
     * Insère les données dans la base de données.
     *
     * @param personnes liste des objets PersonneResponsable à insérer
     * @param connection la connexion à la base de données
     * @throws SQLException en cas d'erreur lors de l'insertion
     */
    private void insertDataToDatabase(List<PersonneResponsable> personnes, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL_TEMPLATE)) {
            for (PersonneResponsable personne : personnes) {
                stmt.setString(1, personne.getNom());
                stmt.setString(2, personne.getPrenom());

                stmt.addBatch();  // Ajouter à un batch pour insérer plusieurs lignes en une seule fois
            }
            stmt.executeBatch();  // Exécuter le batch d'insertion
        }
    }
}

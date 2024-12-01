package org.example.importation;

import bean.CGenUtil;
import bean.TypeObjet;
import map.BureauVote;
import map.PersonneResponsable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.data.*;
import utilitaire.Utilitaire;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelImporter {

    private static String INSERT_SQL_TEMPLATE = "INSERT INTO personne (id, nom, prenom, telephone, comptefb, sexe, age, cin, idArrondissement, idQuartier, idBureauVote, idSource, numeroCarteElectorale, codeLotissement, numeroLotissement, idNiveauEtude, idPossessionTelephone, idEtatReseauSociaux, dureeResidenceQuartier, score, idCivilite, presenceListeCeni, nomSource, etatInfo, qualification, nombrePersonneCitee, idCommuneDefaut, estimporte, etat) " +
            "VALUES ('PERSIMP00' || getseqpersonne(), '%s', '%s', '%s', '%s', '%s', %d, '%s', '%s','%s','%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d,  %d,'%s', '%s', '%s', '%s', '%s', %d, '%s', 1, 1);";


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


                    // Récupérer les valeurs des colonnes du fichier Excel

                    String civilite = getCellStringValueWithDefault(row, 3,"NA");
                    String nom = getCellStringValueWithDefault(row, 4,"NA");
                    String prenom = getCellStringValueWithDefault(row, 5,"NA");
                    String cin = getCellStringValueWithDefault(row, 6,"NA");
                    String numerocarteElectorale = getCellStringValueOrNumeric(row,7);

                    String codeLotissement = getCellStringValueWithDefault(row, 8,"NA");
                    String numeroLotissement = getCellStringValueWithDefault(row, 9,"NA");
                    String arrondissement = getCellStringValue(row,10);
                    String quartier = getCellStringValue(row,11);
                    String bureaudevote = getCellStringValue(row,12);
                    String idSource = getCellStringValue(row,13);
                    String nomSource = getCellStringValueWithDefault(row, 14,"NA");
                    String sexe = getCellStringValue(row,15);
                    int age = getCellIntValue(row, 16);

                    int dureeResidenceQuartier = getCellIntValueDDR(row, 17);
                    String niveauEtude = getCellStringValue(row,20);
                    String possessionTelephone = getCellStringValue(row,21);
                    String telephone = getCellStringValueWithDefault(row, 22,"NA");
                    String comptefb = getCellStringValueWithDefault(row, 23,"NA");
                    String etatReseauxSociaux = getCellStringValue(row,24);
                    int nombrePersonneCitee = getCellIntValue(row, 25);

                    String presenceListeCENI = getCellStringValue(row,26);
                    String etatInfo = getCellStringValue(row,27);
                    String qualification = getCellStringValue(row,28);
                    int score = getCellIntValue(row, 29);

                    civilite = DataCivilite.getData(Utilitaire.champNull(civilite).trim());
                    arrondissement = DataArrondissement.getData(arrondissement);
                    etatInfo = DataEtatInfo.getData(etatInfo);
                    etatReseauxSociaux = DataEtatReseauSociaux.getData(etatReseauxSociaux);
                    niveauEtude = DataNiveauEtude.getData(niveauEtude);
                    possessionTelephone = DataPossessionTelephone.getData(possessionTelephone);
                    qualification = DataQualification.getData(qualification);
                    idSource = DataSource.getData(idSource);
                    presenceListeCENI = DataPresenceListeCeni.getData(presenceListeCENI) != null ? DataPresenceListeCeni.getData(presenceListeCENI) : "0";

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
                    lieuvote.setVal(bureaudevote);
                    BureauVote[] bureauVotes = (BureauVote[]) CGenUtil.rechercher(lieuvote, null,null, connection, "");
                    if(bureauVotes.length>0){
                        lieuvote = bureauVotes[0];
                        quartier = lieuvote.getIdQuartier();
                        bureaudevote = lieuvote.getId();
                    }else{
                        throw new Exception("tsy misy bureau de vote "+ bureaudevote+ " =============");
                    }


//              Créer un objet PersonneResponsable à partir des données
                    String sql = String.format(INSERT_SQL_TEMPLATE, nom, prenom, telephone, comptefb, sexe, age, cin, arrondissement, quartier,bureaudevote, idSource, numerocarteElectorale, codeLotissement, numeroLotissement, niveauEtude, possessionTelephone, etatReseauxSociaux, dureeResidenceQuartier, score, civilite, presenceListeCENI, nomSource, etatInfo, qualification, nombrePersonneCitee,"COM1");

                    // Écrire la requête SQL dans le fichier
                    writer.write(sql);
                    writer.newLine();
                }catch (Exception ex){
                    failedRows.add(row);
                    // Log de l'erreur dans un fichier
                    System.err.println("Erreur lors du traitement de la ligne " + row.getRowNum() + ": " + ex.getMessage());
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

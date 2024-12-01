package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataPossessionTelephone {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("SMART-PHONE", "POSTEL001");
        DONNEES.put("PAS DE TELEPHONE", "POSTEL003");
        DONNEES.put("TELEPHONE", "POSTEL002");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}


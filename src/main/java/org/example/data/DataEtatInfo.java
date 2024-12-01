package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataEtatInfo {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("COMPLETES", "1");
        DONNEES.put("INCOMPLETES", "0");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}

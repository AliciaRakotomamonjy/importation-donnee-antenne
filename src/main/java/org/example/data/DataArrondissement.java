package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataArrondissement {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("ANTANANARIVO_II", "ARR0002");
        DONNEES.put("ANTANANARIVO_III", "ARR0003");
        DONNEES.put("ANTANANARIVO_IV", "ARR0004");
        DONNEES.put("ANTANANARIVO_V", "ARR0005");
        DONNEES.put("ANTANANARIVO_VI", "ARR0006");
        DONNEES.put("ANTANANARIVO_I", "ARR0001");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }
}

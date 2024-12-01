package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataQualification {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("QUALIFIEE", "1");
        DONNEES.put("DISQUALIFIEE", "0");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}

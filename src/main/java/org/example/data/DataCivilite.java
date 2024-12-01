package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataCivilite {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("MME", "CIV002");
        DONNEES.put("MR", "CIV001");
        DONNEES.put("MMR", "CIV001");
        DONNEES.put("MLLE", "CIV003");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}

package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataSource {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("SUR TERRAIN", "SRC0002");
        DONNEES.put("POLITIQUE", "SRC0001");
        DONNEES.put("COMMUNICATION", "SRC0003");
        DONNEES.put("VOLONTAIRE", "SRC0004");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}

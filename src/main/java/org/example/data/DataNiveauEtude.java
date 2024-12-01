package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataNiveauEtude {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("BACC", "NVE0002");
        DONNEES.put("BACC+2", "NVE0005");
        DONNEES.put("BEPC", "NVE0001");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}

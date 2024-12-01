package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataPresenceListeCeni {


    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("ABSENTE", "1");
        DONNEES.put("PRESENT", "0");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}


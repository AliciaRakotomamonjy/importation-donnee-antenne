package org.example.data;

import java.util.HashMap;
import java.util.Map;

public class DataDureeResidence {


    public static final Map<String, Integer> DONNEES = new HashMap<>();

    static {
        DONNEES.put("Plus de 10 ans", 12);
    }

    public static Integer getData(String cle) {
        return DONNEES.get(cle);
    }

}

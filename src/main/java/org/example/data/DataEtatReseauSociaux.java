package org.example.data;


import java.util.HashMap;
import java.util.Map;

public class DataEtatReseauSociaux {

    public static final Map<String, String> DONNEES = new HashMap<>();

    static {
        DONNEES.put("PAS PRESENT SUR FACEBOOK", "RESC0004");
        DONNEES.put("FOLLOWERS DE LA PAGE DU CANDIDAT", "RESC0003");
        DONNEES.put("PRESENT SUR FACEBOOK MAIS PAS FOLLOWERS", "RESC0005");
    }

    public static String getData(String cle) {
        return DONNEES.get(cle);
    }

}



package com.example.orchom;

import java.io.Serializable;

/**
 * Classe de configuration du jeu
 */
public class GameConfig implements Serializable {
    public final String gameName;
    public final int playerCount;
    public final int targetScore; // Score à atteindre pour perdre
    public final String[] playerNames;
    
    // Motor configuration
    public final String startingPlayerMode; // "ALEATOIRE", "ROTATION", "MANUAL"
    public final int firstPlayerIndex; // Utilisé pour le mode rotation ou manuel

    // Scoring values
    public final int ghaltaValue;
    public final int frich1Value;
    public final int frich2Value;
    public final int memnechValue;

    public GameConfig(String gameName, int playerCount, int targetScore, String[] playerNames,
                      String startingPlayerMode, int firstPlayerIndex,
                      int ghaltaValue, int frich1Value, int frich2Value, int memnechValue) {
        this.gameName = gameName;
        this.playerCount = playerCount;
        this.targetScore = targetScore;
        this.playerNames = playerNames;
        this.startingPlayerMode = startingPlayerMode;
        this.firstPlayerIndex = firstPlayerIndex;
        this.ghaltaValue = ghaltaValue;
        this.frich1Value = frich1Value;
        this.frich2Value = frich2Value;
        this.memnechValue = memnechValue;
    }

    public org.json.JSONObject toJSON() {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("gameName", gameName);
            json.put("playerCount", playerCount);
            json.put("targetScore", targetScore);
            org.json.JSONArray names = new org.json.JSONArray();
            for (String n : playerNames) names.put(n);
            json.put("playerNames", names);
            
            json.put("startingPlayerMode", startingPlayerMode);
            json.put("firstPlayerIndex", firstPlayerIndex);

            json.put("ghaltaValue", ghaltaValue);
            json.put("frich1Value", frich1Value);
            json.put("frich2Value", frich2Value);
            json.put("memnechValue", memnechValue);
            
            return json;
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GameConfig fromJSON(org.json.JSONObject json) {
        try {
            int count = json.getInt("playerCount");
            String[] names = new String[count];
            org.json.JSONArray namesArr = json.getJSONArray("playerNames");
            for(int i=0; i<count; i++) names[i] = namesArr.getString(i);

            return new GameConfig(
                json.optString("gameName", "Partie sans nom"),
                count,
                json.getInt("targetScore"),
                names,
                json.optString("startingPlayerMode", "MANUAL"),
                json.optInt("firstPlayerIndex", 0),
                json.optInt("ghaltaValue", 10),
                json.optInt("frich1Value", 10),
                json.optInt("frich2Value", 20),
                json.optInt("memnechValue", 50)
            );
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            // Fallback to a safe default configuration
            return new GameConfig("Partie sans nom", 2, 500, new String[]{"Joueur 1", "Joueur 2"}, "ROTATION", 0, 50, 10, 20, 100);
        }
    }
}

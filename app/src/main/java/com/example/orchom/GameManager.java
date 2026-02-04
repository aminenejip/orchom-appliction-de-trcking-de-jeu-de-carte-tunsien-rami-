package com.example.orchom;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    private GameConfig currentConfig;
    private List<Player> currentPlayers;
    private boolean isGameActive = false;
    private int currentRound = 1;
    private int currentStarterIndex = 0;
    
    // Constants for SharedPreferences
    private static final String PREF_NAME = "GameSave";
    private static final String KEY_ACTIVE = "game_active";
    private static final String KEY_CONFIG = "game_config";
    private static final String KEY_PLAYERS = "players";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_ROUND = "current_round";
    private static final String KEY_STARTER_INDEX = "starter_index";

    private GameManager() {}

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startNewGame(GameConfig config) {
        this.currentConfig = config;
        this.currentPlayers = new ArrayList<>();
        this.currentRound = 1;
        
        if ("ALEATOIRE".equals(config.startingPlayerMode)) {
            this.currentStarterIndex = (int) (Math.random() * config.playerCount);
        } else {
            this.currentStarterIndex = config.firstPlayerIndex;
        }
        
        for (int i = 0; i < config.playerCount; i++) {
            String color = ColorManager.getPlayerColorHex(i);
            this.currentPlayers.add(new Player(config.playerNames[i], i, color));
        }
        
        this.isGameActive = true;
    }

    public int getStartingPlayerIndex(Context context) {
        return currentStarterIndex;
    }

    public void rotateStarter() {
        if (currentConfig != null && currentConfig.playerCount > 0) {
            currentStarterIndex = (currentStarterIndex + 1) % currentConfig.playerCount;
        }
    }

    public void setStarterIndex(int index) {
        if (currentConfig != null && index >= 0 && index < currentConfig.playerCount) {
            this.currentStarterIndex = index;
        }
    }


    public void restoreGame(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_ACTIVE, false)) {
            try {
                String configJson = prefs.getString(KEY_CONFIG, "");
                String playersJson = prefs.getString(KEY_PLAYERS, "");
                
                if (configJson != null && !configJson.isEmpty() && playersJson != null && !playersJson.isEmpty() && !playersJson.equals("[]")) {
                    this.currentConfig = GameConfig.fromJSON(new JSONObject(configJson));
                    
                    JSONArray arr = new JSONArray(playersJson);
                    this.currentPlayers = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        Player p = Player.fromJSON(arr.getJSONObject(i));
                        if (p != null) {
                            this.currentPlayers.add(p);
                        }
                    }
                    
                    this.currentRound = prefs.getInt(KEY_ROUND, 1);
                    this.currentStarterIndex = prefs.getInt(KEY_STARTER_INDEX, 0);
                    
                    // Final safety check
                    this.isGameActive = (this.currentConfig != null && this.currentPlayers != null && !this.currentPlayers.isEmpty());
                } else {
                    this.isGameActive = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                this.isGameActive = false; // Corrupt data
            }
        } else {
            this.isGameActive = false;
        }
    }

    public void saveGame(Context context) {
        if (!isGameActive || currentConfig == null) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putBoolean(KEY_ACTIVE, true);
        editor.putInt(KEY_ROUND, currentRound);
        editor.putInt(KEY_STARTER_INDEX, currentStarterIndex);
        
        JSONObject configJson = currentConfig.toJSON();
        if (configJson != null) editor.putString(KEY_CONFIG, configJson.toString());
        
        JSONArray playersArr = new JSONArray();
        for (Player p : currentPlayers) {
            JSONObject pj = p.toJSON();
            if (pj != null) playersArr.put(pj);
        }
        editor.putString(KEY_PLAYERS, playersArr.toString());
        
        editor.apply();
    }


    public void startNextRound() {
        currentRound++;
        
        // Handle automatic rotation modes (Bug 3)
        if (currentConfig != null) {
            if ("ROTATION".equals(currentConfig.startingPlayerMode)) {
                rotateStarter();
            } else if ("ALEATOIRE".equals(currentConfig.startingPlayerMode)) {
                currentStarterIndex = (int) (Math.random() * currentConfig.playerCount);
            }
        }
    }

    public void clearCurrentGame(Context context) {
        isGameActive = false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ACTIVE).remove(KEY_CONFIG).remove(KEY_PLAYERS).remove(KEY_ROUND).remove(KEY_STARTER_INDEX).apply();
    }
    
    public void saveToHistory(Context context, String imagePath) {
        if (currentPlayers == null || currentPlayers.isEmpty()) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyStr = prefs.getString(KEY_HISTORY, "[]");
        try {
            JSONArray history = new JSONArray(historyStr);
            JSONObject entry = new JSONObject();
            
            Player winner = getWinner();
            entry.put("winner", winner.getName());
            entry.put("winner_score", winner.getScore());
            entry.put("date", System.currentTimeMillis());
            entry.put("image_path", imagePath);
            if (currentConfig != null) {
                entry.put("game_name", currentConfig.gameName);
            }
            
            // Save full players list (includes round scores)
            JSONArray playersArr = new JSONArray();
            for (Player p : currentPlayers) {
                playersArr.put(p.toJSON());
            }
            entry.put("players", playersArr);
            
            // Save config for reference
            if (currentConfig != null) {
                entry.put("config", currentConfig.toJSON());
            }
            
            history.put(entry);
            
            prefs.edit().putString(KEY_HISTORY, history.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public List<JSONObject> getHistory(Context context) {
         SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
         String historyStr = prefs.getString(KEY_HISTORY, "[]");
         List<JSONObject> list = new ArrayList<>();
         try {
             JSONArray history = new JSONArray(historyStr);
             for(int i=history.length()-1; i>=0; i--) { // Newest first
                 JSONObject obj = history.getJSONObject(i);
                 if (obj.has("winner") && obj.has("date")) { // Validation
                     list.add(obj);
                 }
             }
         } catch(JSONException e) {
             e.printStackTrace();
         }
         return list;
    }

    public void deleteLastHistoryEntry(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyStr = prefs.getString(KEY_HISTORY, "[]");
        try {
            JSONArray history = new JSONArray(historyStr);
            if (history.length() > 0) {
                JSONArray newHistory = new JSONArray();
                // If newest is first, we remove index 0. If newest is last, we remove history.length()-1.
                // Current implementation of getHistory returns reversed.
                // Let's stick to conventional (newest last in string, reversed in getHistory).
                for (int i = 0; i < history.length() - 1; i++) {
                    newHistory.put(history.get(i));
                }
                prefs.edit().putString(KEY_HISTORY, newHistory.toString()).apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isGameActive() { return isGameActive; }
    public GameConfig getCurrentConfig() { return currentConfig; }
    public List<Player> getPlayers() { return currentPlayers; }
    public int getCurrentRound() { return currentRound; }
    
    public Player getWinner() {
        if (currentPlayers == null || currentPlayers.isEmpty()) return null;
        Player winner = currentPlayers.get(0);
        for (Player p : currentPlayers) {
            if (p.getScore() < winner.getScore()) {
                winner = p;
            }
        }
        return winner;
    }
}

package com.example.orchom;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int id;
    private String color;
    private int score;
    private java.util.List<Integer> roundScores;

    public Player(String name, int id, String color) {
        this.name = name;
        this.id = id;
        this.color = color;
        this.score = 0;
        this.roundScores = new java.util.ArrayList<>();
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void addRoundScore(int points) {
        this.roundScores.add(points);
        this.score += points;
    }

    public java.util.List<Integer> getRoundScores() {
        return roundScores;
    }

    public void resetScores() {
        this.score = 0;
        this.roundScores.clear();
    }

    // Getters
    public String getName() { return name; }
    public int getId() { return id; }
    public String getColor() { return color; }
    public int getScore() { return score; }

    // JSON Serialization
    public org.json.JSONObject toJSON() {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("name", name);
            json.put("id", id);
            json.put("color", color);
            json.put("score", score);
            org.json.JSONArray rounds = new org.json.JSONArray();
            for (int rs : roundScores) rounds.put(rs);
            json.put("roundScores", rounds);
            return json;
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Player fromJSON(org.json.JSONObject json) {
        try {
            Player p = new Player(
                json.getString("name"),
                json.getInt("id"),
                json.getString("color")
            );
            p.score = json.getInt("score");
            org.json.JSONArray rounds = json.optJSONArray("roundScores");
            if (rounds != null) {
                for (int i = 0; i < rounds.length(); i++) {
                    p.roundScores.add(rounds.getInt(i));
                }
            }
            return p;
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
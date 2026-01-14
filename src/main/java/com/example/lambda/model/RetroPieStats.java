package com.example.lambda.model;

public class RetroPieStats {
    private String timestamp;
    private String action;
    private String emulator;
    private String game;

    public RetroPieStats() {}

    public RetroPieStats(String timestamp, String action, String emulator, String game) {
        this.timestamp = timestamp;
        this.action = action;
        this.emulator = emulator;
        this.game = game;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEmulator() { return emulator; }
    public void setEmulator(String emulator) { this.emulator = emulator; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
}

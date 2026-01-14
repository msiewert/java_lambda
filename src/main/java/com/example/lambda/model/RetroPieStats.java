package com.example.lambda.model;

public record RetroPieStats(
    String timestamp,
    String action,
    String emulator,
    String game
) {}

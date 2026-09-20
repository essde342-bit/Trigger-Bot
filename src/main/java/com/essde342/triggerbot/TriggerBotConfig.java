package com.essde342.triggerbot;

public class TriggerBotConfig {
    public boolean enabled = true;
    public boolean onlyCrits = false;
    public boolean smartCrits = true;
    public boolean onlyWeapon = false;
    public int triggerDelayMs = 0;
    public double triggerRange = 6.0D;

    public boolean aimAssist = false;
    public int aimAssistDurationMs = 300;
    public double aimAssistRange = 6.0D;

    public boolean lightningEsp = false;
    public boolean targetEsp = false;

    public boolean aspectRatioEnabled = true;
    public double aspectRatio = 1.7777778D;

    public boolean optimization = true;
    public int optimizationLevel = 2;
    public boolean adaptiveOptimization = false;
    public int targetFps = 45;

    public boolean fullbright = false;
    public boolean noHurtCam = true;
    public double fullbrightGamma = 15.0D;
}

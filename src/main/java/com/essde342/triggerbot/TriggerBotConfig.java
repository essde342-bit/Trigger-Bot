package com.essde342.triggerbot;

public class TriggerBotConfig {
    public boolean enabled = true;
    public boolean onlyCrits = false;
    public boolean smartCrits = true;
    public boolean onlyWeapon = false;
    public boolean aimAssist = false;
    public double aspectRatio = 1.7777778D;

    // Custom mobile optimizer.
    public boolean optimization = true;
    public int optimizationLevel = 2; // 0=Balanced, 1=Performance, 2=Extreme
    public boolean adaptiveOptimization = false;
    public int targetFps = 45;

    // Visual modules.
    public boolean fullbright = false;
    public boolean noHurtCam = true;
    public double fullbrightGamma = 15.0D;
}

package com.essde342.triggerbot;

public class TriggerBotConfig {
    public boolean enabled = true;
    public boolean onlyCrits = false;
    public boolean onlyWeapon = false;

    // Custom mobile optimizer.
    public boolean optimization = true;
    public int optimizationLevel = 1; // 0=Balanced, 1=Performance, 2=Extreme
    public boolean adaptiveOptimization = true;
    public int targetFps = 45;
}

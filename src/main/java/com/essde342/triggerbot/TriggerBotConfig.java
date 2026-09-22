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
    public boolean jumpCircle = false;

    public boolean aspectRatioEnabled = false;
    public double aspectRatio = 1.7777778D;

    public boolean optimization = true;
    public int optimizationLevel = 2;
    public boolean adaptiveOptimization = false;
    public int targetFps = 45;

    public boolean fullbright = false;
    public boolean noHurtCam = true;
    public double fullbrightGamma = 1.0D;

    public int bindTriggerBot = -1;
    public int bindAimAssist = -1;
    public int bindLightningEsp = -1;
    public int bindTargetEsp = -1;
    public int bindJumpCircle = -1;
    public int bindFullbright = -1;
    public int bindNoHurtCam = -1;
    public int bindAspectRatio = -1;
    public int bindOptimization = -1;
    public int bindAdaptiveOptimization = -1;
}

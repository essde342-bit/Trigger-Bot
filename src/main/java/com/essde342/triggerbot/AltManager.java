package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public final class AltManager {
    private static final List<String> ALTS = new ArrayList<String>();
    private static final Random RANDOM = new Random();
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static boolean loaded = false;

    private static final String[] NAME_PREFIXES = {
            "Nova", "Shadow", "Frost", "Night", "Sky", "Dark", "Pixel",
            "Ghost", "Rapid", "Silent", "Storm", "Lunar", "Blaze", "Vibe"
    };

    private static final String[] NAME_SUFFIXES = {
            "Fox", "Wolf", "PvP", "Ace", "Rush", "King", "Byte",
            "X", "Pro", "Craft", "Fire", "Max", "Zed", "Play"
    };

    private AltManager() {
    }

    public static void load() {
        if (loaded) {
            return;
        }

        loaded = true;
        ALTS.clear();

        File file = getFile();
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            StringBuilder text = new StringBuilder();
            char[] buffer = new char[512];
            int read;

            while ((read = reader.read(buffer)) != -1) {
                text.append(buffer, 0, read);
            }

            String json = text.toString();
            int cursor = 0;

            while (cursor < json.length()) {
                int quoteStart = json.indexOf('"', cursor);
                if (quoteStart < 0) {
                    break;
                }

                int quoteEnd = json.indexOf('"', quoteStart + 1);
                if (quoteEnd < 0) {
                    break;
                }

                String name = json.substring(quoteStart + 1, quoteEnd);
                if (isValidName(name) && !containsIgnoreCase(name)) {
                    ALTS.add(name);
                }

                cursor = quoteEnd + 1;
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        File file = getFile();
        File parent = file.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("[\n");

            for (int i = 0; i < ALTS.size(); i++) {
                writer.write("  \"" + ALTS.get(i) + "\"");
                if (i + 1 < ALTS.size()) {
                    writer.write(",");
                }
                writer.write("\n");
            }

            writer.write("]\n");
        } catch (IOException ignored) {
        }
    }

    public static boolean add(String name) {
        load();

        String normalized = name == null ? "" : name.trim();
        if (!isValidName(normalized) || containsIgnoreCase(normalized)) {
            return false;
        }

        ALTS.add(normalized);
        save();
        return true;
    }

    public static boolean remove(int index) {
        load();

        if (index < 0 || index >= ALTS.size()) {
            return false;
        }

        ALTS.remove(index);
        save();
        return true;
    }

    public static int size() {
        load();
        return ALTS.size();
    }

    public static String get(int index) {
        load();
        if (index < 0 || index >= ALTS.size()) {
            return "";
        }
        return ALTS.get(index);
    }

    public static List<String> getAll() {
        load();
        return Collections.unmodifiableList(new ArrayList<String>(ALTS));
    }

    public static String randomNickname() {
        load();

        for (int attempt = 0; attempt < 50; attempt++) {
            String prefix = NAME_PREFIXES[RANDOM.nextInt(NAME_PREFIXES.length)];
            String suffix = NAME_SUFFIXES[RANDOM.nextInt(NAME_SUFFIXES.length)];
            String candidate;

            if (RANDOM.nextBoolean()) {
                candidate = prefix + suffix + (10 + RANDOM.nextInt(90));
            } else {
                candidate = prefix + "_" + suffix + RANDOM.nextInt(1000);
            }

            if (candidate.length() > 16) {
                candidate = candidate.substring(0, 16);
            }

            if (isValidName(candidate) && !containsIgnoreCase(candidate)) {
                return candidate;
            }
        }

        return "Player" + (100 + RANDOM.nextInt(900));
    }

    public static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    private static boolean containsIgnoreCase(String name) {
        for (String existing : ALTS) {
            if (existing.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private static File getFile() {
        return new File(
                MinecraftClient.getInstance().runDirectory,
                "config/triggerbot_alts.json"
        );
    }
}

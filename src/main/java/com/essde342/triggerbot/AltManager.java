package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class AltManager {
    private static final List<String> ALTS = new ArrayList<String>();
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static boolean loaded = false;

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
        load();

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

    public static boolean apply(String name) {
        String normalized = name == null ? "" : name.trim();

        if (!isValidName(normalized)) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) {
            return false;
        }

        Session current = client.getSession();
        String accountType = ((SessionAccessor) (Object) current)
                .triggerBot$getAccountType()
                .name()
                .toLowerCase(java.util.Locale.ROOT);

        Session replacement = new Session(
                normalized,
                current.getUuid(),
                current.getAccessToken(),
                accountType
        );

        ((MinecraftClientAccessor) (Object) client).triggerBot$setSession(replacement);
        return normalized.equals(client.getSession().getUsername());
    }

    public static String currentNickname() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) {
            return "";
        }
        return client.getSession().getUsername();
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

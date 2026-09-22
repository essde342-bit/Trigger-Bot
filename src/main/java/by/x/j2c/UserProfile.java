package by.x.j2c;

import net.minecraft.client.MinecraftClient;

public final class UserProfile {
    public static final UserProfile instance = new UserProfile();

    private UserProfile() {}

    public String username() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getSession().getUsername();
    }
}
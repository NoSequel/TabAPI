package io.github.nosequel.tab.v1_8_r3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.nosequel.tab.shared.TabAdapter;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class v1_8_R3TabAdapter extends TabAdapter {

    private final GameProfile[] profiles = new GameProfile[80];
    private final List<Player> initialized = new ArrayList<>();
    private final JavaPlugin plugin;

    public v1_8_R3TabAdapter(JavaPlugin plugin) {
        this.setupProfiles();
        this.plugin = plugin;
    }

    /**
     * Send a packet to the player
     *
     * @param player the player
     * @param packet the packet to send
     */
    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Check if the player should be able to see the fourth row
     *
     * @param player the player
     * @return whether they should be able to see the fourth row
     */
    @Override
    public int getMaxElements(Player player) {
        return 80;
    }

    /**
     * Send an entry's data to a player
     *
     * @param player   the player
     * @param axis     the axis of the entry
     * @param ping     the ping to display on the entry's position
     * @param text     the text to display on the entry's position
     * @param skinData the data to change the entity's skin to
     * @return the current adapter instance
     */
    @Override
    public TabAdapter sendEntryData(Player player, int axis, int ping, String text, String[] skinData) {
        final GameProfile profile = this.profiles[axis];
        final EntityPlayer entityPlayer = this.getEntityPlayer(profile);

        entityPlayer.ping = ping;
        entityPlayer.listName = new ChatComponentText(text);

        if (skinData.length >= 2 && !skinData[0].isEmpty() && !skinData[1].isEmpty()) {
            profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));
        }

        this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, entityPlayer);
        this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, entityPlayer);

        return this;
    }

    /**
     * Add fake players to the player's tablist
     *
     * @param player the player to send the fake players to
     * @return the current adapter instance
     */
    @Override
    public TabAdapter addFakePlayers(Player player) {
        if(!initialized.contains(player)) {
            for (int i = 0; i < 80; i++) {
                final GameProfile profile = this.profiles[i];
                final EntityPlayer entityPlayer = this.getEntityPlayer(profile);

                this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
            }

            initialized.add(player);
        }

        return this;
    }

    /**
     * Get an entity player by a profile
     *
     * @param profile the profile
     * @return the entity player
     */
    private EntityPlayer getEntityPlayer(GameProfile profile) {
        final MinecraftServer server = MinecraftServer.getServer();
        final PlayerInteractManager interactManager = new PlayerInteractManager(server.getWorldServer(0));

        return new EntityPlayer(server, server.getWorldServer(0), profile, interactManager);
    }

    /**
     * Hide all real players from the tab
     *
     * @param player the player
     * @return the current adapter instance
     */
    @Override
    public TabAdapter hideRealPlayers(Player player) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if ((player.canSee(target) || player.equals(target))) {
                this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, target);
                this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, target);

                Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, target), 2L);
            }
        }

        return this;
    }

    /**
     * Show all real players on the tab
     *
     * @param player the player
     * @return the current adapter instance
     */
    @Override
    public TabAdapter showRealPlayers(Player player) {
        return this;
    }

    /**
     * Send the {@link PacketPlayOutPlayerInfo} to a player
     *
     * @param player the player
     * @param action the action
     * @param target the target
     */
    private void sendInfoPacket(Player player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, EntityPlayer target) {
        this.sendPacket(player, new PacketPlayOutPlayerInfo(action, target));
    }

    /**
     * Send the {@link PacketPlayOutPlayerInfo} to a player
     *
     * @param player the player
     * @param action the action
     * @param target the target
     */
    private void sendInfoPacket(Player player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, Player target) {
        this.sendInfoPacket(player, action, ((CraftPlayer) target).getHandle());
    }

    /**
     * Create a new game profile
     *
     * @param index the index of the profile
     * @param text  the text to display
     * @return the current adapter instance
     */
    @Override
    public TabAdapter createProfiles(int index, String text) {
        final GameProfile profile = new GameProfile(UUID.randomUUID(), text);
        final String[] skinData = new String[] {
                "eyJ0aW1lc3RhbXAiOjE1MTE3Mzk5MDc1MzksInByb2ZpbGVJZCI6IjIzZDE4YjNhN2E1NjQyM2E4NDZmZGJlNGVjYjJmNzJmIiwicHJvZmlsZU5hbWUiOiJHZW1pbml4UGxheXMiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E1NmUyMWE5MzYyODE1ZDJiNzJiNjg5ZTc2NmZhZmQzYmVlY2U5OTRjM2QxMDI3ODg3ZjM3MmEyZjkyOWZmMyJ9fX0=",
                "IOC895nkomyPq/eU8RNbWV543JMrY6we0XPyqaZ4i1mFW+wtv6GRx6fB6/N8QM+FgF9l6lqoADeij8tbJoqvmOYp4zvcE0B3zVmlH9si61V//6uAxzYTZNZUymKENI9rTv6PS9YasvnN2ybcARe0P+C9tVPE1rUcyL6PUObW9vew3yT9XVRJDuv5NEySOWHr+q+tG7xuOH5c+1h1HX+Lnmpg/lMqJvkfNbBGcVtbvcyHUCslwx0b6o03AbJ+lfPyRJ4S4VB9X0UJFSC6aGG5vGijGYatrwcCBB1HKqRVyF0AzVZ4rNmDeHGBvXDWwrYAF0K8Bny4QBHQUctJKCiYVF5hk6gkQPABxKsMDMqe3tK6Zs7riI28L1JXGxjG4EsnsG9r+bWawNrJXUJnLxD3vG4Wq7EXVBwTKt3a5SzV5MtWVHwQ66ROQCOjgIc/BHgFwQkEk01S08u1zH3PECqgcWnFyUQeq/ujIuxftz5i0NS2YiMLXAevx1jGavOl330FXaKWJ6j4RTaUVO7c8iPLo1kr4p+pcrIVdGjDSLYjI1N4R3M3EmKipcrOzqj6MPDU/qFRKYPKFcIf6Yt5IYnSUzC86piomaiks/A13YbhyIij/DWMu9tGZZgf4r1Ev/kprHJDRSM/1uwAZAkUgk0qVha/vIu8DhqtI8EGbvibM5o="
        };

        profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));

        this.profiles[index] = profile;

        return this;
    }
}
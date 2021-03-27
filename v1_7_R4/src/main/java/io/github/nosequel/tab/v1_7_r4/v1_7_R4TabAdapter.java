package io.github.nosequel.tab.v1_7_r4;

import io.github.nosequel.tab.shared.TabAdapter;
import io.github.nosequel.tab.shared.skin.SkinType;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.PacketPlayOutRespawn;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class v1_7_R4TabAdapter extends TabAdapter {

    private final Map<Player, GameProfile[]> profiles = new HashMap<>();
    private final List<Player> initialized = new ArrayList<>();

    /**
     * Send a packet to the player
     *
     * @param player the player
     * @param packet the packet to send
     */
    private void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }


    /**
     * Create a new game profile
     *
     * @param index  the index of the profile
     * @param text   the text to display
     * @param player the player to make the profiles for
     */
    @Override
    public void createProfiles(int index, String text, Player player) {
        if (!this.profiles.containsKey(player)) {
            this.profiles.put(player, new GameProfile[80]);
        }

        if (this.profiles.get(player).length < index + 1) {
            final GameProfile profile = new GameProfile(UUID.randomUUID(), text);
            final String[] skinData = SkinType.DARK_GRAY.getSkinData();

            profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));

            this.profiles.get(player)[index] = profile;
        }
    }

    /**
     * Send the header and footer to a player
     *
     * @param player the player to send the header and footer to
     * @param header the header to send
     * @param footer the footer to send
     * @return the current adapter instance
     */
    @Override
    public TabAdapter sendHeaderFooter(Player player, String header, String footer) {
        if(this.getMaxElements(player) != 60 && (header != null || footer != null)) {
            this.sendPacket(player, new ProtocolInjector.PacketTabHeader(
                    ChatSerializer.a("{text:\"" + StringEscapeUtils.escapeJava(header) + "\"}"),
                    ChatSerializer.a("{text:\"" + StringEscapeUtils.escapeJava(footer) + "\"}")
            ));
        }

        return this;
    }


    /**
     * Update the skin on the tablist for a player
     *
     * @param skinData the data of the new skin
     * @param index    the index of the profile
     * @param player   the player to update the skin for
     */
    @Override
    public void updateSkin(String[] skinData, int index, Player player) {
        final GameProfile profile = this.profiles.get(player)[index];
        final Property property = profile.getProperties().get("textures").iterator().next();
        final EntityPlayer entityPlayer = this.getEntityPlayer(profile);

        skinData = skinData != null && skinData.length >= 1 && !skinData[0].isEmpty() && !skinData[1].isEmpty()
                ? skinData
                : SkinType.DARK_GRAY.getSkinData();

        if (!property.getSignature().equals(skinData[1]) || !property.getValue().equals(skinData[0])) {
            profile.getProperties().remove("textures", property);
            profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));

            this.sendPacket(player, PacketPlayOutPlayerInfo.addPlayer(entityPlayer));
        }
    }

    /**
     * Check if the player should be able to see the fourth row
     *
     * @param player the player
     * @return whether they should be able to see the fourth row
     */
    @Override
    public int getMaxElements(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() > 5 ? 80 : 60;
    }

    /**
     * Send an entry's data to a player
     *
     * @param player   the player
     * @param axis     the axis of the entry
     * @param ping     the ping to display on the entry's position
     * @param text     the text to display on the entry's position
     * @return the current adapter instance
     */
    @Override
    public TabAdapter sendEntryData(Player player, int axis, int ping, String text) {
        final GameProfile profile = this.profiles.get(player)[axis];
        final EntityPlayer entityPlayer = this.getEntityPlayer(profile);

        entityPlayer.ping = ping;

        this.setupScoreboard(player, text, profile.getName());
        this.sendPacket(player, PacketPlayOutPlayerInfo.updatePing(entityPlayer));

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
        if(!this.initialized.contains(player)) {
            for (int i = 0; i < this.getMaxElements(player); i++) {
                final GameProfile profile = this.profiles.get(player)[i];
                final EntityPlayer entityPlayer = this.getEntityPlayer(profile);

                this.sendPacket(player, PacketPlayOutPlayerInfo.addPlayer(entityPlayer));
            }

            this.initialized.add(player);
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
        for (Player target : Bukkit.matchPlayer("")) {
            if(player.canSee(target) || player.equals(target)) {
                this.sendPacket(player, PacketPlayOutPlayerInfo.removePlayer(((CraftPlayer) target).getHandle()));
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
        if (!this.initialized.contains(player)) {
            final PlayerConnection connection = this.getPlayerConnection(player);
            final NetworkManager networkManager = connection.networkManager;

            try {
                final Field outgoingQueueField = networkManager.getClass().getDeclaredField("k");
                outgoingQueueField.setAccessible(true);

                ((Queue<?>) outgoingQueueField.get(networkManager)).removeIf(object -> {
                    if (object != null) {
                        if (object instanceof PacketPlayOutNamedEntitySpawn) {
                            this.handlePacketPlayOutNamedEntitySpawn(player, (PacketPlayOutNamedEntitySpawn) object);
                            return true;
                        } else if (object instanceof PacketPlayOutRespawn) {
                            this.handlePacketPlayOutRespawn(player);
                            return true;
                        }
                    }

                    return false;
                });
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    /**
     * Handle an {@link PacketPlayOutNamedEntitySpawn} packet
     *
     * @param player the player to handle it for
     * @param packet the packet to handle
     */
    private void handlePacketPlayOutNamedEntitySpawn(Player player, PacketPlayOutNamedEntitySpawn packet) {
        try {
            final Field gameProfileField = packet.getClass().getDeclaredField("b");
            gameProfileField.setAccessible(true);

            final Player target = Bukkit.getPlayer(((GameProfile) gameProfileField.get(packet)).getId());

            if (target != null) {
                sendPacket(player, PacketPlayOutPlayerInfo.addPlayer(getEntityPlayer(target)));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle an {@link PacketPlayOutRespawn} packet
     *
     * @param player the player to handle it for
     */
    private void handlePacketPlayOutRespawn(Player player) {
        this.sendPacket(player, PacketPlayOutPlayerInfo.addPlayer(this.getEntityPlayer(player)));
    }

    /**
     * Get the {@link PlayerConnection} of a player
     *
     * @param player the player to get the player connection object from
     * @return the object
     */
    private PlayerConnection getPlayerConnection(Player player) {
        return this.getEntityPlayer(player).playerConnection;
    }

    /**
     * Get the {@link EntityPlayer} object of a player
     *
     * @param player the player to get the entity player object from
     * @return the entity player
     */
    private EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }


}
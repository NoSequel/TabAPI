package io.github.nosequel.tab.v1_7_r4;

import io.github.nosequel.tab.shared.TabAdapter;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class v1_7_R4TabAdapter extends TabAdapter {

    private final GameProfile[] profiles = new GameProfile[80];
    private final List<Player> initialized = new ArrayList<>();

    public v1_7_R4TabAdapter() {
        this.setupProfiles();
    }

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
     * @param skinData the data to change the entity's skin to
     * @return the current adapter instance
     */
    @Override
    public TabAdapter sendEntryData(Player player, int axis, int ping, String text, String[] skinData) {
        final GameProfile profile = this.profiles[axis];
        final EntityPlayer entityPlayer = this.getEntityPlayer(profile);

        entityPlayer.ping = ping;
        entityPlayer.listName = text;

        if(skinData.length >= 2 && !skinData[0].isEmpty() && !skinData[1].isEmpty()) {
            profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));
        }

        this.sendPacket(player, PacketPlayOutPlayerInfo.updateDisplayName(entityPlayer));
        this.sendPacket(player, PacketPlayOutPlayerInfo.updatePing(entityPlayer));
        this.showRealPlayers(player);

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
            for (int i = 0; i < 80; i++) {
                final GameProfile profile = this.profiles[i];
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
        if(!this.initialized.contains(player)) {
            final PlayerConnection connection = this.getPlayerConnection(player);
            final NetworkManager networkManager = connection.networkManager;

            try {
                final Queue<?> outgoingQueue = (Queue<?>) networkManager.getClass().getDeclaredField("k").get(networkManager);

                outgoingQueue.removeIf(object ->
                        Objects.nonNull(object) &&
                                object instanceof PacketPlayOutNamedEntitySpawn &&
                                this.handlePacketPlayOutNamedEntitySpawn(player, (PacketPlayOutNamedEntitySpawn) object)
                );
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
     * @return always true
     */
    private boolean handlePacketPlayOutNamedEntitySpawn(Player player, PacketPlayOutNamedEntitySpawn packet) {
        try {
            final Field uuidField = packet.getClass().getDeclaredField("b");
            uuidField.setAccessible(true);

            final Player target = Bukkit.getPlayer((UUID) uuidField.get(packet));

            if (target != null) {
                sendPacket(player, PacketPlayOutPlayerInfo.addPlayer(getEntityPlayer(player)));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return true;
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
                "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=",
                "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw="
        };

        profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));

        this.profiles[index] = profile;

        return this;
    }
}
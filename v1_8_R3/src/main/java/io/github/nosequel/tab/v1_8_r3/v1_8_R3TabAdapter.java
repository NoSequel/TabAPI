package io.github.nosequel.tab.v1_8_r3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.nosequel.tab.shared.TabAdapter;
import io.github.nosequel.tab.shared.skin.SkinType;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PacketPlayOutRespawn;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class v1_8_R3TabAdapter extends TabAdapter {

    private final GameProfile[] profiles = new GameProfile[80];
    private final List<Player> initialized = new ArrayList<>();

    public v1_8_R3TabAdapter() {
        this.setupProfiles();
    }

    /**
     * Send a packet to the player
     *
     * @param player the player
     * @param packet the packet to send
     */
    private void sendPacket(Player player, Packet<?> packet) {
        this.getPlayerConnection(player).sendPacket(packet);
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
        if (header != null || footer != null) {
            final Packet<?> packet = new PacketPlayOutPlayerListHeaderFooter(
                    IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}")
            );

            try {
                final Field footerField = packet.getClass().getDeclaredField("b");

                footerField.setAccessible(true);
                footerField.set(packet, IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}"));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }

            this.sendPacket(player, packet);
        }

        return this;
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
        final Property property = profile.getProperties().get("textures").iterator().next();

        skinData = skinData != null && skinData.length >= 1 && !skinData[0].isEmpty() && !skinData[1].isEmpty()
                ? skinData
                : SkinType.DARK_GRAY.getSkinData();

        if (!property.getSignature().equals(skinData[1]) || !property.getValue().equals(skinData[0])) {
            profile.getProperties().remove("textures", property);
            profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));

            this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        }

        final String name = profile.getName();
        final String[] splitText = this.splitText(text);

        final Scoreboard scoreboard = player.getScoreboard() == null
                ? Bukkit.getScoreboardManager().getNewScoreboard()
                : player.getScoreboard();

        final Team team = scoreboard.getTeam(name) == null
                ? scoreboard.registerNewTeam(name)
                : scoreboard.getTeam(name);

        if (!team.hasEntry(name)) {
            team.addEntry(name);
        }

        team.setPrefix(splitText[0]);
        team.setSuffix(splitText[1]);

        player.setScoreboard(scoreboard);


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
        for(Player target : Bukkit.getOnlinePlayers()) {
            this.sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, target);
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
            this.getPlayerConnection(player).networkManager.channel.pipeline().addBefore(
                    "packet_handler",
                    player.getName(),
                    this.createShowListener(player)
            );
        }

        return this;
    }

    /**
     * Create the listener required to show the players
     *
     * @param player the player to create it for
     * @return the handler
     */
    private ChannelDuplexHandler createShowListener(Player player) {
        return new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
                if (packet instanceof PacketPlayOutNamedEntitySpawn) {
                    final PacketPlayOutNamedEntitySpawn entitySpawn = (PacketPlayOutNamedEntitySpawn) packet;
                    final Field uuidField = entitySpawn.getClass().getDeclaredField("b");

                    uuidField.setAccessible(true);

                    final Player target = Bukkit.getPlayer((UUID) uuidField.get(entitySpawn));

                    if (target != null) {
                        sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, target);
                    }
                } else if (packet instanceof PacketPlayOutRespawn) {
                    sendInfoPacket(player, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player);
                }

                super.write(context, packet, promise);
            }
        };
    }

    /**
     * Get the {@link PlayerConnection} of a player
     *
     * @param player the player to get the player connection object from
     * @return the object
     */
    private PlayerConnection getPlayerConnection(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection;
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
        final String[] skinData = SkinType.DARK_GRAY.getSkinData();

        profile.getProperties().put("textures", new Property("textures", skinData[0], skinData[1]));

        this.profiles[index] = profile;

        return this;
    }
}
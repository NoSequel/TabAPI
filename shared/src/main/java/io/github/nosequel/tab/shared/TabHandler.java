package io.github.nosequel.tab.shared;

import io.github.nosequel.tab.shared.entry.TabElement;
import io.github.nosequel.tab.shared.entry.TabElementHandler;
import io.github.nosequel.tab.shared.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class TabHandler {

    private final TabAdapter adapter;
    private final TabElementHandler handler;

    private final long ticks;

    /**
     * Constructor to make a new tab handler
     *
     * @param adapter the adapter to send the tab with
     * @param handler the handler to get the elements from
     * @param plugin  the plugin to register the thread to
     * @param ticks   the amount it should update
     */
    public TabHandler(TabAdapter adapter, TabElementHandler handler, JavaPlugin plugin, long ticks) {
        this.adapter = adapter;
        this.handler = handler;
        this.ticks = ticks;

        new TabRunnable(this).runTaskTimer(plugin, 0L, ticks);

        // register listener for hiding players from tab
        //Bukkit.getPluginManager().registerEvents(new PlayerListener(this), plugin);
    }

    /**
     * Update the tablist for a player
     *
     * @param player the player to update it for
     */
    public void sendUpdate(Player player) {
        final TabElement tabElement = this.handler.getElement(player);

        this.adapter
                .setupProfiles(player)
                .showRealPlayers(player).addFakePlayers(player)
                .hideRealPlayers(player).handleElement(player, tabElement)
                .sendHeaderFooter(player, tabElement.getHeader(), tabElement.getFooter());
    }

}
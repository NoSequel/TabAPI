package io.github.nosequel.tab.shared;

import io.github.nosequel.tab.shared.TabHandler;
import io.github.nosequel.tab.shared.entry.TabElement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TabRunnable extends BukkitRunnable {

    private final TabHandler handler;

    /**
     * Constructor to make a new TabThread
     *
     * @param handler the handler to register it to
     */
    public TabRunnable(TabHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final TabElement tabElement = this.handler.getHandler().getElement(player);

            this.handler.getAdapter()
                    .setupProfiles(player)
                    .showRealPlayers(player).addFakePlayers(player)
                    .hideRealPlayers(player).handleElement(player, tabElement)
                    .sendHeaderFooter(player, tabElement.getHeader(), tabElement.getFooter());
        }

    }
}
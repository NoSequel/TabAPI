package io.github.nosequel.tab.shared.thread;

import io.github.nosequel.tab.shared.TabHandler;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class TabRunnable extends BukkitRunnable {

    private final TabHandler handler;

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> handler.getAdapter()
                .showRealPlayers(player)
                .hideRealPlayers(player)
                .addFakePlayers(player)
                .handleElement(player, handler.getHandler().getElement(player)));
    }
}
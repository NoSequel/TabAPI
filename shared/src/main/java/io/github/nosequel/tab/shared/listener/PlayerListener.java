package io.github.nosequel.tab.shared.listener;

import io.github.nosequel.tab.shared.TabAdapter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final TabAdapter adapter;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            adapter.hideRealPlayers(player);
        }
    }
}
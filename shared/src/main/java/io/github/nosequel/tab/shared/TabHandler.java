package io.github.nosequel.tab.shared;

import io.github.nosequel.tab.shared.entry.TabElementHandler;
import io.github.nosequel.tab.shared.thread.ThreadRunnable;
import lombok.Getter;
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

        new ThreadRunnable(this).runTaskTimer(plugin, 0L, ticks);
    }
}
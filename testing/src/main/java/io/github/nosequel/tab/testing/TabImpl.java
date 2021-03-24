package io.github.nosequel.tab.testing;

import io.github.nosequel.tab.shared.entry.TabElement;
import io.github.nosequel.tab.shared.entry.TabElementHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TabImpl implements TabElementHandler {

    /**
     * Get the tab element of a player
     *
     * @param player the player
     * @return the element
     */
    @Override
    public TabElement getElement(Player player) {
        final TabElement element = new TabElement();

        for (int i = 0; i < 80; i++) {
            final int x = i % 4;
            final int y = i / 4;

            element.add(x, y, ChatColor.GREEN + "Slot: " + ChatColor.GRAY + x + ", " + y);
        }

        return element;
    }
}
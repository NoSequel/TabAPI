package io.github.nosequel.tab.shared;

import io.github.nosequel.tab.shared.entry.TabElement;
import io.github.nosequel.tab.shared.entry.TabEntry;
import org.bukkit.entity.Player;

public abstract class TabAdapter {

    /**
     * Setup the profiles of the tab adapter
     */
    public void setupProfiles() {
        for (int axis = 0; axis < 80; axis++) {
            final int x = axis % 4;
            final int y = axis / 4;
            final String text = "§0§" + x + (y > 9
                    ? "§" + String.valueOf(y).toCharArray()[0] + "§" + String.valueOf(y).toCharArray()[1]
                    : "§0§" + String.valueOf(y).toCharArray()[0]
            );

            this.createProfiles(axis, text);
        }
    }

    /**
     * Handle an element being send to a player
     *
     * @param player  the player
     * @param element the element to send
     */
    public TabAdapter handleElement(Player player, TabElement element) {
        for (int axis = 0; axis < this.getMaxElements(player); axis++) {
            final int x = axis % (this.getMaxElements(player)/20);
            final int y = axis / (this.getMaxElements(player)/20);

            final TabEntry entry = element.getEntry(x, y);

            this.sendEntryData(player, axis, entry.getPing(), entry.getText(), entry.getSkinData());
        }

        return this;
    }


    /**
     * Clear a player's tab
     *
     * @param player the player who's tab to clear
     * @return the current adapter instance
     */
    public TabAdapter clearTab(Player player) {
        for (int i = 0; i < this.getMaxElements(player); i++) {
            this.sendEntryData(player, i, -1, "", new String[]{});
        }

        return this;
    }

    /**
     * Check if the player should be able to see the fourth row
     *
     * @param player the player
     * @return whether they should be able to see the fourth row
     */
    public abstract int getMaxElements(Player player);

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
    public abstract TabAdapter sendEntryData(Player player, int axis, int ping, String text, String[] skinData);

    /**
     * Hide all real players from the tab
     *
     * @param player the player
     * @return the current adapter instance
     */
    public abstract TabAdapter hideRealPlayers(Player player);

    /**
     * Create a new game profile
     *
     * @param index the index of the profile
     * @param text  the text to display
     * @return the current adapter instance
     */
    public abstract TabAdapter createProfiles(int index, String text);

}
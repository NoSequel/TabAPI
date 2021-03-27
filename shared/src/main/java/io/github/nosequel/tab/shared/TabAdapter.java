package io.github.nosequel.tab.shared;

import io.github.nosequel.tab.shared.entry.TabElement;
import io.github.nosequel.tab.shared.entry.TabEntry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
        final int rows = this.getMaxElements(player) / 20;

        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < rows; x++) {
                final TabEntry entry = element.getEntry(x, y);

                this.sendEntryData(player, y * rows + x, entry.getPing(), entry.getText(), entry.getSkinData());
            }
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
     * Split the text to display on the tablist
     *
     * @param text the text to split
     * @return the split text
     */
    public String[] splitText(String text) {
        if (text.length() < 17) {
            return new String[]{text, ""};
        } else {
            final String left = text.substring(0, 16);
            final String right = text.substring(16);

            if (left.endsWith("§")) {
                return new String[]{left.substring(0, left.toCharArray().length - 1), StringUtils.left(ChatColor.getLastColors(left) + "§" + right, 16)};
            } else {
                return new String[]{left, StringUtils.left(ChatColor.getLastColors(left) + right, 16)};
            }
        }
    }

    /**
     * Setup the scoreboard for the player
     *
     * @param player the player to setup the scoreboard for
     * @param text   the text to display
     * @param name   the name of the team
     */
    public void setupScoreboard(Player player, String text, String name) {
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
    }

    /**
     * Send the header and footer to a player
     *
     * @param player the player to send the header and footer to
     * @param header the header to send
     * @param footer the footer to send
     * @return the current adapter instance
     */
    public abstract TabAdapter sendHeaderFooter(Player player, String header, String footer);

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
     * Add fake players to the player's tablist
     *
     * @param player the player to send the fake players to
     * @return the current adapter instance
     */
    public abstract TabAdapter addFakePlayers(Player player);

    /**
     * Hide all real players from the tab
     *
     * @param player the player
     * @return the current adapter instance
     */
    public abstract TabAdapter hideRealPlayers(Player player);

    /**
     * Show all real players on the tab
     *
     * @param player the player
     * @return the current adapter instance
     */
    public abstract TabAdapter showRealPlayers(Player player);

    /**
     * Create a new game profile
     *
     * @param index the index of the profile
     * @param text  the text to display
     * @return the current adapter instance
     */
    public abstract TabAdapter createProfiles(int index, String text);

}
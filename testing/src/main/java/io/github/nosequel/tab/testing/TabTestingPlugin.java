package io.github.nosequel.tab.testing;

import io.github.nosequel.tab.shared.TabHandler;
import io.github.nosequel.tab.v1_8_r3.v1_8_R3TabAdapter;
import org.bukkit.plugin.java.JavaPlugin;

public class TabTestingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new TabHandler(new v1_8_R3TabAdapter(), new TabImpl(), this, 20L);
    }

}

package io.github.nosequel.tab.testing;

import io.github.nosequel.tab.shared.TabHandler;
import io.github.nosequel.tab.v1_7_r4.v1_7_R4TabAdapter;
import org.bukkit.plugin.java.JavaPlugin;

public class TabTestingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new TabHandler(new v1_7_R4TabAdapter(), new TabImpl(), this, 20L);
    }

}

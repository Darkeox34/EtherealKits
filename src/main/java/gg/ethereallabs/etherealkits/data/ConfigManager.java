package gg.ethereallabs.etherealkits.data;

import gg.ethereallabs.etherealkits.EtherealKits;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public void loadConfig() {
        Configs.setPrefix(config.getString("messages.prefix"));
    }
}

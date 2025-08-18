package xyz.hashdog.rdm.ui.common;

import javafx.collections.ObservableMap;
import xyz.hashdog.rdm.ui.entity.config.ConfigSettings;
import xyz.hashdog.rdm.ui.entity.config.ConnectionServerNode;

import java.util.Map;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 16:46
 */
public class ConfigPreferences {

    private ObservableMap<String, ConnectionServerNode> connectionNodeMap;
    private ObservableMap<String, ConfigSettings> configSettingsMap;

    public ObservableMap<String, ConnectionServerNode> getConnectionNodeMap() {
        return connectionNodeMap;
    }

    protected void setConnectionNodeMap(ObservableMap<String, ConnectionServerNode> connectionNodeMap) {
        this.connectionNodeMap = connectionNodeMap;
    }

    public ObservableMap<String, ConfigSettings> getConfigSettingsMap() {
        return configSettingsMap;
    }

    protected void setConfigSettingsMap(ObservableMap<String, ConfigSettings> configSettingsMap) {
        this.configSettingsMap = configSettingsMap;
    }
}

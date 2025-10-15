package redisfx.tanh.rdm.ui.common;

import javafx.collections.ObservableMap;
import redisfx.tanh.rdm.ui.entity.config.ConfigSettings;
import redisfx.tanh.rdm.ui.entity.config.ConnectionServerNode;

/**
 * 配置文件
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 16:46
 */
public class ConfigPreferences {

    /**
     * 连接节点
     */
    private ObservableMap<String, ConnectionServerNode> connectionNodeMap;
    /**
     * 配置项
     */
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

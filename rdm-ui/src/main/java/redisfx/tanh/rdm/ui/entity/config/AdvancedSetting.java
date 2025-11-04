package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;

import java.util.Objects;

public class AdvancedSetting implements ConfigSettings {

    /**
     * 连接超时
     */
    private int connectionTimeout;
    /**
     * 读超时
     */
    private int soTimeout;
    /**
     * key 分隔符
     */
    private String keySeparator;
    /**
     * key 分隔符正则
     */
    private boolean keySeparatorRegex;
    /**
     * 是否树形显示
     */
    private boolean treeShow;

    /**
     * 版本
     */
    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getName() {
        return ConfigSettingsEnum.ADVANCED.name;
    }

    @Override
    public AdvancedSetting init() {
        this.connectionTimeout = 6000;
        this.soTimeout = 6000;
        this.keySeparator = ":";
        this.keySeparatorRegex = false;
        this.treeShow = true;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getKeySeparator() {
        return keySeparator;
    }

    public void setKeySeparator(String keySeparator) {
        this.keySeparator = keySeparator;
    }

    public boolean isTreeShow() {
        return treeShow;
    }

    public void setTreeShow(boolean treeShow) {
        this.treeShow = treeShow;
    }

    public boolean getKeySeparatorRegex() {
        return keySeparatorRegex;
    }

    public void setKeySeparatorRegex(boolean keySeparatorRegex) {
        this.keySeparatorRegex = keySeparatorRegex;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AdvancedSetting that)) {
            return false;
        }
        return connectionTimeout == that.connectionTimeout && soTimeout == that.soTimeout && treeShow == that.treeShow && version == that.version && Objects.equals(keySeparator, that.keySeparator) && Objects.equals(keySeparatorRegex, that.keySeparatorRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionTimeout, soTimeout, keySeparator, keySeparatorRegex, treeShow, version);
    }
}

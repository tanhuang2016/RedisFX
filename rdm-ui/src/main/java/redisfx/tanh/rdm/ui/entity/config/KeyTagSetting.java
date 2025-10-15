package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.common.KeyTypeTagEnum;

import java.util.List;
import java.util.Objects;

public class KeyTagSetting implements ConfigSettings{

    private List<String> tags;
    private List<String> colors;
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
        return ConfigSettingsEnum.KEY_TAG.name;
    }

    @Override
    public ConfigSettings init() {
        this.tags= KeyTypeTagEnum.tags();
        this.colors= KeyTypeTagEnum.colors();
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KeyTagSetting that = (KeyTagSetting) o;
        return version == that.version && Objects.equals(tags, that.tags) && Objects.equals(colors, that.colors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags, colors, version);
    }
}

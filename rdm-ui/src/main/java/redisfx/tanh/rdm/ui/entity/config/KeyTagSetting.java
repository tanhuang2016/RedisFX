package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.common.KeyTypeTagEnum;

import java.util.List;

public class KeyTagSetting implements ConfigSettings{

    private List<String> tags;
    private List<String> colors;

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
}

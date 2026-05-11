package redisfx.tanh.rdm.ui.entity.config;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
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

    private  Double tagMaxWidth= null;

    public double getMaxWidth() {
        if(tagMaxWidth!=null){
            return tagMaxWidth;
        }
        double mwidth = 0;
        Text textHelper = new Text();
        textHelper.setFont(Font.font(12));
        for (int i = 0; i < getTags().size()-1; i++) {
            String settingTag=getTags().get(i);
            textHelper.setText(settingTag);
            double width = textHelper.getLayoutBounds().getWidth();
            if (width > mwidth) {
                mwidth = width;
            }
        }
        this.tagMaxWidth = mwidth;
        return mwidth;
    }
}

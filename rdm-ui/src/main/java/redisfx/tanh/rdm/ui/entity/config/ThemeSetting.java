package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;

import java.util.Objects;

public  class ThemeSetting implements ConfigSettings{
    private String colorTheme;
//    private String accentColor=ThemeManager.getInstance().getAccentColor().primaryColor().toString() ;
    private String font ;
    private Integer fontSize;
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

    public String getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(String colorTheme) {
        this.colorTheme = colorTheme;
    }

//    public String getAccentColor() {
//        return accentColor;
//    }
//
//    public void setAccentColor(String accentColor) {
//        this.accentColor = accentColor;
//    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public ThemeSetting init(){
        colorTheme=ThemeManager.getInstance().getDefaultTheme().getName();
        font = ThemeManager.DEFAULT_FONT_FAMILY_NAME;
        fontSize = ThemeManager.DEFAULT_FONT_SIZE;
        return this;
    }

    @Override
    public String getName() {
        return ConfigSettingsEnum.THEME.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ThemeSetting that = (ThemeSetting) o;
        return version == that.version && Objects.equals(colorTheme, that.colorTheme) && Objects.equals(font, that.font) && Objects.equals(fontSize, that.fontSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colorTheme, font, fontSize, version);
    }
}

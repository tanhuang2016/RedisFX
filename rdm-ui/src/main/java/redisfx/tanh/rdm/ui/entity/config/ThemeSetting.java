package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;

public  class ThemeSetting implements ConfigSettings{
    private String colorTheme;
//    private String accentColor=ThemeManager.getInstance().getAccentColor().primaryColor().toString() ;
    private String font ;
    private Integer fontSize;

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
}

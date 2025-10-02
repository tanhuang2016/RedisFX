package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;

import java.util.Locale;

public class LanguageSetting implements ConfigSettings{

    /**
     * 连接超时
     */
    private String localLanguage;
    private String localCountry;


    @Override
    public String getName() {
        return ConfigSettingsEnum.LANGUAGE.name;
    }

    @Override
    public LanguageSetting init() {
        this.localLanguage= Locale.getDefault().getLanguage();
        this.localCountry= Locale.getDefault().getCountry();
        return this;
    }

    public String getLocalLanguage() {
        return localLanguage;
    }

    public void setLocalLanguage(String localLanguage) {
        this.localLanguage = localLanguage;
    }

    public String getLocalCountry() {
        return localCountry;
    }

    public void setLocalCountry(String localCountry) {
        this.localCountry = localCountry;
    }
}

package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;

import java.util.Locale;
import java.util.Objects;

public class LanguageSetting implements ConfigSettings{

    /**
     * 连接超时
     */
    private String localLanguage;
    private String localCountry;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LanguageSetting that = (LanguageSetting) o;
        return version == that.version && Objects.equals(localLanguage, that.localLanguage) && Objects.equals(localCountry, that.localCountry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localLanguage, localCountry, version);
    }
}

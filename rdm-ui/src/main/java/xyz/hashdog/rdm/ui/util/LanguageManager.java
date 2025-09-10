package xyz.hashdog.rdm.ui.util;

import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.Main;

import java.util.*;

/**
 * @author th
 */
public class LanguageManager {

    public static final String BASE_NAME = "i18n.messages";

    public static Locale DEFAULT_LOCALE = Locale.getDefault();
    /**
     * 获取系统支持的所有语言环境
     *在打包exe环境中，无法正确获取到所有支持的语言环境，改为用directSupportedLocales直接获取
     * @return 所有语言环境列表
     */
    @Deprecated
    public static List<Locale> getSupportedLocales() {
        List<Locale> availableLocales = new ArrayList<>();
        // 获取系统支持的所有语言环境
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if(DataUtil.isBlank(locale.toString())){
                continue;
            }
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, locale);
                if (bundle.getLocale().equals(locale) ||
                        bundle.getLocale().equals(Locale.ROOT)) {
                    availableLocales.add(locale);
                }
            } catch (MissingResourceException e) {
                // 资源文件不存在，跳过该语言环境
            }
        }

        return availableLocales;
    }

    /**
     * FIXME 打包为exe的jre缺少本地化环境，导致下拉选全是英文（不影响使用的，但是影响体验），后续考虑自定义名称来显示
     * 获取系统支持的所有语言环境
     * @return 所有语言环境列表
     */
    public static List<Locale> directSupportedLocales() {
        List<Locale> supportedLocales = new ArrayList<>();
        supportedLocales.add(Locale.US);
        supportedLocales.add(Locale.SIMPLIFIED_CHINESE);
        supportedLocales.add(Locale.JAPAN);

        return supportedLocales;
    }

    /**
     * 获取指定键的翻译
     *
     * @param key 键
     * @return 翻译
     */
    public static String language(String key) {
       return Main.RESOURCE_BUNDLE.getString(key);
    }
}

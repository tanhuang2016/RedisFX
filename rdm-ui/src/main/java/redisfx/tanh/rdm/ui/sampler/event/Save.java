package redisfx.tanh.rdm.ui.sampler.event;

import redisfx.tanh.rdm.ui.common.Applications;
import redisfx.tanh.rdm.ui.entity.config.KeyTabPaneSetting;
import redisfx.tanh.rdm.ui.entity.config.ServerTabPaneSetting;
import redisfx.tanh.rdm.ui.entity.config.ThemeSetting;
import redisfx.tanh.rdm.ui.sampler.page.components.TabPanePage;
import redisfx.tanh.rdm.ui.sampler.theme.SamplerTheme;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;


public class Save {

    public static void init() {
        /*
          监听主题事件
         */
        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == ThemeEvent.EventType.THEME_CHANGE ) {
                SamplerTheme theme = ThemeManager.getInstance().getTheme();
                ThemeSetting themeSetting=new ThemeSetting();
                themeSetting.setColorTheme(theme.getName());
                Applications.putConfigSettings(themeSetting.getName(),themeSetting);
            }
            if (eventType == ThemeEvent.EventType.FONT_CHANGE) {
                String fontFamily = ThemeManager.getInstance().getFontFamily();
                int fontSize = ThemeManager.getInstance().getFontSize();
                ThemeSetting themeSetting=new ThemeSetting();
                themeSetting.setFont(fontFamily);
                themeSetting.setFontSize(fontSize);
                Applications.putConfigSettings(themeSetting.getName(),themeSetting);
            }

        });

        DefaultEventBus.getInstance().subscribe(TabPaneEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == TabPaneEvent.EventType.SERVER_TAB_PANE_CHANGE ) {
                ServerTabPaneSetting setting=new ServerTabPaneSetting();
                setting.setSide(TabPanePage.server.getTabSide().name());
                setting.setAnimated(TabPanePage.server.isAnimated());
                setting.setFullWidth(TabPanePage.server.isFullWidth());
                setting.setDense(TabPanePage.server.isDense());
                setting.setStyle(TabPanePage.server.getStyle());
                Applications.putConfigSettings(setting.getName(),setting);
            }
            if (eventType == TabPaneEvent.EventType.KEY_TAB_PANE_CHANGE ) {
                KeyTabPaneSetting setting=new KeyTabPaneSetting();
                setting.setSide(TabPanePage.key.getTabSide().name());
                setting.setAnimated(TabPanePage.key.isAnimated());
                setting.setFullWidth(TabPanePage.key.isFullWidth());
                setting.setDense(TabPanePage.key.isDense());
                setting.setStyle(TabPanePage.key.getStyle());
                Applications.putConfigSettings(setting.getName(),setting);
            }


        });
    }
}

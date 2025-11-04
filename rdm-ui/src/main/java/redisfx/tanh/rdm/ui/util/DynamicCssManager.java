package redisfx.tanh.rdm.ui.util;

import redisfx.tanh.rdm.ui.common.Applications;
import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.common.KeyTypeTagEnum;
import redisfx.tanh.rdm.ui.entity.config.KeyTagSetting;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;

import java.util.List;

/**
 * 动态css变量管理
 * @author th
 */
public class DynamicCssManager {

    public static final String COLOR_PREFIX = "-color-tag-";

    public static String styles() {
        KeyTagSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAG.name);
        List<String> colors = setting.getColors();
        List<String> tags = KeyTypeTagEnum.tags();
        StringBuilder styleBuilder = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            String color = colors.get(i);
            if(ThemeManager.getInstance().getTheme().getName().contains(Constant.THEME_LIGHT)){
                color=GuiUtil.hexToRgba(color,0.3);
            }
            styleBuilder.append(COLOR_PREFIX).append(tags.get(i))
                    .append(": ")
                    .append(color)
                    .append("; ");
        }
        return styleBuilder.toString();

    }
}

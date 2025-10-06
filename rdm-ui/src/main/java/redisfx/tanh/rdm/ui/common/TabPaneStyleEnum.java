package redisfx.tanh.rdm.ui.common;

import atlantafx.base.theme.Styles;

import java.util.List;

/**
 * tabPane样式枚举
 * @author th
 */

public enum TabPaneStyleEnum {
    DEFAULT("Default", List.of("whatever", Styles.TABS_FLOATING, Styles.TABS_CLASSIC)),
    FLOATING("Floating",  List.of(Styles.TABS_FLOATING, "whatever", Styles.TABS_CLASSIC)),
    CLASSIC("Classic", List.of(Styles.TABS_CLASSIC, "whatever", Styles.TABS_FLOATING)),
    ;

    public final String name;
    public final List<String> classes;

    TabPaneStyleEnum(String name, List<String> classes) {
        this.name = name;
        this.classes = classes;
    }

    public static TabPaneStyleEnum getByName(String name) {
        for (TabPaneStyleEnum style : values()) {
            if (style.name.equals(name)) {
                return style;
            }
        }
        return null;

    }
}

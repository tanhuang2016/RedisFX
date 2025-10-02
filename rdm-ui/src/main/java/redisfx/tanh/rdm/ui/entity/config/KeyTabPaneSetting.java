package redisfx.tanh.rdm.ui.entity.config;

import javafx.geometry.Side;
import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;

public class KeyTabPaneSetting extends TabPaneSetting{



    @Override
    public TabPaneSetting init(){
        super.init();
        side= Side.BOTTOM.name();
        return this;
    }
    @Override
    public String getName() {
        return ConfigSettingsEnum.KEY_TAB_PANE.name;
    }
}

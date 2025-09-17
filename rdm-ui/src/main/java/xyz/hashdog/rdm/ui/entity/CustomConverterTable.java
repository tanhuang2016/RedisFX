package xyz.hashdog.rdm.ui.entity;


import atlantafx.base.controls.ToggleSwitch;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import xyz.hashdog.rdm.ui.util.GuiUtil;

/**
 * @author th
 * @version 2.3.6
 * @since 2025/9/16 22:35
 */
public class CustomConverterTable  implements ITable {

    /**
     * 名称
     */
    private  String name;
    /**
     * 是否启用
     */
    private  Boolean enabled;

    public CustomConverterTable(String name, Boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public CustomConverterTable() {
    }

    @Override
    public  String[] getProperties() {
        return new String[]{"#row", "name","enabled",""};
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

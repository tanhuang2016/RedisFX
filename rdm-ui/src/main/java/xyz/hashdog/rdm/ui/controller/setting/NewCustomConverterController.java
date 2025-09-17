package xyz.hashdog.rdm.ui.controller.setting;

import atlantafx.base.controls.ToggleSwitch;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.ui.controller.MainController;
import xyz.hashdog.rdm.ui.controller.ServerConnectionsController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;

public class NewCustomConverterController extends BaseWindowController<MainController>  {
    @FXML
    public ToggleSwitch enabled;
    public Tab decodeTab;
    public TextField decodeCmd;
    public TextField decodeDir;
    public Tab encodeTab;
    public TextField encodeCmd;
    public TextField encodeDir;
    public RadioButton stdioInteractionButton;
    public RadioButton fileInteractionButton;
    public Label help;

    @FXML
    public void initialize() {
//        initLanguage();
        initIcon();
        initStyles();
    }

    private void initStyles() {
        help.setCursor(Cursor.HAND);
    }

    private void initIcon() {
        decodeTab.setGraphic(new FontIcon(Material2AL.LOG_OUT));
        encodeTab.setGraphic(new FontIcon(Material2AL.LOG_IN));
        help.setGraphic(new FontIcon(Material2AL.LIVE_HELP));
    }


}

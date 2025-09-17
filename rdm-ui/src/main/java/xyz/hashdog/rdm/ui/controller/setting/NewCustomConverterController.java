package xyz.hashdog.rdm.ui.controller.setting;

import atlantafx.base.controls.ToggleSwitch;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.ui.controller.MainController;
import xyz.hashdog.rdm.ui.controller.ServerConnectionsController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;
import xyz.hashdog.rdm.ui.util.GuiUtil;

public class NewCustomConverterController extends BaseWindowController<MainController>  {
    @FXML
    public ToggleSwitch enabled;
    public Tab decodeTab;
    public TextField decodeCmd;
    public TextField decodeDir;
    public Tab encodeTab;
    public TextField encodeCmd;
    public TextField encodeDir;
    public RadioButton encodeStdio;
    public RadioButton encodeFile;
    public RadioButton decodeStdio;
    public RadioButton decodeFile;
    public Label help;


    @FXML
    public void initialize() {
        initIcon();
        initLanguage();
        initStyles();
    }

    @Override
    protected void initLanguage() {
        super.initLanguage();
        help.setTooltip(GuiUtil.textTooltip("点击查看"));
        Tooltip.install(encodeStdio.getGraphic(), GuiUtil.textTooltip("stdio"));
        Tooltip.install(decodeStdio.getGraphic(), GuiUtil.textTooltip("stdio"));
        Tooltip.install(encodeFile.getGraphic(), GuiUtil.textTooltip("file"));
        Tooltip.install(decodeFile.getGraphic(), GuiUtil.textTooltip("file"));
    }

    private void initStyles() {
        help.setCursor(Cursor.HAND);
    }

    private void initIcon() {
        decodeTab.setGraphic(new FontIcon(Material2AL.LOG_OUT));
        encodeTab.setGraphic(new FontIcon(Material2AL.LOG_IN));
        help.setGraphic(new FontIcon(Material2AL.LIVE_HELP));
        encodeStdio.setGraphic(new FontIcon(Material2AL.HELP_OUTLINE));
        encodeStdio.setContentDisplay(ContentDisplay.RIGHT);
        decodeStdio.setGraphic(new FontIcon(Material2AL.HELP_OUTLINE));
        decodeStdio.setContentDisplay(ContentDisplay.RIGHT);
        decodeFile.setGraphic(new FontIcon(Material2AL.HELP_OUTLINE));
        decodeFile.setContentDisplay(ContentDisplay.RIGHT);
        encodeFile.setGraphic(new FontIcon(Material2AL.HELP_OUTLINE));
        encodeFile.setContentDisplay(ContentDisplay.RIGHT);
    }


}

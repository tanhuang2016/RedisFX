package xyz.hashdog.rdm.ui.controller.setting;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.controller.MainController;
import xyz.hashdog.rdm.ui.controller.ServerConnectionsController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;
import xyz.hashdog.rdm.ui.entity.config.CustomConverterSetting;
import xyz.hashdog.rdm.ui.handler.convert.CustomInvokeConverter;
import xyz.hashdog.rdm.ui.sampler.page.custom.CustomConverterPage;
import xyz.hashdog.rdm.ui.util.GuiUtil;

public class NewCustomConverterController extends BaseWindowController<CustomConverterPage>  {
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
    public Button decodeDirButton;
    public Button encodeDirButton;
    public TextField name;
    public TabPane tabPane;


    @FXML
    public void initialize() {
        initIcon();
        initRadioButton();
        initLanguage();
        initStyles();
    }

    private void initRadioButton() {
        ToggleGroup  encodeGroup = new ToggleGroup();
        encodeStdio.setToggleGroup(encodeGroup);
        encodeFile.setToggleGroup(encodeGroup);
        ToggleGroup  decodeGroup = new ToggleGroup();
        decodeStdio.setToggleGroup(decodeGroup);
        decodeFile.setToggleGroup(decodeGroup);
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
        decodeDirButton.getStyleClass().addAll( Styles.BUTTON_ICON);
        encodeDirButton.getStyleClass().addAll( Styles.BUTTON_ICON);
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
        GuiUtil.setIcon(decodeDirButton,new FontIcon(Material2MZ.MORE_HORIZ));
        GuiUtil.setIcon(encodeDirButton,new FontIcon(Material2MZ.MORE_HORIZ));
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }
    /**
     * 编解码器名称
     */
    public void setName(String name) {
        this.name.setText(name);
        this.name.setEditable(false);
        CustomConverterSetting configSettings = Applications.getConfigSettings(ConfigSettingsEnum.CONVERTER.name);
        for (CustomInvokeConverter converter : configSettings.getList()) {
            if(converter.getName().equals(name)){
                decodeCmd.setText(converter.getDecode().getCmd());
                decodeDir.setText(converter.getDecode().getIoDir());
                decodeStdio.setSelected(converter.getDecode().isUseCmd());
                encodeCmd.setText(converter.getEncode().getCmd());
                encodeDir.setText(converter.getEncode().getIoDir());
                encodeStdio.setSelected(converter.getEncode().isUseCmd());
            }
        }
    }

    public void ok(ActionEvent actionEvent) {
        if(checkForm()){
            return;
        }
        CustomInvokeConverter.Invoker decodeInvoker = new CustomInvokeConverter.Invoker(decodeCmd.getText(), decodeDir.getText(), decodeStdio.isSelected());
        CustomInvokeConverter.Invoker encodeInvoker = new CustomInvokeConverter.Invoker(encodeCmd.getText(), encodeDir.getText(), encodeStdio.isSelected());
        CustomInvokeConverter converter = new CustomInvokeConverter(name.getText(), encodeInvoker, decodeInvoker, enabled.isSelected());
        if(model==ServerConnectionsController.ADD){
            parentController.addConverter(converter);
        }else {
            parentController.updateConverter(converter);
        }

    }

    private boolean checkForm() {
        if(GuiUtil.requiredTextField(name)){
            return true;
        }
        boolean checkDecode =checkForm(decodeCmd, decodeDir,decodeFile);
        if(checkDecode){
            tabPane.getSelectionModel().select(decodeTab);
            return true;
        }
        boolean checkEncode =checkForm(encodeCmd, encodeDir,encodeFile);
        if(checkEncode){
            tabPane.getSelectionModel().select(encodeTab);
            return true;
        }
        return false;
    }

    private boolean checkForm(TextField cmd, TextField dir, RadioButton file) {
       boolean flg = GuiUtil.requiredTextField(cmd);
       if(!flg&&file.isSelected()){
           flg = GuiUtil.requiredTextField(dir);
       }
       return flg;
    }
}

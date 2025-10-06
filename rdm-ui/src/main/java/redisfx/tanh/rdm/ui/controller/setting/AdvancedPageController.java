package redisfx.tanh.rdm.ui.controller.setting;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import redisfx.tanh.rdm.ui.common.Applications;
import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.entity.config.AdvancedSetting;
import redisfx.tanh.rdm.ui.util.GuiUtil;

public class AdvancedPageController  {
    @FXML
    public AnchorPane root;
    public Spinner<Integer> connectionTimeout;
    public Spinner<Integer> soTimeout;
    public ToggleButton treeShow;
    public ToggleButton listShow;
    public TextField keySeparator;
    public Button ok;
    public Button reset;
    public ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        initToggleButton();
        initSpinner();
        initData();
        initButton();
        initListener();
    }

    private void initListener() {
        GuiUtil.filterIntegerInput(false,this.connectionTimeout.getEditor(),this.soTimeout.getEditor());
        changeListener();
    }

    private boolean toggleGroupChange = false;
    private void changeListener() {
        textFieldChangeListener(connectionTimeout.getEditor(),soTimeout.getEditor(),keySeparator);
        // 添加监听，确保至少一个按钮被选中
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                toggleGroupChange=false;
                // 如果没有选中任何按钮，恢复上一个或默认选中
                if (oldToggle != null) {
                    oldToggle.setSelected(true);
                } else {
                    treeShow.setSelected(true);
                }
            }else {
                if(oldToggle!=newToggle && toggleGroupChange)ok.setDisable(false);
                toggleGroupChange=true;
            }
        });
    }

    private void textFieldChangeListener(TextField... textFields) {
        for (TextField textField : textFields) {
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if(!oldValue.equals(newValue)){
                    ok.setDisable( false);
                }
            });
        }
    }

    private void initSpinner() {
        connectionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60_000, 6000));
        soTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60_000, 6000));
    }

    private void initData() {
        AdvancedSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.ADVANCED.name);
        setData(setting);
    }

    private void setData(AdvancedSetting setting) {
        connectionTimeout.getEditor().setText(String.valueOf(setting.getConnectionTimeout()));
        soTimeout.getEditor().setText(String.valueOf(setting.getConnectionTimeout()));
        treeShow.setSelected(setting.isTreeShow());
        listShow.setSelected(!setting.isTreeShow());
        keySeparator.setText(setting.getKeySeparator());
    }

    private void initButton() {
        initButtonStyles();
    }
    private void initButtonStyles() {
        ok.getStyleClass().add(Styles.ACCENT);
    }

    private void initToggleButton() {
        this.toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(treeShow,listShow);
        GuiUtil.setIcon(treeShow,new FontIcon(Material2AL.ACCOUNT_TREE));
        GuiUtil.setIcon(listShow,new FontIcon(Material2MZ.VIEW_LIST));
        treeShow.getStyleClass().addAll(Styles.LEFT_PILL);
        listShow.getStyleClass().addAll(Styles.RIGHT_PILL);
    }

    public void ok(ActionEvent actionEvent) {
        ok.setDisable(true);
        AdvancedSetting setting=new AdvancedSetting();
        setting.setConnectionTimeout(connectionTimeout.getValue());
        setting.setSoTimeout(soTimeout.getValue());
        setting.setKeySeparator(keySeparator.getText());
        setting.setTreeShow(treeShow.isSelected());
        Applications.putConfigSettings(setting.getName(),setting);
    }

    public void reset(ActionEvent actionEvent) {
        setData(new AdvancedSetting().init());
    }

    @FXML
    public void filterIntegerInput(KeyEvent keyEvent) {
        // 获取用户输入的字符
        String inputChar = keyEvent.getCharacter();
        // 如果输入字符不是整数，则阻止其显示在TextField中
        if (!inputChar.matches("\\d")) {
            keyEvent.consume();
        }
    }
}

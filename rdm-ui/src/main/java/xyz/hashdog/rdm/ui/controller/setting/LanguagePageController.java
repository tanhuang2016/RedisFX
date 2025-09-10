package xyz.hashdog.rdm.ui.controller.setting;

import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.entity.config.LanguageSetting;
import xyz.hashdog.rdm.ui.util.LanguageManager;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public class LanguagePageController {
    public Button ok;
    public Button system;
    public AnchorPane root;
    public ComboBox<Locale> langComboBox;
    public Label language;

    @FXML
    public void initialize() {
        initLanguage();
        initLangComboBox();
        initListener();
        initButton();

    }

    private void initLanguage() {
        language.setText(language("main.setting.general.language"));
        ok.setText(language("common.ok"));
        system.setText(language("common.default"));
    }

    private void initButton() {
        initButtonStyles();
    }
    private void initButtonStyles() {
        ok.getStyleClass().add(Styles.ACCENT);
    }

    private void initListener() {
        langComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                this.ok.setDisable(false);
            }
        });
    }

    private void initLangComboBox() {
        // 获取支持的语言列表
        List<Locale> supportedLocales = LanguageManager.getSupportedLocales();

        // 设置ComboBox显示
        langComboBox.setItems(FXCollections.observableArrayList(supportedLocales));
        langComboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                if (locale != null) {
//                    if (DataUtil.isBlank(locale.toString())) {
//                        return String.format("%s", "English");
//                    }
                    return String.format("%s", locale.getDisplayName(locale));
                }
                return "";
            }

            @Override
            public Locale fromString(String string) {
                return null;
            }
        });

        LanguageSetting configSettings = Applications.getConfigSettings(ConfigSettingsEnum.LANGUAGE.name);
        // 设置当前语言
        langComboBox.setValue(Locale.of(configSettings.getLocalLanguage(), configSettings.getLocalCountry()));

    }

    public void ok(ActionEvent actionEvent) {
        this.ok.setDisable(true);
        LanguageSetting configSettings = new LanguageSetting();
        configSettings.setLocalCountry(langComboBox.getValue().getCountry());
        configSettings.setLocalLanguage(langComboBox.getValue().getLanguage());
        Applications.putConfigSettings(configSettings.getName(), configSettings);
        Main.RESOURCE_BUNDLE= ResourceBundle.getBundle(LanguageManager.BASE_NAME,Locale.of(configSettings.getLocalLanguage(),configSettings.getLocalCountry()));
        Main.instance.resetLanguage();
//        if (GuiUtil.alert(Alert.AlertType.CONFIRMATION, language(ALERT_MESSAGE_RESTART_SUCCESS))) {
//        }


    }

    public void system(ActionEvent actionEvent) {
        langComboBox.setValue(Locale.getDefault());
    }

    public void resetLanguage() {
        initLanguage();
    }
}

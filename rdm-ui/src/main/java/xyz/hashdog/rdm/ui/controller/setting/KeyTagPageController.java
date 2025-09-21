package xyz.hashdog.rdm.ui.controller.setting;

import atlantafx.base.theme.Styles;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.common.KeyTypeTagEnum;
import xyz.hashdog.rdm.ui.entity.config.KeyTagSetting;
import xyz.hashdog.rdm.ui.entity.config.ThemeSetting;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public  class KeyTagPageController implements Initializable {
    public HBox tagLabels;
    public HBox tagTexts;
    public HBox tagColors;
    public Button ok;
    public Button reset;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//       reset();
        initData();
        initButton();
        addListener();

    }
    private void initButton() {
        initButtonStyles();
    }
    private void initButtonStyles() {
        ok.getStyleClass().add(Styles.ACCENT);
    }

    private void initData() {
        KeyTagSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAG.name);
        List<String> tags =setting.getTags();
        List<String> colors = setting.getColors();
        setTagLabels(tags,colors);
        setTagTexts(tags);
        setTagColors(colors);
    }

    private void addListener() {
        addTagTextsListener();
        addTagColorsListener();
    }

    private void addTagColorsListener() {
        for (int i = 0; i < tagColors.getChildren().size(); i++) {
            ColorPicker colorPicker= (ColorPicker) tagColors.getChildren().get(i);
            int finalI = i;
            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                Label tag = getTagLabel(finalI);
                tag.setStyle("-fx-background-color:"+GuiUtil.color2hex(newValue));
                ok.setDisable(false);
            });
        }
    }

    private void addTagTextsListener() {
        for (int i = 0; i < tagTexts.getChildren().size(); i++) {
            TextField node = (TextField) tagTexts.getChildren().get(i);
            int finalI = i;
            node.textProperty().addListener((observable, oldValue, newValue) -> {
                Label tag = getTagLabel(finalI);
                if(DataUtil.isBlank(newValue)){
                    node.setText(oldValue);
                }else {
                    tag.setText(newValue);
                    ok.setDisable(false);
                }
            });
        }
    }

    /**
     * 根据下标获取Label
     * @param i
     * @return
     */
    private Label getTagLabel(int i) {
        VBox vBox = (VBox) tagLabels.getChildren().get(i);
        return (Label) vBox.getChildren().get(1);
    }

    @FXML
    private void reset() {
        List<String> tags = KeyTypeTagEnum.tags();
        List<String> colors = KeyTypeTagEnum.colors();
        setTagLabels(tags,colors);
        setTagTexts(tags);
        setTagColors(colors);
    }

    private void setTagColors(List<String> colors) {
        for (int i = 0; i < tagColors.getChildren().size(); i++) {
            ColorPicker c= (ColorPicker) tagColors.getChildren().get(i);
            c.setValue(Color.web(colors.get(i)));
        }
    }

    private void setTagTexts(List<String> tags) {
        for (int i = 0; i < tagTexts.getChildren().size(); i++) {
            TextField node = (TextField) tagTexts.getChildren().get(i);
            node.setText(tags.get(i));
        }
    }

    private void setTagLabels(List<String> tags,List<String> colors) {
        ObservableList<Node> children = tagLabels.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if(children.get(i) instanceof VBox v){
                ObservableList<Node> vc = v.getChildren();
                Label node = (Label)vc.get(1);
                node.setText(tags.get(i));
                node.setStyle("-fx-background-color:"+colors.get(i));

            }
        }
    }


    /**
     * 保存配置，确认按钮隐藏
     * @param actionEvent
     */
    public void ok(ActionEvent actionEvent) {
        this.ok.setDisable(true);
        KeyTagSetting setting=new KeyTagSetting();
        setting.setTags(getTagTexts());
        setting.setColors(getTagColors());
        setting.getTags().add(KeyTypeTagEnum.UNKNOWN.tag);
        setting.getColors().add(KeyTypeTagEnum.UNKNOWN.color);
        Applications.putConfigSettings(setting.getName(),setting);
    }

    private List<String> getTagColors() {
        return Arrays.stream(tagColors.getChildren().toArray())
                .map(node -> ((ColorPicker) node))
                .map(ColorPicker::getValue)
                .map(GuiUtil::color2hex)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> getTagTexts() {
        return Arrays.stream(tagTexts.getChildren().toArray())
                .map(node -> ((TextField) node))
                .map(TextField::getText)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}

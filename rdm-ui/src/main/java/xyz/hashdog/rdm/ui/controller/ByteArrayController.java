package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.hashdog.rdm.common.Constant;
import xyz.hashdog.rdm.common.util.EncodeUtil;
import xyz.hashdog.rdm.common.util.FileUtil;
import xyz.hashdog.rdm.ui.common.UiStyles;
import xyz.hashdog.rdm.ui.common.ValueTypeEnum;
import xyz.hashdog.rdm.ui.controller.base.BaseController;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyController;
import xyz.hashdog.rdm.ui.handler.convert.ValueConverter;
import xyz.hashdog.rdm.ui.handler.convert.ValueConverters;
import xyz.hashdog.rdm.ui.handler.view.CharacterEncoding;
import xyz.hashdog.rdm.ui.handler.view.ValueViewer;
import xyz.hashdog.rdm.ui.handler.view.ValueViewers;
import xyz.hashdog.rdm.ui.handler.view.ViewerNode;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.Util;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/1 14:46
 */
public class ByteArrayController extends BaseController<BaseController> implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(ByteArrayController.class);

//    @FXML
//    public ChoiceBox<String> typeChoiceBox;
//    @FXML
//    public TextArea value2;
    private ViewerNode viewerNode;
    private ValueConverter converter;
    protected static final String SIZE = "Size:%s";

    @FXML
    public Label size;
    @FXML
    public Label name;
    @FXML
    public ComboBox<String> characterChoiceBox;
    @FXML
    public MenuItem into;
    @FXML
    public MenuItem export;
    @FXML
    public AnchorPane root;
    @FXML
    public Button view;
    public Button copy;
    public MenuButton typeMenuButton;
    public AnchorPane valuePane;
    public MenuButton importMenu;
    public Menu viewerMenu;
    public Menu converterMenu;
    /**
     * 当前value的二进制
     */
    private byte[] currentValue;

    private long currentSize;
    /**
     * 当前type
     */
    private ValueTypeEnum type;
    /**
     * 选中的最后的文件的父级目录
     */
    private  File lastFile;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLanguage();
        initCharacterChoiceBox();
//        initTypeChoiceBox();
        initTypeMenuButton();
        initListener();
        initButton();
        characterChoiceBox.setVisible(false);
        characterChoiceBox.setManaged(false);
        characterChoiceBox.getStyleClass().add(UiStyles.MINI_SPACE_ARROW);
        Tooltip tooltip = GuiUtil.textTooltip(characterChoiceBox.getValue());
        tooltip.textProperty().bind(characterChoiceBox.valueProperty());
        characterChoiceBox.setTooltip(tooltip);
        view.setVisible(false);
        view.setManaged(false);
        typeMenuButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED,UiStyles.MINI_SPACE_ARROW);
    }



    @Override
    protected void initLanguage() {
        copy.setTooltip(GuiUtil.textTooltip(language("key.string.copy")));
        into.setText((language("key.string.import")));
        export.setText((language("key.string.export")));
        importMenu.setTooltip(GuiUtil.textTooltip(into.getText()+"/"+export.getText()));
        view.setText(language("key.string.view"));
        viewerMenu.setText("查看方式");
        converterMenu.setText("编解码方式");
    }

    private void initButton() {
        initButtonStyles();
        GuiUtil.setIcon(copy,new FontIcon(Feather.COPY));
        GuiUtil.setIcon(importMenu,new FontIcon(Material2AL.IMPORT_EXPORT));
        into.setGraphic(new FontIcon(Material2MZ.PUBLISH));
        export.setGraphic(new FontIcon(Material2AL.GET_APP));
    }
    private void initButtonStyles() {
        copy.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.SUCCESS,Styles.FLAT);
        importMenu.getStyleClass().addAll(Styles.BUTTON_ICON,Tweaks.NO_ARROW,Styles.FLAT,Styles.SUCCESS);

    }

    /**
     * 初始化字符集选项
     */
    private void initCharacterChoiceBox() {
        characterChoiceBox.getItems().addAll(Constant.CHARSETS);
        characterChoiceBox.setValue(StandardCharsets.UTF_8.displayName());
    }
   private ToggleGroup viewerGroup ;
    private ToggleGroup converterGroup ;
    /**
     * 初始化类型菜单
     * 查看器、编解码器
     */
    private void initTypeMenuButton() {
        // 创建查看器组
        this.viewerGroup = new ToggleGroup();
        this.converterGroup = new ToggleGroup();
        // 添加查看器菜单项
        List<RadioMenuItem> viewerItems = ValueViewers.getInstance().names().stream()
                .map(RadioMenuItem::new)
                .peek(item -> item.setToggleGroup(viewerGroup))
                .toList();
        viewerMenu.getItems().addAll(viewerItems);
        // 添加编解码器菜单项
        List<RadioMenuItem> converterItems = ValueConverters.getInstance().names().stream()
                .map(RadioMenuItem::new)
                .peek(item -> item.setToggleGroup(converterGroup))
                .toList();
        converterMenu.getItems().addAll(converterItems);


        // 设置默认选中项（可选）
        viewerItems.getFirst().setSelected(true);
        converterItems.getFirst().setSelected(true);
        updateTypeMenuButtonText(viewerGroup, converterGroup);
    }


    /**
     * 绑定typeMenuButton文本与选中项
     * @param viewerGroup 查看器组
     * @param converterGroup 编解码器组
     */
    private void bindTypeMenuButtonText(ToggleGroup viewerGroup, ToggleGroup converterGroup) {

        // 监听查看器组变化
        viewerGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            updateTypeMenuButtonText(viewerGroup, converterGroup);
            viewerChange(((RadioMenuItem)newValue).getText());
        });

        // 监听编解码器组变化
        converterGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            updateTypeMenuButtonText(viewerGroup, converterGroup);
            converterChange(((RadioMenuItem)newValue).getText());
        });
    }

    private void converterChange(String newValue) {
        this.converter = ValueConverters.getInstance().getByName(newValue);
        try {
            byte[] decode = converter.decode(currentValue);
            this.viewerNode.set(decode);
        }catch (Exception e){
            log.error("converterChange exception", e);
        }
    }

    private void viewerChange(String newValue) {
        ViewerNode node = ValueViewers.getInstance().getViewerNodeByName(newValue);
        setViewerNode(node,converter.decode(currentValue));
    }

    /**
     * 更新typeMenuButton的文本
     * @param viewerGroup 查看器组
     * @param converterGroup 编解码器组
     */
    private void updateTypeMenuButtonText(ToggleGroup viewerGroup, ToggleGroup converterGroup) {
        StringBuilder text = new StringBuilder();

        // 获取选中的查看器
        if (viewerGroup.getSelectedToggle() != null) {
            RadioMenuItem selectedItem = (RadioMenuItem) viewerGroup.getSelectedToggle();
            text.append(selectedItem.getText());
        }

        // 获取选中的编解码器
        if (converterGroup.getSelectedToggle() != null) {
            RadioMenuItem selectedItem = (RadioMenuItem) converterGroup.getSelectedToggle();
            String converterText = selectedItem.getText();
            // 如果不是"None"，则添加到文本中
            if (!"None".equals(converterText)) {
                if (!text.isEmpty()) {
                    text.append("-");
                }
                text.append(converterText);
            }
        }
        // 设置typeMenuButton的文本
        typeMenuButton.setText(text.toString());
        typeMenuButton.setTooltip(GuiUtil.textTooltip(typeMenuButton.getText()));
    }

    /**
     * 初始化监听
     */
    private void initListener() {
//        typeChoiceBoxListener();
        characterChoiceBoxListener();
    }



    /**
     * 初始化单选框
     */
//    private void initTypeChoiceBox() {
//        ObservableList items = typeChoiceBox.getItems();
//        items.clear();
//        for (ValueTypeEnum valueTypeEnum : ValueTypeEnum.values()) {
//            items.add(valueTypeEnum.name);
//        }
//    }

    /**
     * 字符集选中监听
     */
    private void characterChoiceBoxListener() {
        characterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(this.viewerNode instanceof CharacterEncoding ce){
                ce.change(Charset.forName(newValue));
            }
        });
    }

    /**
     * 类型选择触发事件
     * text系列才会显示编码字符集
     * 二进制数据,才会显示导入导出
     */
//    private void typeChoiceBoxListener() {
//        typeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if(newValue==null){
//                return;
//            }
//            boolean isText = newValue.startsWith(ValueTypeEnum.TEXT.name);
//            characterChoiceBox.setVisible(isText);
//            characterChoiceBox.setManaged(isText);
//            //如果选择text显示,且不是utf8编码,则用US-ASCII字符集
//            if(isText && !EncodeUtil.isUTF8(this.currentValue)){
//                characterChoiceBox.setValue(StandardCharsets.US_ASCII.displayName());
//            }
//            boolean isBinary = newValue.equals(ValueTypeEnum.BINARY.name);
//            boolean isView = newValue.startsWith(ValueTypeEnum.IMAGE.name);
//            view.setVisible(isView);
//            view.setManaged(isView);
//
//
//            this.type=ValueTypeEnum.getByName(newValue);
////            this.value.setText(Objects.requireNonNull(type).handler.byte2Text(this.currentValue,Charset.forName(characterChoiceBox.getValue())));
//            this.viewerNode.set(converter.decode(this.currentValue));
//            if(this.type.handler.isView()){
//                view(null);
//            }
//
//        });
//    }

    /**
     * 复制值
     *
     * @param actionEvent 事件
     */
    @FXML
    public void copy(ActionEvent actionEvent) {
        if(this.viewerNode instanceof CharacterEncoding ce){
            GuiUtil.copyString(ce.text());
        }
    }


    /**
     * 返回最新的数据
     *
     * @return 最新的数据
     */
    public byte[] getByteArray() {
        //这里需要通过Text即时计算byte数组,根据类型进行转换为byte数组
//        return type.handler.text2Byte(value.getText(),Charset.forName(characterChoiceBox.getValue()));
        return converter.encode(viewerNode.get());
    }


    /**
     * 设置数据
     *
     * @param currentValue  数据
     */
    public void setByteArray(byte[] currentValue) {
        this.currentValue = currentValue;
        this.currentSize = currentValue.length;
//        //根据key的类型切换对应视图
//        String fileTypeByStream = FileUtil.getFileTypeByStream(currentValue);
//        //不是可识别的文件类型,都默认采用16进制展示
//        if (fileTypeByStream == null) {
//            boolean isUtf8 = EncodeUtil.isUTF8(currentValue);
//            //是utf8编码或则非特殊字符,直接转utf8字符串
//            if (isUtf8 || !EncodeUtil.containsSpecialCharacters(currentValue)) {
//                type = ValueTypeEnum.TEXT;
//            }
//        }
//        if (type == null) {
//            type = ValueTypeEnum.HEX;
//        } else {
//            type = ValueTypeEnum.TEXT;
//        }
        this.size.setText(String.format(SIZE, Util.convertMemorySizeStr(currentSize,2)));
//        this.typeChoiceBox.setValue(type.name);

        this.converter = ValueConverters.converterByValue(currentValue);
        byte[] decode = converter.decode(currentValue);
        ValueViewer viewer = ValueViewers.viewerByValue(decode);
        setViewerNode(viewer.newViewerNode(),decode);
        selectMenuItemByName(viewerMenu.getItems(),viewer.name());
        selectMenuItemByName(converterMenu.getItems(),converter.name());
        // 监听选中事件，绑定typeMenuButton文本
        updateTypeMenuButtonText(viewerGroup, converterGroup);
        bindTypeMenuButtonText(viewerGroup, converterGroup);
    }

    private void selectMenuItemByName(ObservableList<MenuItem> items, String name) {
        for (MenuItem item : items) {
            if (item.getText().equals(name) && item instanceof RadioMenuItem rmi) {
                rmi.setSelected(true);
                return;
            }
        }
    }

    private void setViewerNode(ViewerNode viewerNode, byte[] decode) {
        this.viewerNode = viewerNode;
        if(viewerNode instanceof CharacterEncoding node){
            node.init(Charset.forName(characterChoiceBox.getValue()));
            characterChoiceBox.setVisible(true);
            characterChoiceBox.setManaged(true);
        }else {
            characterChoiceBox.setVisible(false);
            characterChoiceBox.setManaged(false);
        }
        viewerNode.set(decode);
        Node content = viewerNode.view();
        valuePane.getChildren().clear();
        valuePane.getChildren().add(content);
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
    }

    /**
     * 设置数据，并使用默认类型
     * @param currentValue  数据
     * @param type  类型
     */
    public void setByteArray(byte[] currentValue,ValueTypeEnum type) {
        this.currentValue = currentValue;
        this.currentSize = currentValue.length;
        this.size.setText(String.format(SIZE, Util.convertMemorySizeStr(currentSize,2)));
//        this.typeChoiceBox.setValue(type.name);
    }

    /**
     * 设置名称
     *
     * @param key 名称
     */
    public void setName(String key) {
        this.name.setText(key);

    }

    /**
     * 查看
     * @param actionEvent  事件
     */
    @FXML
    public void view(ActionEvent actionEvent) {
        Parent view = this.type.handler.view(this.currentValue, Charset.forName(characterChoiceBox.getValue()));
        Scene scene = new Scene(view);
        Stage stage=new Stage();
        stage.initStyle(StageStyle.DECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setMaximized(false);
        stage.getIcons().add(GuiUtil.ICON_REDIS);
        stage.initOwner(root.getScene().getWindow());
        stage.setScene(scene);
        stage.setTitle(String.format("View Of %s",this.type.name ));
        stage.show();


    }

    /**
     * 导入文件
     * @param actionEvent  事件
     */
    public void into(ActionEvent actionEvent) {
        File file = GuiUtil.fileChoose(this.root.getScene().getWindow(), lastFile);
        lastFile=file.getParentFile();
        byte[] bytes = FileUtil.file2byte(file);
        setByteArray(bytes);
    }

    /**
     * 导出文件
     * @param actionEvent  事件
     */
    public void export(ActionEvent actionEvent) {
        BaseKeyController parentController = (BaseKeyController) this.parentController;
        File file = GuiUtil.saveFileChoose(this.root.getScene().getWindow(), lastFile, parentController.getParameter().getKey());
        FileUtil.byteWrite2file(this.currentValue,file.getAbsolutePath());
    }
}

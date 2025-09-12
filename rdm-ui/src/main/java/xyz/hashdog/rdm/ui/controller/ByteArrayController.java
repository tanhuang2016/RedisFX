package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.Constant;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.EncodeUtil;
import xyz.hashdog.rdm.common.util.FileUtil;
import xyz.hashdog.rdm.ui.common.ValueTypeEnum;
import xyz.hashdog.rdm.ui.controller.base.BaseController;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyController;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.Util;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/1 14:46
 */
public class ByteArrayController extends BaseController<BaseController> implements Initializable {

    @FXML
    public ChoiceBox<String> typeChoiceBox;
    @FXML
    public TextArea value;
    protected static final String SIZE = "Size:%s";

    @FXML
    public Label size;
    @FXML
    public Label name;
    @FXML
    public ComboBox<String> characterChoiceBox;
    @FXML
    public Button into;
    @FXML
    public Button export;
    @FXML
    public AnchorPane root;
    @FXML
    public Button view;
    public Button copy;
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
        initTypeChoiceBox();
        initListener();
        initButton();
        characterChoiceBox.setVisible(false);
        characterChoiceBox.setManaged(false);
        into.setVisible(false);
        into.setManaged(false);
        export.setVisible(false);
        export.setManaged(false);
        view.setVisible(false);
        view.setManaged(false);
    }

    @Override
    protected void initLanguage() {
        copy.setText(language("key.string.copy"));
        copy.setTooltip(GuiUtil.textTooltip(language("key.string.copy")));
        into.setText(language("key.string.import"));
        export.setText(language("key.string.export"));
        view.setText(language("key.string.view"));
    }

    private void initButton() {
        initButtonStyles();
        GuiUtil.setIcon(copy,new FontIcon(Feather.COPY));
    }
    private void initButtonStyles() {
        copy.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.SUCCESS,Styles.FLAT);

    }

    /**
     * 初始化字符集选项
     */
    private void initCharacterChoiceBox() {
        ObservableList items = characterChoiceBox.getItems();
        items.clear();
        items.addAll(Constant.CHARSETS);
        characterChoiceBox.setValue(StandardCharsets.UTF_8.displayName());
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        typeChoiceBoxListener();
        characterChoiceBoxListener();
    }



    /**
     * 初始化单选框
     */
    private void initTypeChoiceBox() {
        ObservableList items = typeChoiceBox.getItems();
        items.clear();
        for (ValueTypeEnum valueTypeEnum : ValueTypeEnum.values()) {
            items.add(valueTypeEnum.name);
        }
    }

    /**
     * 字符集选中监听
     */
    private void characterChoiceBoxListener() {
        characterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.value.setText(type.handler.byte2Text(this.currentValue,Charset.forName(newValue))));
    }

    /**
     * 类型选择触发事件
     * text系列才会显示编码字符集
     * 二进制数据,才会显示导入导出
     */
    private void typeChoiceBoxListener() {
        typeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue==null){
                return;
            }
            boolean isText = newValue.startsWith(ValueTypeEnum.TEXT.name);
            characterChoiceBox.setVisible(isText);
            characterChoiceBox.setManaged(isText);
            //如果选择text显示,且不是utf8编码,则用US-ASCII字符集
            if(isText && !EncodeUtil.isUTF8(this.currentValue)){
                characterChoiceBox.setValue(StandardCharsets.US_ASCII.displayName());
            }
            boolean isBinary = newValue.equals(ValueTypeEnum.BINARY.name);
            into.setVisible(isBinary);
            into.setManaged(isBinary);
            export.setVisible(isBinary);
            //设置node不可见时不占用空间
            export.setManaged(isBinary);
            boolean isView = newValue.startsWith(ValueTypeEnum.IMAGE.name);
            view.setVisible(isView);
            view.setManaged(isView);


            this.type=ValueTypeEnum.getByName(newValue);
            this.value.setText(Objects.requireNonNull(type).handler.byte2Text(this.currentValue,Charset.forName(characterChoiceBox.getValue())));
            if(this.type.handler.isView()){
                view(null);
            }

        });
    }

    /**
     * 复制值
     *
     * @param actionEvent 事件
     */
    @FXML
    public void copy(ActionEvent actionEvent) {
        GuiUtil.copyString(value.getText());
    }


    /**
     * 返回最新的数据
     *
     * @return 最新的数据
     */
    public byte[] getByteArray() {
        //这里需要通过Text即时计算byte数组,根据类型进行转换为byte数组
        return type.handler.text2Byte(value.getText(),Charset.forName(characterChoiceBox.getValue()));
    }


    /**
     * 设置数据
     *
     * @param currentValue  数据
     */
    public void setByteArray(byte[] currentValue) {
        this.currentValue = currentValue;
        this.currentSize = currentValue.length;
        //根据key的类型切换对应视图
        String fileTypeByStream = FileUtil.getFileTypeByStream(currentValue);
        //不是可识别的文件类型,都默认采用16进制展示
        if (fileTypeByStream == null) {
            boolean isUtf8 = EncodeUtil.isUTF8(currentValue);
            //是utf8编码或则非特殊字符,直接转utf8字符串
            if (isUtf8 || !EncodeUtil.containsSpecialCharacters(currentValue)) {
                type = ValueTypeEnum.TEXT;
            }
        }
        if (type == null) {
            type = ValueTypeEnum.HEX;
        } else {
            type = ValueTypeEnum.TEXT;
        }
        this.size.setText(String.format(SIZE, Util.convertMemorySizeStr(currentSize,2)));
        this.typeChoiceBox.setValue(type.name);
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
        this.typeChoiceBox.setValue(type.name);
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

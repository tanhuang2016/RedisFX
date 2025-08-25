package xyz.hashdog.rdm.ui.util;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.*;
import xyz.hashdog.rdm.ui.controller.BaseController;
import xyz.hashdog.rdm.ui.controller.BaseKeyController;
import xyz.hashdog.rdm.ui.controller.ByteArrayController;
import xyz.hashdog.rdm.ui.entity.ITable;
import xyz.hashdog.rdm.ui.entity.PassParameter;
import xyz.hashdog.rdm.ui.entity.config.KeyTagSetting;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 针对视图的操作工具
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 13:10
 */
public class GuiUtil {

    public static final Image ICON_GTOUP = new Image(Main.class.getResourceAsStream("/icon/group.png"));
    public static final Image ICON_CONNECTION = new Image(Main.class.getResourceAsStream("/icon/connection.png"));
//    public static final Image ICON_REDIS =  new Image(Main.class.getResourceAsStream("/icon/redis256.png"));
    public static final Image ICON_REDIS =  GuiUtil.svgImage("/svg/redis_red.svg",256);
    private static final Image ICON_KEY =new Image(Main.class.getResourceAsStream("/icon/key.png"));
    private static final Image ICON_CONSOLE =new Image(Main.class.getResourceAsStream("/icon/console.png"));

    /**
     * 系统剪贴板
     * @param copyString 文本
     */
    public static void copyString(String copyString) {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(copyString);
        systemClipboard.setContent(content);
    }

    /**
     * 创建新的连接图标
     * @return
     */
    public static FontIcon creatConnctionImageView() {
        return  new FontIcon(Feather.LINK);
    }
//    public static ImageView creatConnctionImageView() {
//        return GuiUtil.creatImageView(ICON_CONNECTION ,16,16);
//    }

    /**
     * 创建新的分组图标
     * @return
     */
    public static FontIcon creatGroupImageView() {
        return new FontIcon(Feather.FOLDER);
    }
//    public static ImageView creatGroupImageView() {
//        return GuiUtil.creatImageView(ICON_GTOUP,16,16);
//    }
    /**
     * 创建新的key图标
     * 只有png是可以在下拉选中显示图标，但是要想办法把tag也做出图片来，后续优化吧 todo
     * @return
     */
    public static FontIcon creatKeyImageView() {
        return new FontIcon(Feather.KEY);
    }
    public static ImageView creatKeyImageView2() {
        return GuiUtil.creatImageView(ICON_KEY,16,16);
    }
    /**
     * 创建新的控制台图标
     * @return
     */
    public static FontIcon creatConsoleImageView() {
        return  new FontIcon(Feather.TERMINAL);
//        return  new FontIcon(Feather.MONITOR);
    }
//    public static ImageView creatConsoleImageView() {
//        return GuiUtil.creatImageView(ICON_CONSOLE,16,16);
//    }

    public static FontIcon creatMonitorImageView() {
        return  new FontIcon(Feather.MONITOR);
    }
    public static FontIcon creatInfoImageView() {
        return  new FontIcon(Feather.INFO);
    }
    public static FontIcon creatPubSubImageView() {
        return  new FontIcon(Feather.RSS);
    }

    /**
     *  创建String绑定对象
     *  重新toString,是为了展示在ui上
     * @param key
     * @return
     */
    public static StringProperty creatStringProperty(String key) {
        return new SimpleStringProperty(key){
            @Override
            public String toString() {
                return get();
            }
        };
    }

    /**
     * 只允许输入整数,可以负数
     * @param textFields
     */
    public static void filterIntegerInput(TextField... textFields) {
        filterIntegerInput(true,textFields);
    }
    /**
     * flg为true允许输入整数,可以负数
     * flg为false只能正整数
     * @param textFields
     */
    public static void filterIntegerInput(boolean flg,TextField... textFields) {
        for (TextField textField : textFields) {
            // 绑定监听器，当文本框内容发生变化时进行过滤
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                // 如果新的文本不是整数，则将文本还原为旧值
                if (flg&&!newValue.matches("-?\\d*")) {
                    textField.setText(oldValue);
                }
                if (!flg&&!newValue.matches("\\d*")) {
                    textField.setText(oldValue);
                }
            });
        }
    }

    /**
     * 返回为true证明有未填的
     *
     * @param textFields
     * @return
     */
    public static boolean requiredTextField(TextField... textFields) {
        boolean flg = false;
        for (TextField textField : textFields) {
            String text = textField.getText();
            if (text.isEmpty()) {
                // 当名字未填写时，将TextField的边框颜色设置为红色
                textField.setStyle("-fx-border-color: red;");
                flg = true;
            } else {
                // 当名字已填写时，将TextField的边框颜色还原为默认值
                textField.setStyle("");
            }
        }
        return flg;
    }

    /**
     * 提示框
     *
     * @param alertType 弹框类型
     * @param message   附带消息
     * @return 确定/取消
     */
    public static boolean alert(Alert.AlertType alertType, String message) {
        Alert a=createAlert(alertType,message);
        // 添加响应处理程序
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 瞬间显示
     * @param message
     */
    public static void alertMoment( String message) {
        Alert a=createAlert(Alert.AlertType.NONE,message);
        Stage window = (Stage) a.getDialogPane().getScene().getWindow();
        a.show();
        // 设置PauseTransition暂停1秒
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            // 在暂停时间结束后关闭对话框
            window.close();
        });
        // 启动PauseTransition
        delay.play();

    }

    /**
     * 创建alert
     * @param alertType 类型
     * @param message 消息
     * @return
     */
    private static Alert createAlert(Alert.AlertType alertType, String message) {
        Alert a = new Alert(alertType);
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ICON_REDIS);
//        a.setGraphic(creatImageView(ICON_REDIS,32,32));
        a.getDialogPane().getStylesheets().add("/css/global.css");
        a.setHeaderText(Main.RESOURCE_BUNDLE.getString("alert."+alertType.name().toLowerCase()));
        a.setContentText(message);


        // 设置按钮文本
        Button okButton = (Button) a.getDialogPane().lookupButton(ButtonType.OK);
        if(okButton!=null){
            okButton.setText(Main.RESOURCE_BUNDLE.getString(Constant.OK));
        }

        Button cancelButton = (Button) a.getDialogPane().lookupButton(ButtonType.CANCEL);
        if(cancelButton!=null){
            cancelButton.setText(Main.RESOURCE_BUNDLE.getString(Constant.CANCEL));
        }
        return a;
    }

    /**
     * 创建新的ImageView
     * @param icon 图标
     * @param w 宽
     * @param h 高
     * @return
     */
    public static ImageView creatImageView(Image icon, int w, int h) {
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
        return imageView;

    }


    /**
     * 所有节点展开
     * @param item
     */
    public static void expandAllNodes(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandAllNodes(child);
            }
        }
    }



    /**
     * 创建新的邮件菜单,切添加菜单事件
     * @param tab
     * @return
     */
    public static ContextMenu newTabContextMenu(Tab tab) {
        MenuItem close = new MenuItem(Main.RESOURCE_BUNDLE.getString(Constant.CLOSE)+"(_C)");
        close.setMnemonicParsing(true);
        MenuItem closeOther = new MenuItem(Main.RESOURCE_BUNDLE.getString(Constant.CLOSE_OTHER)+"(_O)");
        closeOther.setMnemonicParsing(true);
        MenuItem closeLeft = new MenuItem(Main.RESOURCE_BUNDLE.getString(Constant.CLOSE_LEFT));
        MenuItem closeRight = new MenuItem(Main.RESOURCE_BUNDLE.getString(Constant.CLOSE_RIGHT));
        MenuItem closeAll = new MenuItem(Main.RESOURCE_BUNDLE.getString(Constant.CLOSE_ALL)+"(_A)");
        closeAll.setMnemonicParsing(true);
        ContextMenu cm = new ContextMenu(close,closeOther,closeLeft,closeRight,closeAll);
//        cm.setOpacity(0.8d);
        tab.setContextMenu(cm);
        // 关闭当前
        close.setOnAction(event -> {
            // 获取触发事件的MenuItem
            MenuItem clickedMenuItem = (MenuItem) event.getSource();
            // 获取与MenuItem关联的ContextMenu
            ContextMenu triggeredMenu = clickedMenuItem.getParentPopup();
            TabPane tabPane = tab.getTabPane();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() == triggeredMenu) {
                    //关闭redis连接,移除tab
                    closeTab(tabPane,tab1);
                    break;
                }
            }
        });

        // 关闭其他
        closeOther.setOnAction(event -> {
            // 获取触发事件的MenuItem
            MenuItem clickedMenuItem = (MenuItem) event.getSource();
            // 获取与MenuItem关联的ContextMenu
            ContextMenu triggeredMenu = clickedMenuItem.getParentPopup();
            TabPane tabPane = tab.getTabPane();
            List<Tab> dels  = new ArrayList<>();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() != triggeredMenu) {
                    dels.add(tab1);
                }
            }
            //关闭redis连接,移除tab
            closeTab(tabPane,dels);
        });

        // 关闭左边所有
        closeLeft.setOnAction(event -> {
            // 获取触发事件的MenuItem
            MenuItem clickedMenuItem = (MenuItem) event.getSource();
            // 获取与MenuItem关联的ContextMenu
            ContextMenu triggeredMenu = clickedMenuItem.getParentPopup();
            TabPane tabPane = tab.getTabPane();
            List<Tab> dels  = new ArrayList<>();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() != triggeredMenu) {
                    dels.add(tab1);
                }else{
                    break;
                }
            }
            //关闭redis连接,移除tab
            closeTab(tabPane,dels);
        });

        // 关闭右边所有
        closeRight.setOnAction(event -> {
            // 获取触发事件的MenuItem
            MenuItem clickedMenuItem = (MenuItem) event.getSource();
            // 获取与MenuItem关联的ContextMenu
            ContextMenu triggeredMenu = clickedMenuItem.getParentPopup();
            TabPane tabPane = tab.getTabPane();
            boolean flg=false;
            List<Tab> dels  = new ArrayList<>();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() == triggeredMenu) {
                    flg=true;
                }else{
                    if(flg){
                        dels.add(tab1);
                    }
                }
            }
            //关闭redis连接,移除tab
            closeTab(tabPane,dels);
        });
        // 关闭所有
        closeAll.setOnAction(event -> {
            TabPane tabPane = tab.getTabPane();
            List<Tab> dels = new ArrayList<>(tabPane.getTabs());
            //关闭redis连接,移除tab
            closeTab(tabPane,dels);
        });
        return cm;
    }

    /**
     * 关闭多个
     * @param tabPane
     * @param dels
     */
    public static void closeTab(TabPane tabPane, List<Tab> dels) {
        for (Tab del : dels) {
            closeTab(tabPane,del);
        }
    }

    /**
     * 关闭redis连接,移除tab
     * @param tabPane
     * @param selectedTab
     */
    public static void closeTab(TabPane tabPane,Tab selectedTab) {
        BaseKeyController userData = (BaseKeyController)selectedTab.getContent().getUserData();
        //CONSOLE类型需要关闭redis连接
        if(userData.getParameter().getTabType()== PassParameter.CONSOLE){
            ThreadPool.getInstance().execute(()->userData.getRedisClient().close());
        }
        if(userData.getParameter().getTabType()== PassParameter.REDIS){
            ThreadPool.getInstance().execute(()->userData.getRedisContext().close());
        }
        tabPane.getTabs().remove(selectedTab);
    }

    /**
     * 是否删除弹窗
     * @return
     */
    public static boolean alertRemove() {
       return GuiUtil.alert(Alert.AlertType.CONFIRMATION, Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL));
    }

    /**
     * 视图上删除对应数据
     *
     * @param lastSelect
     */
    public static <T>void remove2UI(ObservableList<T> list, TableView<T> tableView, T lastSelect) {
        Platform.runLater(() -> {
            //缓存的所有数据需要删除
            list.remove(lastSelect);
        });
        int i = tableView.getItems().indexOf(lastSelect);
        if (i > -1) {
            Platform.runLater(() -> {
                //视图需要删除
                tableView.getItems().remove(i);
                tableView.refresh();
            });
        }
    }

    /**
     * 创建通用子stae
     * @param title
     * @param anchorPane
     * @param window
     * @return
     */
    public static Stage createSubStage(String title, Parent anchorPane, Window window) {
        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(anchorPane);
        stage.initOwner(window);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        return stage;
    }



    /**
     * 加载byteArrayView
     *
     * @param bytes
     * @return
     */
    public static Tuple2<AnchorPane, ByteArrayController> loadByteArrayView(byte[] bytes, BaseController baseController) {
        Tuple2<AnchorPane, ByteArrayController> tuple2 = baseController.loadFXML("/fxml/ByteArrayView.fxml");
        tuple2.t2().setParentController(baseController);
        tuple2.t2().setByteArray(bytes);
        return tuple2;
    }


    /**
     * 设置图标和快捷键
     * @param item
     * @param fontIcon
     * @param keyCodeCombination
     */
    public static void setIconAndKey(MenuItem item, FontIcon fontIcon, KeyCodeCombination keyCodeCombination) {
        item.setGraphic(fontIcon);
        item.setAccelerator(keyCodeCombination);
    }

    /**
     * 设置图标
     * @param button
     * @param fontIcon
     */
    public static void setIcon(ButtonBase button, Node fontIcon) {
        button.setGraphic(fontIcon);
    }

    /**
     * 获取key的标签
     * @param type
     * @return
     */
    public static Label getKeyTypeLabel(String type) {
       Tuple2<String,String> tag= getKeyTypeTag(type);
        return createTypeLabel(tag);
    }
    public static Label getKeyTypeNameLabel(String type) {
        Tuple2<String,String> tag= getKeyTypeNameTag(type);
        return createTypeLabel(tag);
    }

    private static Label createTypeLabel(Tuple2<String, String> tag) {
        Label tagLabel = new Label(tag.t1());
        tagLabel.getStyleClass().add("tag");
        tagLabel.setStyle("-fx-background-color:"+tag.t2());
        return tagLabel;
    }

    public static Label getKeyTypeLabelMax(String type) {
        Label tagLabel = getKeyTypeNameLabel(type);
        tagLabel.getStyleClass().add("max");
        return tagLabel;
    }

    public static Tuple2<String, String> getKeyTypeTag(String type) {
        KeyTagSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAG.name);
        int i  =RedisDataTypeEnum.getIndex(type);
        return new Tuple2<>(setting.getTags().get(i),setting.getColors().get(i));
    }
    public static Tuple2<String, String> getKeyTypeNameTag(String type) {
        KeyTagSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAG.name);
        int i  =RedisDataTypeEnum.getIndex(type);
        RedisDataTypeEnum byType = RedisDataTypeEnum.getByType(type);
        return new Tuple2<>(byType.type,setting.getColors().get(i));
    }

    public static FXMLLoader loadFXML(String xml) {
        return new FXMLLoader(Main.class.getResource(xml),Main.RESOURCE_BUNDLE);
    }
    public static  <T1,T2>Tuple2<T1,T2> doLoadFXML(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml), Main.RESOURCE_BUNDLE);
            T1 t1 = fxmlLoader.load();
            T2 t2 = fxmlLoader.getController();
            Tuple2<T1,T2> tuple2 =new Tuple2<>(t1,t2);
            return tuple2;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 设置TabPane的side
     * @param tabPane
     * @param side
     */
    public static void setTabPaneSide(TabPane tabPane, Side side) {
        tabPane.setSide(side);

    }

    public static String color2hex(Color newValue) {
        return String.format("#%02X%02X%02X",
                (int)(newValue.getRed() * 255),
                (int)(newValue.getGreen() * 255),
                (int)(newValue.getBlue() * 255));
    }

    public static String hexToRgba(String hexColor) {
        return hexToRgba(hexColor, 1.0); // 默认不透明
    }

    public static String hexToRgba(String hexColor, double alpha) {
        // 处理可能的#前缀
        if (!hexColor.startsWith("#")) {
            return hexColor;
        }
        hexColor = hexColor.substring(1);

        // 处理3位十六进制颜色（如#FFF）
        if (hexColor.length() == 3) {
            StringBuilder expanded = new StringBuilder();
            for (char c : hexColor.toCharArray()) {
                expanded.append(c).append(c);
            }
            hexColor = expanded.toString();
        }

        // 解析RGB值
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, alpha);
    }

    /**
     * svg图标
     * @param svg
     * @return
     */
    public static ImageView svgImageView(String svg) {
        Image fxImage = svgImage(svg,20,1);
        return new ImageView(fxImage);
    }
    public static ImageView svgImageView(String svg,int w) {
        Image fxImage = svgImage(svg,w,1);
        return new ImageView(fxImage);
    }

    public static Image svgImage(String svg,int w){
       return svgImage(svg,w,1);
    }
    /**
     * zoom用于图标在画布上的整体大小，zoom=2,相当于图片大小不变，图标缩小一倍
     * @param svg
     * @param w
     * @param zoom
     * @return
     */
    public static Image svgImage(String svg,int w,int zoom) {
        SVGLoader loader = new SVGLoader();
        URL svgUrl = Main.class.getResource(svg);
        SVGDocument svgDocument = loader.load(svgUrl);
        BufferedImage image = new BufferedImage(w*zoom,w*zoom,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        svgDocument.render(null,g,new ViewBox((zoom-1)*w, (zoom-1)*w, w, w));
        g.dispose();
        Image fxImage = SwingFXUtils.toFXImage(image, null);
        return fxImage;
    }
/*    public static Image svgImage2(String svg,int w) {
        SVGLoader loader = new SVGLoader();
        URL svgUrl = Main.class.getResource(svg);
        SVGDocument svgDocument = loader.load(svgUrl);
        BufferedImage image = new BufferedImage(w,w,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        svgDocument.render(null,g,new ViewBox(0, 0, w, w));
        g.dispose();
        Image fxImage = SwingFXUtils.toFXImage(image, null);
        return fxImage;
    }*/

    /**
     * 创建普通的表格
     * @param tableView
     * @param iTable
     */
    @SuppressWarnings("unchecked")
    public static <T extends ITable,S>void initSimpleTableView(TableView<T> tableView, T iTable) {
        ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
        TableColumn<T, Integer> c0 = (TableColumn<T,Integer>)columns.getFirst();
        c0.setCellValueFactory(
                param -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(param.getValue()) + 1)
        );
        c0.setSortable(false);
        for (int i = 1; i < columns.size(); i++) {
            TableColumn<T, S> c1 =  (TableColumn<T, S>)columns.get(i);
            c1.setCellValueFactory(
                    new PropertyValueFactory<>(iTable.getProperties()[i])
            );
            int finalI = i;
            c1.setCellFactory(param -> TUtil.ifNull(iTable.getCellFactory(finalI),new GuiUtil.OneLineTableCell<>()));
        }
    }

    /**
     * 动态调整表格高度，不显示滚动条
     * @param tableView
     */
    public static void adjustTableViewHeightPrecise(TableView<?> tableView) {
        // 确保表格已经渲染
        tableView.layout();

        double headerHeight = 0;
        double rowHeight = 0;

        // 获取表头高度
        Node header = tableView.lookup(".column-header-background");
        if (header != null) {
            headerHeight = header.getBoundsInLocal().getHeight();
        }

        // 获取行高（通过第一行计算）
        if (!tableView.getItems().isEmpty()) {
            Node firstRow = tableView.lookup(".table-row-cell");
            if (firstRow != null) {
                rowHeight = firstRow.getBoundsInLocal().getHeight();
            } else {
                rowHeight = 24.0; // 默认行高
            }
        }

        // 如果无法获取行高，则使用默认值
        if (rowHeight <= 0) {
            rowHeight = 24.0;
        }

        // 计算总高度
        int rowCount = tableView.getItems().size();
        double totalHeight = headerHeight + (rowCount * rowHeight);

        // 设置高度
        tableView.setPrefHeight(totalHeight);
        tableView.setMinHeight(totalHeight);
        tableView.setMaxHeight(totalHeight);

        // 同时禁用滚动
        tableView.setFixedCellSize(rowHeight);
    }

    /**
     * 文本提示
     * @param text
     * @return
     */
    public static Tooltip textTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(200)); // 200ms 延迟
        tooltip.setHideDelay(Duration.millis(100)); // 100ms 隐藏延迟
        return tooltip;
    }


    /**
     * 用于tableView压缩为单行,就是避免出现换行的情况
     * @param <T>
     */
    public static class OneLineTableCell<T,S> extends TableCell<T, S> {
        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                if(item instanceof String str){
                    setText(str.replaceAll("\\s+", " ").trim());
                }else if(item instanceof Number number) {
                    setText(number.toString().replaceAll("\\s+", " ").trim());
                }
            } else {
                setText(null);
            }
        }
    }


    /**
     * 文件选择
     *
     * @param ownerWindow
     * @param last
     * @return
     */
    public static File fileChoose(Window ownerWindow, File last) {
        return fileChoose(ownerWindow,last,"file(*)","*.*");
    }
    public static File fileChoose(Window ownerWindow, File last, String description, String... extensions) {
        FileChooser fileChooser =createFileChooser(last,"文件选择",description,extensions);
        File chooseFile = fileChooser.showOpenDialog(ownerWindow);
        return chooseFile;
    }

    /**
     * 创建文件选择器
     * @param last 最后一个文件
     * @param title 标题
     * @param description
     * @param extensions
     * @return
     */
    private static FileChooser createFileChooser(File last, String title, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        if (last != null && last.exists() && last.isDirectory()) {
            fileChooser.setInitialDirectory(last);
        }
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    /**
     * 文件选择
     *
     * @param ownerWindow
     * @param last
     * @return
     */
    public static File savaFileChoose(Window ownerWindow, File last) {
        FileChooser fileChooser =createFileChooser(last,"文件保存","file(*)","*.*");
        File chooseFile =  fileChooser.showSaveDialog(ownerWindow);
        return chooseFile;
    }
}

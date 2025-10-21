package redisfx.tanh.rdm.ui.util;

import atlantafx.base.theme.Styles;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
//import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.stage.Window;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.common.pool.ThreadPool;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.common.util.TUtil;
import redisfx.tanh.rdm.ui.Main;
import redisfx.tanh.rdm.ui.common.Applications;
import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.common.RedisDataTypeEnum;
import redisfx.tanh.rdm.ui.controller.base.BaseController;
import redisfx.tanh.rdm.ui.entity.ITable;
import redisfx.tanh.rdm.ui.entity.config.KeyTagSetting;
import redisfx.tanh.rdm.ui.sampler.custom.HintTooltip;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * 针对视图的操作工具
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 13:10
 */
public class GuiUtil {
    private static final Logger log = LoggerFactory.getLogger(GuiUtil.class);

//    public static final Image ICON_REDIS =  new Image(Main.class.getResourceAsStream("/icon/redis256.png"));
    public static final Image ICON_REDIS =  GuiUtil.svgImage("/svg/fx_icon.svg",256);
    public static final Image ICON_REDIS_32 =  GuiUtil.svgImage("/svg/fx_icon.svg",32);

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
     * 创建新地连接图标
     * @return 连接图标
     */
    public static FontIcon creatConnectionIcon() {
        return  new FontIcon(Feather.LINK);
    }

    /**
     * 创建新的分组图标
     * @return 分组图标
     */
    public static FontIcon creatGroupIcon() {
        return new FontIcon(Feather.FOLDER);
    }
    /**
     * 创建新的key图标
     * 只有png是可以在下拉选中显示图标，但是要想办法把tag也做出图片来，后续优化吧 ，其实不展示下拉图标也无所谓
     * @return key图标
     */
    public static FontIcon creatKeyIcon() {
        return new FontIcon(Feather.KEY);
    }
    /**
     * 创建新的控制台图标
     * @return 控制台图标
     */
    public static FontIcon creatConsoleIcon() {
        return  new FontIcon(Feather.TERMINAL);
    }

    /**
     * 创建新的监控图标
     * @return 监控图标
     */
    public static FontIcon creatMonitorIcon() {
        return  new FontIcon(Feather.MONITOR);
    }
    /**
     * 创建新的信息图标
     * @return 信息图标
     */
    public static FontIcon creatInfoIcon() {
        return  new FontIcon(Feather.INFO);
    }
    /**
     * 创建新地发布订阅图标
     * @return 发布订阅图标
     */
    public static FontIcon creatPubSubIcon() {
        return  new FontIcon(Feather.RSS);
    }


    /**
     * 只允许输入整数,可以负数
     * @param textFields 输入框
     */
    public static void filterIntegerInput(TextField... textFields) {
        filterIntegerInput(true,textFields);
    }
    /**
     * flg为true允许输入整数,可以负数
     * flg为false只能正整数
     * @param textFields 输入框
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
     * @param textFields 输入框
     * @return 判断是否有未填
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
     * @param alertType 弹框类型
     * @param message   附带消息
     * @return 确定/取消
     */
    public static boolean alert(Alert.AlertType alertType, String message) {
        return alert(alertType, message,Main.instance.getController().currentStage);
    }
    public static boolean alert(Alert.AlertType alertType, String message,Window owner) {
        Alert a=createAlert(alertType,message,owner);
        // 添加响应处理程序
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * 瞬间显示
     * @param message 弹框信息
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
     * @return  alert
     */
    private static Alert createAlert(Alert.AlertType alertType, String message,Window owner) {
        Alert a = new Alert(alertType);
        a.initOwner(Main.instance.getController().currentStage);
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ICON_REDIS);
        a.setHeaderText(null);
        a.initOwner(owner);
//        a.setHeaderText(Main.RESOURCE_BUNDLE.getString("alert."+alertType.name().toLowerCase()));
        a.setContentText(message);
        // 获取内容标签并计算实际所需宽度
        DialogPane dialogPane = a.getDialogPane();
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setWrapText(true);
            // 计算文本所需宽度
            double textWidth = GuiUtil.computeTextWidth(contentLabel.getFont(), a.getContentText(), 150);
            // 设置合适的宽度 (图标64px + 间距 + 文本宽度 + 边距)
            double desiredWidth = Math.max(150, Math.min(400, 64 + 5 + textWidth + 5));
            dialogPane.setPrefWidth(desiredWidth);
        }
        a.getDialogPane().getScene().getWindow().sizeToScene();
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
    private static Alert createAlert(Alert.AlertType alertType, String message) {
        return createAlert(alertType,message,Main.instance.getController().currentStage);
    }


    /**
     * 所有节点展开
     * @param item 节点
     */
    public static void expandAllNodes(TreeItem<?> item) {
        expandAllNodes(item,true);
    }

    /**
     * 所有节点展开/收起
     * @param node 节点
     * @param expanded true是展开false是收起
     */
    public static void expandAllNodes(TreeItem<?> node, boolean expanded) {
        if (node != null) {
            if(node.isLeaf()){
                return;
            }
            //只操作非根节点
            if(node.getParent()!=null){
                node.setExpanded(expanded);
            }
            for (TreeItem<?> child : node.getChildren()) {
                // 递归展开所有子节点
                expandAllNodes(child,expanded);
            }
        }
    }



    /**
     * 创建新的右键菜单,切添加菜单事件
     * @param tab tab页
     * @return 上下文菜单
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
            List<Tab> tabsToClose  = new ArrayList<>();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() != triggeredMenu) {
                    tabsToClose.add(tab1);
                }
            }
            //关闭redis连接,移除tab
            closeTab(tabPane,tabsToClose);
        });

        // 关闭左边所有
        closeLeft.setOnAction(event -> {
            // 获取触发事件的MenuItem
            MenuItem clickedMenuItem = (MenuItem) event.getSource();
            // 获取与MenuItem关联的ContextMenu
            ContextMenu triggeredMenu = clickedMenuItem.getParentPopup();
            TabPane tabPane = tab.getTabPane();
            List<Tab> tabsToClose  = new ArrayList<>();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() != triggeredMenu) {
                    tabsToClose.add(tab1);
                }else{
                    break;
                }
            }
            //关闭redis连接,移除tab
            closeTab(tabPane,tabsToClose);
        });

        // 关闭右边所有
        closeRight.setOnAction(event -> {
            // 获取触发事件的MenuItem
            MenuItem clickedMenuItem = (MenuItem) event.getSource();
            // 获取与MenuItem关联的ContextMenu
            ContextMenu triggeredMenu = clickedMenuItem.getParentPopup();
            TabPane tabPane = tab.getTabPane();
            boolean flg=false;
            List<Tab> tabsToClose  = new ArrayList<>();
            for (Tab tab1 : tabPane.getTabs()) {
                if (tab1.getContextMenu() == triggeredMenu) {
                    flg=true;
                }else{
                    if(flg){
                        tabsToClose.add(tab1);
                    }
                }
            }
            //关闭redis连接,移除tab
            closeTab(tabPane,tabsToClose);
        });
        // 关闭所有
        closeAll.setOnAction(event -> {
            TabPane tabPane = tab.getTabPane();
            List<Tab> tabsToClose = new ArrayList<>(tabPane.getTabs());
            //关闭redis连接,移除tab
            closeTab(tabPane,tabsToClose);
        });
        return cm;
    }

    /**
     * 关闭多个
     * @param tabPane tab页容器
     * @param tabsToClose 需要删除的tab页
     */
    public static void closeTab(TabPane tabPane, List<Tab> tabsToClose) {
        for (Tab del : tabsToClose) {
            closeTab(tabPane,del);
        }
    }

    /**
     * 触发关闭事件，移除tab
     * @param tabPane tab页容器
     * @param selectedTab 当前选中的tab页
     */
    public static void closeTab(TabPane tabPane,Tab selectedTab) {
        Event.fireEvent(selectedTab, new Event(Tab.CLOSED_EVENT));
        tabPane.getTabs().remove(selectedTab);
    }

    /**
     * 是否删除弹窗
     * 是返回false  否返回true
     * 行数据删除
     * @return true是取消 false 确定
     */
    public static boolean alertRemoveRowCancel() {
       return alertRemoveCancel("该行数据");
    }
    /**
     * 是否删除弹窗
     * 是返回false  否返回true
     * @param content 删除内容
     * @return true是取消 false 确定
     */
    public static boolean alertRemoveCancel(String content) {
        return !GuiUtil.alert(Alert.AlertType.CONFIRMATION, Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL).formatted(content));
    }

    /**
     * 视图上删除对应数据
     *
     * @param lastSelect 最后选择的数据
     */
    public static <T> void remove2TableView(ObservableList<T> list, TableView<T> tableView, T lastSelect) {
        Platform.runLater(() -> {
            //缓存的所有数据需要删除
            list.remove(lastSelect);
            int i = tableView.getItems().indexOf(lastSelect);
            if (i > -1) {
                //视图需要删除
                tableView.getItems().remove(i);
                tableView.refresh();
            }
        });

    }

    /**
     * 创建通用子stage
     * @param title 标题
     * @param anchorPane 容器
     * @param window 父窗口
     * @return 新窗口
     */
    public static Stage createSubStage(String title, Parent anchorPane, Window window) {
        Stage stage = new Stage();
        //显示设置图标，避免有时候未继承父窗口图标
        stage.getIcons().add(GuiUtil.ICON_REDIS);
        stage.setTitle(title);
        Scene scene = new Scene(anchorPane);
        stage.initOwner(window);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        //去掉最小化和最大化
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        //禁用掉最大最小化
        stage.setMaximized(false);
        return stage;
    }






    /**
     * 设置图标和快捷键
     * @param item 菜单项
     * @param fontIcon 图标
     * @param keyCodeCombination 快捷键
     */
    public static void setIconAndKey(MenuItem item, FontIcon fontIcon, KeyCodeCombination keyCodeCombination) {
        item.setGraphic(fontIcon);
        item.setAccelerator(keyCodeCombination);
    }

    /**
     * 设置图标
     * @param button 按钮
     * @param fontIcon 图标
     */
    public static void setIcon(Labeled button, Node fontIcon) {
        button.setGraphic(fontIcon);
    }

    /**
     * 获取key的标签
     * @param type key类型
     * @return 标签
     */
    public static Label getKeyTypeLabel(String type) {
       Tuple2<String,String> tag= getKeyTypeTag(type);
        return createTypeLabel(tag);
    }

    private final static String TAG_ICON_CSS = """
                .tag-icon {
                    -fx-icon-color: %s;
                }
                """;
    /**
     * key颜色表情获取
     * @param type 类型
     * @return 圆圈tag
     */
    public static Label getKeyColorFontIcon(String type) {
        Label label = new Label();
        FontIcon fontIcon = new FontIcon(Material2AL.FIBER_MANUAL_RECORD);
        label.setGraphic(fontIcon);
        if(type==null){
            return label;
        }
        Tuple2<String, String> tag = getKeyTypeTag(type);
        fontIcon.getStyleClass().add("tag-icon");
        label.getStylesheets().add(Styles.toDataURI(TAG_ICON_CSS.formatted(tag.t2())));
        return label;
    }
    public static void getSetFontIconColorByKeyType(String type, Button search) {
        Tuple2<String, String> tag = getKeyTypeTag(type);
        search.getStylesheets().clear();
        search.getStylesheets().addLast(Styles.toDataURI(TAG_ICON_CSS.formatted(tag.t2())));
    }
    /**
     * 获取key name的label表示
     * @param type key类型
     * @return 标签
     */
    public static Label getKeyTypeNameLabel(String type) {
        Tuple2<String,String> tag= getKeyTypeNameTag(type);
        return createTypeLabel(tag);
    }

    /**
     * 创建标签
     * @param tag 标签
     * @return 标签
     */
    private static Label createTypeLabel(Tuple2<String, String> tag) {
        Label tagLabel = new Label(tag.t1());
        tagLabel.getStyleClass().add("tag");
        tagLabel.setStyle("-fx-background-color:"+tag.t2());
        return tagLabel;
    }


    /**
     * 获取key的标签 大的
     * @param type key类型
     * @return 标签
     */
    public static Label getKeyTypeLabelMax(String type) {
        Label tagLabel = getKeyTypeNameLabel(type);
        tagLabel.getStyleClass().add("max");
        return tagLabel;
    }

    /**
     * 获取key的标签 颜色
     * @param type key类型
     * @return 颜色
     */
    public static Tuple2<String, String> getKeyTypeTag(String type) {
        KeyTagSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAG.name);
        int i  =RedisDataTypeEnum.getIndex(type);
        return new Tuple2<>(setting.getTags().get(i),setting.getColors().get(i));
    }
    /**
     * 获取key name的标签 颜色
     * @param type key类型
     * @return 颜色
     */
    public static Tuple2<String, String> getKeyTypeNameTag(String type) {
        KeyTagSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAG.name);
        int i  =RedisDataTypeEnum.getIndex(type);
        RedisDataTypeEnum byType = RedisDataTypeEnum.getByType(type);
        return new Tuple2<>(byType.type,setting.getColors().get(i));
    }


    /**
     * 加载fxml
     * @param fxml fxml文件
     * @param <T1> 容器
     * @param <T2> 控制器
     * @return 新的容器和控制器
     */
    public static  <T1,T2> @NotNull Tuple2<T1,T2> doLoadFxml(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml), Main.RESOURCE_BUNDLE);
            T1 t1 = fxmlLoader.load();
            T2 t2 = fxmlLoader.getController();
            return new Tuple2<>(t1,t2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 设置TabPane的side
     * @param tabPane tab容器
     * @param side 边
     */
    public static void setTabPaneSide(TabPane tabPane, Side side) {
        tabPane.setSide(side);
    }

    /**
     * 颜色转16进制
     * @param newValue 颜色
     * @return 16进制
     */
    public static String color2hex(Color newValue) {
        return String.format("#%02X%02X%02X",
                (int)(newValue.getRed() * 255),
                (int)(newValue.getGreen() * 255),
                (int)(newValue.getBlue() * 255));
    }

    /**
     * 16进制转rgba
     * @param hexColor 16进制
     * @return rgba
     */
    public static String hexToRgba(String hexColor) {
        // 默认不透明
        return hexToRgba(hexColor, 1.0);
    }

    /**
     * 16进制转rgba
     * @param hexColor 16进制
     * @param alpha 透明度
     * @return rgba
     */
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
     * svg图片
     * @param svg svg文件
     * @param w 宽度
     * @return 图片
     */
    public static ImageView svgImageView(String svg,int w) {
        Image fxImage = svgImage(svg,w,1);
        return new ImageView(fxImage);
    }

    /**
     * svg图片
     * @param svg svg文件
     * @param w 宽度
     * @return 图片
     */
    public static Image svgImage(String svg,int w){
       return svgImage(svg,w,1);
    }
    /**
     * zoom用于图标在画布上的整体大小，zoom=2,相当于图片大小不变，图标缩小一倍
     * @param svg svg文件
     * @param w 宽度
     * @param zoom 缩放
     * @return  图片
     */
    public static Image svgImage(String svg,int w,int zoom) {
        SVGLoader loader = new SVGLoader();
        URL svgUrl = Main.class.getResource(svg);
        SVGDocument svgDocument = loader.load(Objects.requireNonNull(svgUrl));
        BufferedImage image = new BufferedImage(w*zoom,w*zoom,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        Objects.requireNonNull(svgDocument).render(null,g,new ViewBox((zoom-1)*w, (zoom-1)*w, w, w));
        g.dispose();
//        return SwingFXUtils.toFXImage(image, null);
        return toFXImage(image);
    }

    public static Image toFXImage(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            return new Image(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建普通的表格
     * @param tableView 表格
     * @param iTable  数据
     */
    @SuppressWarnings("unchecked")
    public static <T extends ITable,S>void initSimpleTableView(TableView<T> tableView, T iTable) {
        ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
        TableColumn<T, Integer> c0 = (TableColumn<T,Integer>)columns.getFirst();
        if("#row".equals(iTable.getProperties()[0])){
            c0.setCellValueFactory(
                    param -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(param.getValue()) + 1)
            );
        }
        c0.setSortable(false);
        for (int i = 0; i < columns.size(); i++) {
            if("#row".equals(iTable.getProperties()[i])){
                continue;
            }
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
     * @param tableView 表格
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
                // 默认行高
                rowHeight = 24.0;
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
 * 动态调整ListView高度，根据内容保持最小高度，最高不超过指定值
 * @param listView 列表视图
 * @param maxHeight 最大高度
 */
public static void adjustListViewHeight(ListView<?> listView, double maxHeight) {
    // 确保列表已经渲染
    listView.applyCss();
    listView.layout();

    double rowHeight = 0;
    // 获取行高（通过第一行计算）
    if (!listView.getItems().isEmpty()) {
        Node firstCell = listView.lookup(".list-cell");
        if (firstCell != null) {
            rowHeight = firstCell.getBoundsInLocal().getHeight();
        } else {
            // 默认行高
            rowHeight = 24.0;
        }
    }

    // 如果无法获取行高，则使用默认值
    if (rowHeight <= 0) {
        rowHeight = 24.0;
    }

    // 计算总高度（添加一些padding）
    int rowCount = listView.getItems().size();
    // 5px padding
    double totalHeight = rowCount * rowHeight + 5;

    // 限制最大高度
    double finalHeight = Math.min(totalHeight, maxHeight);

    // 设置高度
    listView.setPrefHeight(finalHeight);
    listView.setMinHeight(finalHeight);
    listView.setMaxHeight(finalHeight);
}

    /**
     * 文本提示
     * @param text 文本
     * @return 提示
     */
    public static Tooltip textTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        setShowDelay(tooltip);
        return tooltip;
    }

    /**
     * 设置提示延迟
     */
    private static void setShowDelay(Tooltip tooltip) {
        // 200ms 延迟
        tooltip.setShowDelay(Duration.millis(200));
        // 100ms 隐藏延迟
        tooltip.setHideDelay(Duration.millis(100));
    }

    /**
     * 带提示的文本提示
     * @param text 文本
     * @param hint 提示
     * @return 提示
     */
    public static Tooltip hintTooltip(String text,String hint) {
        HintTooltip tooltip = new HintTooltip(text,hint);
        setShowDelay(tooltip);
        return tooltip;
    }



    /**
     * 获取需要的主题颜色
     * @return 主题颜色
     */
    public static Map<String, String> themeNeedColors() {
        try {
            Map<String, String> map = ThemeManager.getInstance().getTheme().parseColors();
            Map<String, String> newMap = new HashMap<>();
            Constant.NEED_COLORS
                    .forEach(colorKey -> {
                        if (map.containsKey(colorKey)) {
                            newMap.put(colorKey, map.get(colorKey));
                        }
                    });
            return newMap;
        } catch (IOException e) {
            log.error("themeNeedColors exception", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取需要的主题颜色
     * @param key 颜色key
     * @return 颜色值
     */
    public static String themeNeedColors(String key) {
        try {
            Map<String, String> map = ThemeManager.getInstance().getTheme().parseColors();
            return map.get(key);
        } catch (IOException e) {
            log.error("themeNeedColors exception", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置tab
     * @param tab  tab
     * @param dbTabPane dbTabPane
     * @param tuple2 tuple2
     */
    public static void setTab(Tab tab, TabPane dbTabPane, Tuple2<? extends Node,? extends BaseController> tuple2) {
        // 监听Tab被关闭事件,但是remove是无法监听的
        tab.setOnClosed(event2 -> ThreadPool.getInstance().execute(()->tuple2.t2().close()));
        tab.setTooltip(GuiUtil.textTooltip(tab.getText()));
        GuiUtil.newTabContextMenu(tab);
        tab.setContent(tuple2.t1());
        dbTabPane.getTabs().add(tab);
        dbTabPane.getSelectionModel().select(tab);
    }

    /**
     * 计算文本宽度
     * @param font 字体
     * @param text 文本
     * @param maxWidth 最大宽度
     * @return 宽度
     */
    public static double computeTextWidth(Font font, String text, int maxWidth) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(font);
        textNode.setWrappingWidth(maxWidth);
        return textNode.getLayoutBounds().getWidth();
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
     * 目录选择
     *
     * @param ownerWindow 父窗口
     * @param last 最后选择的一个目录，用于位置参考
     * @return 选择的目录
     */
    public static File directoryChoose(Window ownerWindow, File last) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(language("file.choose"));

        if (last != null && last.exists() && last.isDirectory()) {
            directoryChooser.setInitialDirectory(last);
        }

        return directoryChooser.showDialog(ownerWindow);
    }

    /**
     * 文件选择
     *
     * @param ownerWindow 父窗口
     * @param last 最后选择的一个文件，用于位置参考
     * @return  文件
     */
    public static File fileChoose(Window ownerWindow, File last) {
        return fileChoose(ownerWindow,last,"file(*)","*.*");
    }
    /**
     * 文件选择
     *
     * @param ownerWindow 父窗口
     * @param last 最后选择的一个文件，用于位置参考
     * @param description 文件描述
     * @param extensions 文件扩展名
     * @return  文件
     */
    public static File fileChoose(Window ownerWindow, File last, String description, String... extensions) {
        FileChooser fileChooser =createFileChooser(last,language("file.choose"),description,extensions);
        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * 创建文件选择器
     * @param last 最后一个文件
     * @param title 标题
     * @param description 描述
     * @param extensions 扩展名
     * @return 文件选择器
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
     * 文件保存
     *
     * @param ownerWindow 父窗口
     * @param last 最后选择一个文件，用于位置参考
     * @param defaultFileName 默认文件名
     * @return 选择的文件
     */
    public static File saveFileChoose(Window ownerWindow, File last, String defaultFileName) {
        FileChooser fileChooser = createSaveFileChooser(last, language("file.save"), defaultFileName, "*.*");
        return fileChooser.showSaveDialog(ownerWindow);
    }

    /**
     * 创建文件保存选择器
     *
     * @param last            最后一个文件
     * @param title           标题
     * @param defaultFileName 默认文件名
     * @param extensions      扩展名
     * @return 文件选择器
     */
    private static FileChooser createSaveFileChooser(File last, String title, String defaultFileName, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        // 设置初始目录
        if (last != null && last.exists()) {
            if (last.isDirectory()) {
                fileChooser.setInitialDirectory(last);
            } else {
                // 如果是文件，则使用其父目录作为初始目录
                fileChooser.setInitialDirectory(last.getParentFile());
            }
        }
        // 设置默认文件名
        fileChooser.setInitialFileName(defaultFileName);
        // 设置文件过滤器
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("file(*)", extensions);
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

}

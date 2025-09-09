package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import xyz.hashdog.rdm.redis.client.RedisSubscriber;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class PubSubController extends BaseClientController<ServerTabController> implements Initializable {

    public WebView webView;

    public StackPane webViewContainer;
    private final StringBuilder tableContent = new StringBuilder();
    public TextField subChannel;
    public ToggleButton subscribe;
    public TextField pubChannel;
    public TextField pubMessage;
    public Button publish;
    public Label messageSize;
    private int messageCounter = 0;
    private static final int MAX_MESSAGES = 200;

    private RedisSubscriber subscriber;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLanguage();
        initButton();
        webView.setContextMenuEnabled(false);
        initCustomContextMenu();
        initWebView();
        applyTheme();
        addTmEventSubscriber(ThemeEvent.class, e -> applyTheme());
    }

    private void initLanguage() {
        if (subscribe.isSelected()) {
            subscribe.setText(language("server.pubsub.unsubscribe"));
        } else {
            subscribe.setText(language("server.pubsub.subscribe"));
        }
        publish.setText(language("server.pubsub.publish"));
    }

    @Override
    protected void paramInitEnd() {
        super.paramInitEnd();
        this.subscriber = this.redisClient.subscriber();
    }

    private void initButton() {
        publish.getStyleClass().addAll(Styles.ACCENT);
    }

    /**
     * 初始化自定义上下文菜单
     */
    private void initCustomContextMenu() {
        // 清空日志
        MenuItem clearItem = new MenuItem(language("server.pubsub.clear"));
        clearItem.setOnAction(e -> clearMessages());

        // 复制选中文本
        MenuItem copyItem = new MenuItem(language("main.edit.copy"));
        copyItem.setOnAction(e -> GuiUtil.copyWebViewSelectedText(webView));

        // 全选
        MenuItem selectAllItem = new MenuItem(language("main.edit.selectall"));
        selectAllItem.setOnAction(e -> GuiUtil.selectWebViewAllText(webView, "table-body"));

        // 保存日志
        MenuItem saveItem = new MenuItem(language("server.pubsub.save"));
        saveItem.setOnAction(e -> saveLogs());
        GuiUtil.setWebViewContextMenu(clearItem, copyItem, selectAllItem, saveItem, webView);
    }


    /**
     * 保存日志
     */
    private void saveLogs() {
        // 实现保存日志功能
        System.out.println("保存日志功能待实现");
        // 可以使用FileChooser来实现文件保存功能
    }


    /**
     * 应用暗色主题
     */
    public void applyTheme() {
        int fontSize = ThemeManager.getInstance().getFontSize();
        String fontFamily = ThemeManager.getInstance().getFontFamily();
        Map<String, String> colors = GuiUtil.themeNeedColors();
        // hover文字色
        String hover = "rgba(255,255,255,0.05)";
        // body文字色
        String text = colors.get(Constant.THEME_COLOR_FG_DEFAULT);
        // 时间戳颜色
        String time = colors.get(Constant.THEME_COLOR_SUCCESS_FG);
        // 类型颜色
        String type = colors.get(Constant.THEME_COLOR_ACCENT_FG);
        //边框色
        String border = colors.get(Constant.THEME_COLOR_BORDER_DEFAULT);

        updateAllStyles(
                border,
                fontFamily,
                colors.get(Constant.THEME_COLOR_BG_SUBTLE),
                hover,
                time,
                type,
                text,
                fontSize + "px"
        );
    }


    private void initWebView() {
        // 初始化HTML内容，包含表格结构
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                    </style>
                </head>
                <body>
                    <table id="message-table">
                        <thead>
                            <tr>
                                <th class="timestamp">%s</th>
                                <th class="channel">%s</th>
                                <th class="message">%s</th>
                            </tr>
                        </thead>
                        <tbody id="table-body">
                        </tbody>
                    </table>
                </body>
                </html>
                """;

        webView.getEngine().loadContent(String.format(htmlContent, language("server.pubsub.time"), language("server.pubsub.channel"), language("server.pubsub.message")));
    }


    /**
     * 动态更新整个样式表
     *
     * @param bodyBgColor    body背景颜色
     * @param hoverColor     hover颜色
     * @param timestampColor 时间戳颜色
     * @param commandColor   命令颜色
     * @param fontSize       字体大小
     */
    public void updateAllStyles(String borderColor, String fontFamily, String bodyBgColor, String hoverColor, String timestampColor,
                                String typeColor, String commandColor, String fontSize) {
        Platform.runLater(() -> {
            String cssContent = """
                     body {
                        font-family: ${fontFamily};
                        background-color: ${bodyBgColor};
                        color: #fff;
                        margin: 0;
                        padding: 10px;
                        font-size: ${fontSize};
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 10px;
                        table-layout: fixed;
                    }
                    thead {
                        position: sticky;
                        top: 0;
                        background-color: rgb(60,60,62);
                    }
                    th {
                        background-color: ${bodyBgColor};
                        color: #fff;
                        text-align: left;
                        padding: 8px 12px;
                        border-bottom: 2px solid #555;
                        font-weight: 600;
                        white-space: nowrap;
                    }
                    td {
                        padding: 6px 12px;
                        border-bottom: 0px solid ${borderColor};
                        text-align: left;
                        vertical-align: top;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }
                    tr:hover {
                        background-color: ${hoverColor};
                    }
                    .timestamp {
                        color: ${timestampColor};
                        width: 25%;
                    }
                    .channel {
                        color: ${typeColor};
                        width: 25%;
                    }
                    .message {
                        color: ${commandColor};
                        width: 50%;
                        word-break: break-all;
                    }
                    #message-table {
                        width: 100%;
                    }
                    """;
            cssContent = cssContent.replace("${fontFamily}", fontFamily).replace("${bodyBgColor}", bodyBgColor)
                    .replace("${fontSize}", fontSize).replace("${bodyBgColor}", bodyBgColor).replace("${timestampColor}", timestampColor)
                    .replace("${typeColor}", typeColor).replace("${commandColor}", commandColor).replace("${typeColor}", typeColor)
                    .replace("${hoverColor}", hoverColor).replace("${borderColor}", borderColor);

            updateStyleSheet(cssContent);
        });
        webViewContainer.setStyle(String.format("-fx-border-color: %s; -fx-border-width: 1px; -fx-border-style: solid;", borderColor));
    }


    /**
     * 更新样式表
     *
     * @param cssContent 新的CSS内容
     */
    private void updateStyleSheet(String cssContent) {
        String script = String.format("""
                (function() {
                    var newStyle = document.createElement('style');
                    newStyle.type = 'text/css';
                    newStyle.innerHTML = `%s`;
                    var head = document.getElementsByTagName('head')[0];
                    var oldStyle = document.getElementById('dynamic-style');
                    if (oldStyle) {
                        head.removeChild(oldStyle);
                    }
                    newStyle.id = 'dynamic-style';
                    head.appendChild(newStyle);
                })();
                """, cssContent.replace("`", "\\`"));

        webView.getEngine().executeScript(script);
    }

    /**
     * 添加订阅消息到表格
     *
     * @param timestamp 时间戳
     * @param channel   频道
     * @param message   消息内容
     */
    public void addSubscriptionMessage(String timestamp, String channel, String message) {
        Platform.runLater(() -> {
            // 创建表格行，确保数据居左对齐
            String tableRow = String.format(
                    "<tr>" +
                            "<td class='timestamp'>%s</td>" +
                            "<td class='channel'>%s</td>" +
                            "<td class='message'>%s</td>" +
                            "</tr>",
                    escapeHtml(timestamp),
                    escapeHtml(channel),
                    escapeHtml(message)
            );

            tableContent.append(tableRow);
            messageCounter++;

            // 限制最大消息数
            if (messageCounter > MAX_MESSAGES) {
                String currentContent = tableContent.toString();
                int firstRowEnd = currentContent.indexOf("</tr>") + 5;
                if (firstRowEnd > 0 && firstRowEnd < currentContent.length()) {
                    tableContent.delete(0, firstRowEnd);
                }
                messageCounter--;
            }

            messageSize.setText(String.valueOf(messageCounter));

            // 更新表格内容
            String script = String.format(
                    "var tbody = document.getElementById('table-body');" +
                            "tbody.innerHTML = `%s`;" +
                            "window.scrollTo(0, document.body.scrollHeight);",
                    tableContent.toString().replace("`", "\\`")
            );

            webView.getEngine().executeScript(script);
        });
    }

    /**
     * HTML转义，防止XSS攻击并保持格式
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("\n", "<br>")
                .replace(" ", "&nbsp;");
    }

    /**
     * 清空消息表格
     */
    private void clearMessages() {
        Platform.runLater(() -> {
            tableContent.setLength(0);
            messageCounter = 0;
            webView.getEngine().executeScript("document.getElementById('table-body').innerHTML = '';");
        });
    }




    /**
     * 取消订阅
     */
    private void unsubscribe() {
        this.subscriber.unsubscribe();
    }

    /**
     * 订阅/取消订阅
     */
    @FXML
    public void subscribe(ActionEvent actionEvent) {
        if (subscribe.isSelected()) {
            subscribe.setText(language("server.pubsub.unsubscribe"));
            this.subscriber.redisPubSub((channel, msg) -> addSubscriptionMessage(LocalDateTime.now().toString(), channel, msg)).text(subChannel.getText());
            this.subscriber.subscribe();
            subChannel.setEditable(false);
        } else {
            this.unsubscribe();
            subscribe.setText(language("server.pubsub.subscribe"));
            subChannel.setEditable(true);
        }

    }



    /**
     * 发布消息
     *
     * @param actionEvent 事件对象
     */
    @FXML
    public void publish(ActionEvent actionEvent) {
        async(() -> this.redisClient.publish(pubChannel.getText(), pubMessage.getText()));
    }

    @Override
    public void close() {
        super.close();
        unsubscribe();
    }
}
























package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.hashdog.rdm.redis.client.RedisPubSub;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public class PubSubController extends BaseClientController<ServerTabController> implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(PubSubController.class);

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
    private static final int MAX_MESSAGES = 1000;
    private volatile Thread subscribeThread;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initButton();
        webView.setContextMenuEnabled(false);
        initCustomContextMenu();
        initWebView();
        applyTheme();
        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            applyTheme();
        });



    }

    private void initButton() {
        publish.getStyleClass().addAll(Styles.ACCENT);
    }

    /**
     * 初始化自定义上下文菜单
     */
    private void initCustomContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        // 清空日志
        MenuItem clearItem = new MenuItem(language("server.pubsub.clear"));
        clearItem.setOnAction(e -> clearMessages());

        // 复制选中文本
        MenuItem copyItem = new MenuItem(language("main.edit.copy"));
        copyItem.setOnAction(e -> copySelectedText());

        // 全选
        MenuItem selectAllItem = new MenuItem(language("main.edit.selectall"));
        selectAllItem.setOnAction(e -> selectAllText());

        // 保存日志
        MenuItem saveItem = new MenuItem(language("server.pubsub.save"));
        saveItem.setOnAction(e -> saveLogs());

        // 添加菜单项
        contextMenu.getItems().addAll(
                clearItem,
                new SeparatorMenuItem(),
                copyItem,
                selectAllItem,
                new SeparatorMenuItem(),
                saveItem
        );

        // 设置右键菜单事件
        webView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                // 检查是否有选中文本，来决定是否启用复制选项
                String selectedText = (String) webView.getEngine().executeScript("window.getSelection().toString();");
                copyItem.setDisable(selectedText == null || selectedText.isEmpty());

                contextMenu.show(webView, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });

        // 点击其他地方隐藏菜单
        webView.setOnMousePressed(event -> {
            if (contextMenu.isShowing() && event.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });
    }



    /**
     * 复制选中文本
     */
    private void copySelectedText() {
        String selectedText = (String) webView.getEngine().executeScript("window.getSelection().toString();");
        if (selectedText != null && !selectedText.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            clipboard.setContent(content);
        }
    }

    /**
     * 全选文本
     */
    private void selectAllText() {
        webView.getEngine().executeScript(
                "var selection = window.getSelection();" +
                        "var range = document.createRange();" +
                        "range.selectNodeContents(document.getElementById('log-container'));" +
                        "selection.removeAllRanges();" +
                        "selection.addRange(range);"
        );
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
    public void applyTheme()  {
        SamplerTheme theme = ThemeManager.getInstance().getTheme();
        int fontSize = ThemeManager.getInstance().getFontSize();
        String fontFamily = ThemeManager.getInstance().getFontFamily();
        Map<String, String> colors = null;
        try {
            colors = theme.parseColors();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String c1 = colors.get("-color-bg-subtle");
        String c2 = colors.get("-color-fg-default");
        String c3 = colors.get("-color-success-fg");
        String c4 = colors.get("-color-accent-fg");
//        String c5 = colors.get("-color-border-default");
//        String c6 = colors.get("-color-bg-inset").replace(")",",0.5)"); todo没有找到合适的hover颜色
        String c6 = "rgba(255,255,255,0.05)";
        String c5 = colors.get("-color-base-9");
        updateAllStyles(
                c5,
                fontFamily,
                c1,    // body背景色
                c6,           // hover文字色
                c3,           // 时间戳颜色
                c4,           // 类型颜色
                c2,           // 命令颜色
                fontSize+"px"               // 字体大小
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
     * @param bodyBgColor body背景颜色
     * @param hoverColor
     * @param timestampColor 时间戳颜色
     * @param commandColor 命令颜色
     * @param fontSize 字体大小
     */
    public void updateAllStyles(String borderColor,String fontFamily,String bodyBgColor, String hoverColor, String timestampColor,
                                 String typeColor,String commandColor, String fontSize) {
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
            cssContent=cssContent.replace("${fontFamily}",fontFamily).replace("${bodyBgColor}",bodyBgColor)
                    .replace("${fontSize}",fontSize).replace("${bodyBgColor}",bodyBgColor).replace("${timestampColor}",timestampColor)
                    .replace("${typeColor}",typeColor).replace("${commandColor}",commandColor).replace("${typeColor}",typeColor)
                    .replace("${hoverColor}",hoverColor).replace("${borderColor}",borderColor);

            updateStyleSheet(cssContent);
        });
        webViewContainer.setStyle(String.format("-fx-border-color: %s; -fx-border-width: 1px; -fx-border-style: solid;",borderColor));
    }


    /**
     * 更新样式表
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
     * @param timestamp 时间戳
     * @param channel 频道
     * @param message 消息内容
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
            messageSize.setText(String.valueOf(messageCounter));

            // 限制最大消息数
            if (messageCounter > MAX_MESSAGES) {
                // 简单处理：清空并重新添加（实际应用中可能需要更精确的处理）
                String currentContent = tableContent.toString();
                int firstRowEnd = currentContent.indexOf("</tr>") + 5;
                if (firstRowEnd > 0 && firstRowEnd < currentContent.length()) {
                    tableContent.delete(0, firstRowEnd);
                }
                messageCounter--;
            }

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
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
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

    @Override
    public void close() {
        super.close();
        unsubscribe();
    }
    /**
     * 取消订阅
     */
    private void unsubscribe() {
        async(() -> {
            if (subscribeThread != null && subscribeThread.isAlive()) {
                // 中断监控线程
                subscribeThread.interrupt();
                try {
                    subscribeThread.join(2000);
                } catch (InterruptedException e) {
                    log.error("unsubscribe Exception", e);
                }
                subscribeThread = null;
                this.redisClient.unsubscribe(subChannel.getText());
            }
        });

    }

    @FXML
    public void subscribe(ActionEvent actionEvent) {
        if (subscribe.isSelected()) {
            subscribe.setText(language("server.pubsub.unsubscribe"));
            subscribeThread = new Thread(() -> {
                try {
                    this.redisClient.psubscribe(new RedisPubSub() {
                        @Override
                        public void onMessage(String channel, String msg) {
                            addSubscriptionMessage(LocalDateTime.now().toString(),channel,msg);
                        }
                    },subChannel.getText());
                }catch (Exception e){
                    // 检查是否因为中断导致的异常
                    if (Thread.currentThread().isInterrupted()) {
                        log.warn("subscribe is interrupted");
                    } else {
                        log.error("subscribe error",e);
                    }
                }

            });
            subscribeThread.setDaemon(true);
            subscribeThread.start();
            subChannel.setEditable(false);
        }else {
            this.unsubscribe();
            subscribe.setText(language("server.pubsub.subscribe"));
            subChannel.setEditable(true);
        }

    }



    public void publish(ActionEvent actionEvent) {
        async(() -> {
            this.redisClient.publish(pubChannel.getText(),pubMessage.getText());
        });
    }
}
























package xyz.hashdog.rdm.ui.controller;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import xyz.hashdog.rdm.redis.client.RedisMonitor;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class MonitorController extends BaseClientController<ServerTabController> implements Initializable {


    public WebView webView;

    private final StringBuilder logContent = new StringBuilder();
    public StackPane webViewContainer;
    private int logCounter = 0;
    private static final int MAX_LOG_LINES = 20;
    private Thread monitorThread;
    private RedisMonitor redisMonitor;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView.setContextMenuEnabled(false);
        initCustomContextMenu();
        initWebView();
        applyTheme();
        addTmEventSubscriber(ThemeEvent.class, e -> {
            applyTheme();
        });

    }

    @Override
    protected void paramInitEnd() {
        monitorThread = new Thread(() -> {
            this.redisClient.monitor(redisMonitor=new RedisMonitor() {
                @Override
                public void onCommand(String msg) {
                    addLogLine(parseLogToList(msg));
                }
            });
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * 初始化自定义上下文菜单
     */
    private void initCustomContextMenu() {
        // 清空日志
        MenuItem clearItem = new MenuItem(language("server.monitor.clear"));
        clearItem.setOnAction(e -> clearLogs());

        // 复制选中文本
        MenuItem copyItem = new MenuItem(language("main.edit.copy"));
        copyItem.setOnAction(e -> copySelectedText());

        // 全选
        MenuItem selectAllItem = new MenuItem(language("main.edit.selectall"));
        selectAllItem.setOnAction(e -> selectAllText());

        // 保存日志
        MenuItem saveItem = new MenuItem(language("server.monitor.save"));
        saveItem.setOnAction(e -> saveLogs());
        GuiUtil.setWebViewContextMenu(clearItem,copyItem,selectAllItem,saveItem,webView);


    }

    /**
     * 清空日志
     */
    private void clearLogs() {
        Platform.runLater(() -> {
            logContent.setLength(0);
            logCounter = 0;
            webView.getEngine().executeScript("document.getElementById('log-container').innerHTML = '';");
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
     * 将Redis监控日志解析为List<String> (简化版本)
     * @param logLine 日志内容，例如: 1753941855.532005 [0 172.18.0.1:42170] "XLEN" "stream1"
     * @return 包含四个元素的列表：[时间戳, 客户端地址, 命令, 参数]
     */
    public List<String> parseLogToList(String logLine) {
        try {
            String time = logLine.substring(0, logLine.indexOf(" ")).trim();
            String host = logLine.substring(logLine.indexOf("["),logLine.indexOf("]")+1).trim();
            String end = logLine.substring(logLine.indexOf("]")+1).trim();
            String type="";
            String parm="";
            if(end.contains(" ")){
                type = end.substring(0, end.indexOf(" ")).trim();
                parm = end.substring( end.indexOf(" ")).trim();
            }else {
                type = end;
            }

            return List.of(time,host,type,parm);
        }catch (Exception e){
            return List.of(logLine,"","","");
        }

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
        String c5 = colors.get("-color-border-default");
        updateAllStyles(
                c5,
                fontFamily,
                c1,    // body背景色
                c2,           // body文字色
                c3,           // 时间戳颜色
                c2,           // 客户端信息颜色
                c4,           // 类型颜色
                c2,           // 命令颜色
                fontSize+"px"               // 字体大小
        );
    }


    private void initWebView() {
        // 初始化HTML内容
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { 
                        font-family: monospace; 
                        background-color: rgb(44,44,46); 
                        color: #fff; 
                        margin: 0; 
                        padding: 5px;
                        font-size: 14px;
                    }
                    .log-line { margin: 2px 0; }
                    .timestamp { color: #4EC9B0; } /* 时间戳颜色 */
                    .client-info { color: #C586C0; } /* 客户端信息颜色 */
                    .command { color: #DCDCAA; } /* 命令颜色 */
                </style>
            </head>
            <body>
                <div id="log-container"></div>
            </body>
            </html>
            """;

        webView.getEngine().loadContent(htmlContent);
    }


    /**
     * 动态更新整个样式表
     * @param bodyBgColor body背景颜色
     * @param bodyColor body文字颜色
     * @param timestampColor 时间戳颜色
     * @param hostColor 客户端信息颜色
     * @param commandColor 命令颜色
     * @param fontSize 字体大小
     */
    public void updateAllStyles(String borderColor,String fontFamily,String bodyBgColor, String bodyColor, String timestampColor,
                                String hostColor, String typeColor,String commandColor, String fontSize) {
        Platform.runLater(() -> {
            String cssContent = String.format("""
                body { 
                    font-family: %s; 
                    background-color: %s; 
                    color: %s; 
                    margin: 0; 
                    padding: 5px;
                    font-size: %s;
                }
                .log-line { margin: 2px 0; }
                .timestamp { color: %s; }
                .host { color: %s; }
                .type { color: %s; }
                .command { color: %s; }
                """, fontFamily,bodyBgColor, bodyColor, fontSize, timestampColor, hostColor, typeColor,commandColor);

            updateStyleSheet(cssContent);
        });
        webViewContainer.setStyle(String.format("-fx-border-color: %s; -fx-border-width: 1px; -fx-border-style: solid;",borderColor));
    }
    private static final List<String> CLASS=List.of("timestamp", "host","type" ,"command");


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
     * 添加日志行
     * @param logs 日志内容，例如: 10:58:13.606 [0 172.18.0.1:36200] "TYPE" "foo"
     */
    public void addLogLine(List<String> logs) {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='log-line'>");
            String span="<span class='%s'>%s </span>";
            for (int i = 0; i < logs.size(); i++) {
                // 添加到内容中
                sb.append(String.format(span,CLASS.get(i),logs.get(i)));
            }
            sb.append("</div>").append("\n");
            logContent.append(sb);
            logCounter++;

            // 限制最大行数
            if (logCounter > MAX_LOG_LINES) {
                int index = logContent.indexOf("\n");
                if (index != -1) {
                    logContent.delete(0, index + 1);
                }
                logCounter--;
            }
            // 更新WebView
            String script = String.format(
                    "document.getElementById('log-container').innerHTML = `%s`;" +
                            "window.scrollTo(0, document.body.scrollHeight);",
                    logContent.toString().replace("`", "\\`")
            );

            webView.getEngine().executeScript(script);
        });
    }

    @Override
    public void close() {
        if (monitorThread != null && monitorThread.isAlive()) {
            // 中断监控线程
            monitorThread.interrupt();
            monitorThread = null;
        }
        if(redisMonitor!=null){
            redisMonitor.close();
        }
    }
}
























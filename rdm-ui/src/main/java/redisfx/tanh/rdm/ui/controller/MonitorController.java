package redisfx.tanh.rdm.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.redis.client.RedisMonitor;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.sampler.event.ThemeEvent;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class MonitorController extends BaseClientController<ServerTabController> implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MonitorController.class);

    public WebView webView;

    private final List<String> logContent = new ArrayList<>();
    public StackPane webViewContainer;
    public Label commandsSize;
    public TextField filter;
    public ToggleButton start;
    private int logCounter = 0;
    private static final int MAX_LOG_LINES = 1000;
    private RedisMonitor redisMonitor;
    private final Queue<List<String>> logQueue = new ConcurrentLinkedQueue<>();
    private static final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView.setContextMenuEnabled(false);
        initCustomContextMenu();
        initWebView();
        applyTheme();
        addTmEventSubscriber(ThemeEvent.class, e -> applyTheme());
    }


    @Override
    protected void paramInitEnd() {
        //默认命令监控是启动的
        start.setSelected(true);
        startCheck();
    }


    /**
     * 启动监控
     */
    private void startMonitor() {
        Thread monitorThread = new Thread(() -> this.redisClient.monitor(redisMonitor = new RedisMonitor() {
            @Override
            public void onCommand(String msg) {
                logQueue.offer(parseLogToList(msg));
                tryAddLogLine();
            }
        }));
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * 停止监控
     */
    private void stopMonitor() {
        if (redisMonitor != null) {
            redisMonitor.close();
        }
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
        copyItem.setOnAction(e -> GuiUtil.copyWebViewSelectedText(webView));

        // 全选
        MenuItem selectAllItem = new MenuItem(language("main.edit.selectall"));
        selectAllItem.setOnAction(e -> GuiUtil.selectWebViewAllText(webView, "log-container"));

        // 保存日志
        MenuItem saveItem = new MenuItem(language("server.monitor.save"));
        saveItem.setOnAction(e -> saveLogs());
        GuiUtil.setWebViewContextMenu(clearItem, copyItem, selectAllItem, saveItem, webView);


    }

    /**
     * 清空日志
     */
    private void clearLogs() {
        Platform.runLater(() -> {
            logContent.clear();
            commandsSize.setText(String.valueOf(0));
            logCounter = 0;
            webView.getEngine().executeScript("document.getElementById('log-container').innerHTML = '';");
        });
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
     *
     * @param logLine 日志内容，例如: 1753941855.532005 [0 172.18.0.1:42170] "XLEN" "stream1"
     * @return 包含四个元素的列表：[时间戳, 客户端地址, 命令, 参数]
     */
    public List<String> parseLogToList(String logLine) {
        try {
            String time = logLine.substring(0, logLine.indexOf(" ")).trim();
            String host = logLine.substring(logLine.indexOf("["), logLine.indexOf("]") + 1).trim();
            String end = logLine.substring(logLine.indexOf("]") + 1).trim();
            String type;
            String parm = "";
            if (end.contains(" ")) {
                type = end.substring(0, end.indexOf(" ")).trim();
                parm = end.substring(end.indexOf(" ")).trim();
            } else {
                type = end;
            }

            return List.of(time, host, type, parm);
        } catch (Exception e) {
            return List.of(logLine, "", "", "");
        }

    }


    /**
     * 应用暗色主题
     */
    public void applyTheme() {
        ThemeManager manager = ThemeManager.getInstance();
        // 字体大小
        int fontSize = manager.getFontSize();
        String fontFamily = manager.getFontFamily();
        Map<String, String> colors = GuiUtil.themeNeedColors();
        // body背景色
        String body = colors.get(Constant.THEME_COLOR_BG_SUBTLE);
        // body文字色// 命令颜色 // 客户端信息颜色
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
                body,
                text,
                time,
                text,
                type,
                text,
                fontSize + "px"
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
     *
     * @param bodyBgColor    body背景颜色
     * @param bodyColor      body文字颜色
     * @param timestampColor 时间戳颜色
     * @param hostColor      客户端信息颜色
     * @param commandColor   命令颜色
     * @param fontSize       字体大小
     */
    public void updateAllStyles(String borderColor, String fontFamily, String bodyBgColor, String bodyColor, String timestampColor,
                                String hostColor, String typeColor, String commandColor, String fontSize) {
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
                    """, fontFamily, bodyBgColor, bodyColor, fontSize, timestampColor, hostColor, typeColor, commandColor);

            updateStyleSheet(cssContent);
        });
        webViewContainer.setStyle(String.format("-fx-border-color: %s; -fx-border-width: 1px; -fx-border-style: solid;", borderColor));
    }

    private static final List<String> CLASS = List.of("timestamp", "host", "type", "command");


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
     * 添加日志行
     * 日志内容，例如: 10:58:13.606 [0 172.18.0.1:36200] "TYPE" "foo"
     */
    public void tryAddLogLine() {
        //已经在处理中，则不处理
        if(LOCK.isLocked()){
            return;
        }
        async(() -> {
            try {
                LOCK.lock();
                processBatchLogs();
                Platform.runLater(() -> {
                    // 更新WebView
                    String script = String.format(
                            "document.getElementById('log-container').innerHTML = `%s`;" +
                                    "window.scrollTo(0, document.body.scrollHeight);",
                            String.join("\n", logContent).replace("`", "\\`")
                    );
                    commandsSize.setText(String.valueOf(logContent.size()));
                    webView.getEngine().executeScript(script);
                });
            }finally {
                LOCK.unlock();
            }
        });

    }

    /**
     * 处理批量日志
     */
    private void processBatchLogs() {
        List<String> subContent = new ArrayList<>();
        while (!logQueue.isEmpty()) {
            List<String> logs = logQueue.poll();
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='log-line'>");
            String span = "<span class='%s'>%s </span>";
            for (int i = 0; i < logs.size(); i++) {
                // 添加到内容中
                sb.append(String.format(span, CLASS.get(i), logs.get(i)));
            }
            sb.append("</div>").append("\n");
            subContent.add(sb.toString());
        }
        //如果本次处理的日志数就已经超过阈值，那么清空内容
        if(subContent.size()>MAX_LOG_LINES){
            logContent.clear();
        }
        logContent.addAll(subContent);
        logCounter+=subContent.size();
        // 限制最大行数
        if (logContent.size() > MAX_LOG_LINES) {
            List<String> newList = logContent.subList(0, logContent.size()-MAX_LOG_LINES);
            logContent.removeAll(newList);
        }
        log.info(" processed log：{},show log :{},log count:{}",subContent.size(),logContent.size(),logCounter);
    }

    @Override
    public void close() {
        stopMonitor();
    }

    /**
     * 启停
     *
     * @param actionEvent 触发事件
     */
    @FXML
    public void start(ActionEvent actionEvent) {
        if(start.isSelected()){
            startCheck();
        }else {
            GuiUtil.setIcon(start,new FontIcon(Material2MZ.PLAY_ARROW));
            start.setText(language("server.monitor.start"));
            stopMonitor();
            clearLogs();
        }
    }

    /**
     * 选择启动监控
     */
    private void startCheck() {
        GuiUtil.setIcon(start,new FontIcon(Material2MZ.PAUSE));
        start.setText(language("server.monitor.stop"));
        startMonitor();
    }
}
























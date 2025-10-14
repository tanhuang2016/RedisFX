package redisfx.tanh.rdm.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.redis.client.RedisMonitor;
import redisfx.tanh.rdm.ui.Main;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.*;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class MonitorController extends BaseClientController<ServerTabController> implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MonitorController.class);

    public CodeArea codeArea;;

    public StackPane webViewContainer;
    public Label commandsSize;
    public TextField filter;
    public ToggleButton start;
    private static final int MAX_LOG_LINES = 1000;
    private RedisMonitor redisMonitor;
    private final static List<String> STYLES = List.of("monitor-time", "monitor-host", "monitor-command", "monitor-param");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setStyle("""
                    -fx-background-color: %s;
                    -fx-border-color: %s;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """.formatted(Constant.THEME_COLOR_BG_SUBTLE, Constant.THEME_COLOR_BORDER_DEFAULT));
        webViewContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));
        // 直接添加样式表
        webViewContainer.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/css/text.css")).toExternalForm());
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
                append(msg);
            }
        }));
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    private void append(String msg) {
        List<String> msgList = parseLogToList(msg);
        Platform.runLater(() -> {
            if (codeArea.getParagraphs().size()> MAX_LOG_LINES) {
                int firstLineLength = codeArea.getParagraph(0).length() + 1;
                codeArea.deleteText(0, firstLineLength);
            }
            for (int i = 0; i < msgList.size(); i++) {
                codeArea.append(msgList.get(i),STYLES.get(i));
            }
            codeArea.appendText("\n");
            commandsSize.setText(String.valueOf(codeArea.getParagraphs().size()-1));
        });
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
    private void clearLogs() {
        Platform.runLater(() -> {
            codeArea.clear();
            commandsSize.setText(String.valueOf(codeArea.getParagraphs().size()));
        });
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
























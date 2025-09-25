package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.ModalPane;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.redis.imp.util.RedisCommandHelp;
import xyz.hashdog.rdm.redis.imp.util.RedisCommandHelpParser;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.RecentHistory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_CONNECT_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class ConsoleController extends BaseClientController<ServerTabController> implements Initializable {

    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public Label label;
    public ModalPane modalPane;
    private RecentHistory<String> recentHistory ;
    private int historyIndex = -1;

    private final List<RedisCommandHelp> redisCommandHelps = RedisCommandHelpParser.parseCommands();

    @FXML
    public void addToTextAreaAction(ActionEvent actionEvent) {
        String inputText = textField.getText();
        if (inputText.isEmpty()) {
            return;
        }
        recentHistory.add(inputText);
        if("clear".equalsIgnoreCase(inputText)){
            textArea.clear();
            textField.clear();
            return;
        }
        textArea.appendText( "\n"+"> "+inputText );
        textField.clear();
        ThreadPool.getInstance().execute(()->{
            List<String> strings = redisClient.getRedisConsole().sendCommand(inputText);
            Platform.runLater(()->{
                if(inputText.trim().startsWith("select")&&!strings.isEmpty()&& "ok".equalsIgnoreCase(strings.getFirst())){
                    this.currentDb=Integer.parseInt(inputText.replace("select","").trim());
                    label.setText(redisContext.getRedisConfig().getName()+":"+this.currentDb+">");
                }
                for (String string : strings) {
                    textArea.appendText( "\n"+string );
                }
            });
        });
        historyIndex = -1;
        modalPane.hide();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textArea.setPrefRowCount(10);
        recentHistory = new RecentHistory<>(20, e->{},false);
        modalPane.setAlignment(Pos.BOTTOM_CENTER);
        modalPane.usePredefinedTransitionFactories(Side.BOTTOM);
        // 添加按键监听器
        textField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> showPreviousCommand();
                case DOWN -> showNextCommand();
                case TAB -> {
                    // 获取输入框中的文本
                    String inputText = textField.getText();
                    handleCommandHelp(inputText);
                }
            }
        });
        // 监听 textField 文本变化
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                modalPane.hide();
                return;
            }
            // 实时处理输入文本
            handleCommandHelp(newValue);
        });
    }

    private boolean isProgrammaticChange = false;
    /**
     * 命令提示
     * @param inputText 输入文本
     */
    private void handleCommandHelp(String inputText) {
        if (isProgrammaticChange) {
            isProgrammaticChange = false;
            modalPane.hide();
            return;
        }
        ListView<String> commandListView = new ListView<>();
        List<String> commands = redisCommandHelps.stream()
                .filter(redisCommandHelp -> DataUtil.isBlank(inputText)||redisCommandHelp.getName().startsWith(inputText.toUpperCase()))
                .map(RedisCommandHelp::getSignature)
                .toList();
        commandListView.getItems().addAll(commands);
        modalPane.show(commandListView);
        GuiUtil.adjustListViewHeight(commandListView, 300);
    }

    // 添加历史命令浏览方法
    private void showPreviousCommand() {
        if(historyIndex== recentHistory.get().size()-1){
           return;
        }
        historyIndex++;
        if (historyIndex == -1||recentHistory.get().isEmpty()) {
            return;
        }

        if (historyIndex >= 0 && historyIndex < recentHistory.get().size()) {
            isProgrammaticChange = true;
            textField.setText(recentHistory.get().get(historyIndex));
            // 将光标移到末尾
            textField.positionCaret(textField.getText().length());
        }

    }

    private void showNextCommand() {
        if(historyIndex>=0){
            historyIndex--;
        }
        if (historyIndex == -1 ) {
            textField.clear();
            return;
        }
        isProgrammaticChange = true;
        textField.setText(recentHistory.get().get(historyIndex));
        // 将光标移到末尾
        textField.positionCaret(textField.getText().length());
    }

    @Override
    protected void paramInitEnd() {
        label.setText(redisContext.getRedisConfig().getName()+":"+this.currentDb+">");
        textArea.appendText( "\n"+redisContext.getRedisConfig().getName()+" "+language(ALERT_MESSAGE_CONNECT_SUCCESS) );
        if(currentDb!=0){
            ThreadPool.getInstance().execute(()->this.redisClient.select(currentDb));
        }

    }
}
























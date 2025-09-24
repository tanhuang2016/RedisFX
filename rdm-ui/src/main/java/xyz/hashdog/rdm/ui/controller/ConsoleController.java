package xyz.hashdog.rdm.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.util.RecentHistory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    private RecentHistory<String> recentHistory ;


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
    }
    private int historyIndex = -1;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textArea.setPrefRowCount(10);
        recentHistory = new RecentHistory<>(20, e->{},false);
        // 添加按键监听器
        textField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> showPreviousCommand();
                case DOWN -> showNextCommand();
            }
        });
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
























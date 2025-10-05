package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.controls.ModalPane;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import redisfx.tanh.rdm.common.pool.ThreadPool;
import redisfx.tanh.rdm.common.util.DataUtil;
import redisfx.tanh.rdm.redis.imp.util.RedisCommandHelp;
import redisfx.tanh.rdm.redis.imp.util.RedisCommandHelpParser;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import redisfx.tanh.rdm.ui.util.RecentHistory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static redisfx.tanh.rdm.ui.common.Constant.ALERT_MESSAGE_CONNECT_SUCCESS;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

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
    /**
     * 为true时不弹出命令提示框
     */
    private boolean isProgrammaticChange = false;
    /**
     * 历史选中行
     */
    private int historyIndex = -1;
    /**
     * 当前滚动条所在行
     */
    int scrollLine=0;
    private static final int MAXLINES = 1000;


    private final List<RedisCommandHelp> redisCommandHelps = RedisCommandHelpParser.parseCommands();

    @FXML
    public void addToTextAreaAction(ActionEvent actionEvent) {
        if(modalPane.isDisplay() && modalPane.getContent() instanceof ListView<?> lv){
            int selectedIndex = lv.getSelectionModel().getSelectedIndex();
            if (selectedIndex!=-1) {
                isProgrammaticChange = true;
                modalPane.hide();
                String selectedCommand=(String) lv.getItems().get(selectedIndex);
                textField.setText(selectedCommand.split("\\s+")[0]);
                textField.positionCaret(textField.getText().length());
                return;
            }
        }
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
            final List<String> strings = redisClient.getRedisConsole(this.currentDb).sendCommand(inputText);
            Platform.runLater(()->{
                if(inputText.trim().startsWith("select")&&!strings.isEmpty()&& "ok".equalsIgnoreCase(strings.getFirst())){
                    this.currentDb=Integer.parseInt(inputText.replace("select","").trim());
                    label.setText(redisContext.getRedisConfig().getName()+":"+this.currentDb+">");
                }
                if(strings.size()>MAXLINES){
                    List<String> list = strings.subList(strings.size() - MAXLINES, strings.size());
                    textArea.clear();
                    textArea.setText(String.join("\n", list));
                    return;
                }
                textArea.appendText( "\n");
                textArea.appendText( String.join("\n", strings) );
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
                    Platform.runLater(() -> textField.requestFocus());
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
        //监听光标丢失
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                modalPane.hide();
            }
        });
    }


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
        // 添加单击事件监听器
        commandListView.setOnMouseClicked(event -> {
            // 单击
            if (event.getClickCount() == 1) {
                String selectedCommand = commandListView.getSelectionModel().getSelectedItem();
                if (selectedCommand != null) {
                    // 提取命令名称（去掉参数部分）
                    String commandName = selectedCommand.split("\\s+")[0];
                    isProgrammaticChange = true;
                    textField.setText(commandName);
                    // 隐藏命令列表
                    modalPane.hide();
                    // 将光标移到末尾
                    textField.positionCaret(commandName.length());
                }
            }
        });
        if(!commands.isEmpty()){
            modalPane.show(commandListView);
            GuiUtil.adjustListViewHeight(commandListView, 300);
        }

    }
    // 添加历史命令浏览方法
    private void showPreviousCommand() {
        if(modalPane.isDisplay() && modalPane.getContent() instanceof ListView<?> lv){
            int selectedIndex = lv.getSelectionModel().getSelectedIndex();
            if (selectedIndex!=-1) {
                if(selectedIndex > 0){
                    lv.getSelectionModel().select(selectedIndex -1);
                    // 判断是否触顶：当选中的索引小于当前滚动行位置时，说明需要向上滚动
                    if(selectedIndex - 1 < scrollLine) {
                        // 向上滚动一行
                        scrollLine = selectedIndex - 1;
                        lv.scrollTo(scrollLine);
                    }
                }
                return;
            }
        }
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
        if(modalPane.isDisplay() && modalPane.getContent() instanceof ListView<?> lv){
            int selectedIndex = lv.getSelectionModel().getSelectedIndex();
            if (selectedIndex < lv.getItems().size() - 1) {
                int newIndex = selectedIndex + 1;
                lv.getSelectionModel().select(newIndex);
                // 触底判断：当可视区域最后一行的索引小于新选中索引时需要向下滚动
                // 最多显示7行(0-6)
                int lastVisibleIndex = scrollLine + 6;
                if(newIndex > lastVisibleIndex) {
                    scrollLine = newIndex - 6;
                    lv.scrollTo(scrollLine);
                }
            }
            return;
        }
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
























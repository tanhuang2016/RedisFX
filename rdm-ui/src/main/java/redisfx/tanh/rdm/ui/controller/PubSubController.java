package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import redisfx.tanh.rdm.redis.client.RedisSubscriber;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.entity.SubscribeTable;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class PubSubController extends BaseClientController<ServerTabController> implements Initializable {


    public StackPane webViewContainer;
    public TextField subChannel;
    public ToggleButton subscribe;
    public TextField pubChannel;
    public TextField pubMessage;
    public Button publish;
    public Label messageSize;
    public TableView<SubscribeTable> subscribeTable;
    public TableColumn<SubscribeTable,String> time;
    public TableColumn<SubscribeTable,String> channel;
    public TableColumn<SubscribeTable,String> message;
    private int count = 0;
    private static final int MAX_MESSAGES = 1000;

    private RedisSubscriber subscriber;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLanguage();
        initButton();
        initTable();
    }

    /**
     * 初始表
     */
    private void initTable() {
        subscribeTable.getStyleClass().addAll(Styles.STRIPED,Styles.DENSE);
        Platform.runLater(() -> {
            GuiUtil.initSimpleTableView(subscribeTable,new SubscribeTable());
            subscribeTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );
        });
    }

    @Override
    protected void initLanguage() {
        if (subscribe.isSelected()) {
            subscribe.setText(language("server.pubsub.unsubscribe"));
        } else {
            subscribe.setText(language("server.pubsub.subscribe"));
        }
        publish.setText(language("server.pubsub.publish"));
        time.setText(language("server.pubsub.time"));
        channel.setText(language("server.pubsub.channel"));
        message.setText(language("server.pubsub.message"));
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
     * 清空消息表格
     */
    private void clearMessages() {
        Platform.runLater(() -> {
            count = 0;
            subscribeTable.getItems().clear();
            messageSize.setText(String.valueOf(count));
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
            clearMessages();
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
     * 添加订阅消息到表格
     *
     * @param timestamp 时间戳
     * @param channel   频道
     * @param message   消息内容
     */
    public void addSubscriptionMessage(String timestamp, String channel, String message) {
        Platform.runLater(() -> {
            if(subscribeTable.getItems().size()>= MAX_MESSAGES){
                subscribeTable.getItems().removeFirst();
            }
            subscribeTable.getItems().add(new SubscribeTable(timestamp, channel, message));
            count++;
            messageSize.setText(String.valueOf(count));
        });
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

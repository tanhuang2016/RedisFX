package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyPageController;
import xyz.hashdog.rdm.ui.entity.ZsetTypeTable;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.Util;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 9:41
 */
public class ZsetTypeController extends BaseKeyPageController<ZsetTypeTable> implements Initializable {
    @FXML
    public BorderPane borderPane;
    @FXML
    public Button delRow;
    @FXML
    public Button add;
    @FXML
    public TextField score;
    /**
     * 最后一个选中的行对应的最新的value展示
     */
    private ByteArrayController byteArrayController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLanguage();
        bindData();
        initListener();
        initButton();
    }
    @Override
    protected void initLanguage() {
        super.initLanguage();
        findButton.setTooltip(GuiUtil.textTooltip(language("key.zset.find")));
        add.setText(language("key.zset.add"));
        delRow.setText(language("key.zset.delete"));
    }

    private void initButton() {
        initButtonStyles();
    }

    private void initButtonStyles() {
        add.getStyleClass().addAll(
                Styles.BUTTON_OUTLINED, Styles.ACCENT
        );
        delRow.getStyleClass().addAll(
                Styles.BUTTON_OUTLINED, Styles.DANGER
        );
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        tableViewListener();
        listListener();
    }


    /**
     * 缓存list数据监听
     */
    private void listListener() {
        this.list.addListener((ListChangeListener<ZsetTypeTable>) change -> {
            while (change.next()) {
                //删除到最后一个元素时,key也被删了,需要关闭tab
                if (change.wasRemoved() && this.list.isEmpty()) {
                    super.parentController.getParentController().removeTabByKeys(Collections.singletonList(parameter.get().getKey()));
                    super.parentController.getParentController().delKey(parameter);
                }
            }
        });
    }


    private void bindData() {
        total.textProperty().bind(Bindings.createStringBinding(() -> String.format(TOTAL, this.list.size()), this.list));
        size.textProperty().bind(Bindings.createStringBinding(() -> String.format(SIZE, Util.convertMemorySizeStr(this.list.stream().mapToLong(e -> e.getBytes().length ).sum(),2)), this.list));
    }

    /**
     * 表格监听
     */
    private void tableViewListener() {
        // 监听选中事件
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                delRow.setDisable(true);
                save.setDisable(true);
            }
            if (newValue != null) {
                delRow.setDisable(false);
                save.setDisable(false);
                this.lastSelect = newValue;
                Platform.runLater(() -> {
                    Tuple2<AnchorPane, ByteArrayController> valueTuple2 = loadByteArrayView(newValue.getBytes());
                    byteArrayController = valueTuple2.t2();
                    VBox vBox = (VBox) borderPane.getCenter();
                    VBox.setVgrow(valueTuple2.t1(), Priority.ALWAYS);
                    ObservableList<Node> children = vBox.getChildren();
                    children.set(children.size() - 1, valueTuple2.t1());
                    score.setText(String.valueOf(newValue.getScore()));

                });
            }
        });
    }


    /**
     * 初始化数据展示
     */
    @Override
    protected void initInfo() {
        ThreadPool.getInstance().execute(() -> {
            Long total = this.exeRedis(j -> j.zcard(this.parameter.get().getKey()));
            Map<Double, byte[]> map = this.exeRedis(j -> j.zrangeWithScores(this.parameter.get().getKey().getBytes(), 0L, total));
            List<ZsetTypeTable> newList = new ArrayList<>();
            map.forEach((k, v) -> newList.add(new ZsetTypeTable(k, v)));
            Platform.runLater(() -> {
                this.list.setAll(newList);
                GuiUtil.initSimpleTableView(tableView, new ZsetTypeTable());
                find(null);
                //设置默认选中第一行
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            });


        });

    }


    @Override
    protected Predicate<ZsetTypeTable> createNameFilter(String query) {
        String regex = query.replace("?", ".?").replace("*", ".*?");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return o -> pattern.matcher(o.getValue()).find();
    }

    /**
     * 保存值
     *
     * @param actionEvent 事件
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        //修改后的value
        byte[] value = byteArrayController.getByteArray();
        double score = Double.parseDouble(this.score.getText());
        int i = this.list.indexOf(lastSelect);
        async(() -> {
            //value发生变化的情况,需要先删后增
            if (!Arrays.equals(value, lastSelect.getBytes())) {
                exeRedis(j -> j.zrem(this.getParameter().getKey().getBytes(), lastSelect.getBytes()));
                lastSelect.setBytes(value);
            }
            exeRedis(j -> j.zadd(this.getParameter().getKey().getBytes(), score, value));
            lastSelect.setScore(score);
            Platform.runLater(() -> {
                //实际上list存的引用,lastSelect修改,list中的元素也会修改,重新set进去是为了触发更新事件
                this.list.set(i, lastSelect);
                tableView.refresh();
                byteArrayController.setByteArray(value);
                GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_SAVE_SUCCESS));
            });
        });
    }

    /**
     * 新增
     *
     * @param actionEvent 事件
     */
    @FXML
    public void add(ActionEvent actionEvent) {
        Button source = (Button) actionEvent.getSource();
        Tuple2<AnchorPane, ByteArrayController> tuple2 = loadByteArrayView("".getBytes());
        VBox vBox = new VBox();
        VBox.setVgrow(tuple2.t1(), Priority.ALWAYS);
        ObservableList<Node> children = vBox.getChildren();
        HBox hBox = createLabelHbox("Score");
        children.add(hBox);
        TextField score = new TextField();
        children.addAll(score, tuple2.t1());
        Tuple2<AnchorPane, AppendController> appendTuple2 = loadFxml("/fxml/AppendView.fxml");
        Stage stage = GuiUtil.createSubStage(source.getText(), appendTuple2.t1(), root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(vBox);
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            double v = Double.parseDouble(score.getText());
            byte[] byteArray = tuple2.t2().getByteArray();
            async(() -> {
                exeRedis(j -> j.zadd(this.parameter.get().getKey().getBytes(), v, byteArray));
                Platform.runLater(() -> {
                    list.add(new ZsetTypeTable(v, byteArray));
                    find(null);
                    stage.close();
                });
            });
        });
    }

    /**
     * 删除行
     *
     * @param actionEvent 事件
     */
    public void delRow(ActionEvent actionEvent) {
        if (GuiUtil.alertRemove()) {
            return;
        }
        async(() -> {
            exeRedis(j -> j.zrem(this.getParameter().getKey().getBytes(), lastSelect.getBytes()));
            GuiUtil.remove2TableView(this.list, this.tableView, lastSelect);
        });
    }

    @Override
    public void reloadInfo() {
        initInfo();
    }

}

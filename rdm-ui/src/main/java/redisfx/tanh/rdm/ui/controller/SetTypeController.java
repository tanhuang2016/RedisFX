package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import redisfx.tanh.rdm.common.pool.ThreadPool;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.ui.controller.base.BaseKeyPageController;
import redisfx.tanh.rdm.ui.entity.SetTypeTable;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import redisfx.tanh.rdm.ui.util.Util;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static redisfx.tanh.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 9:52
 */
public class SetTypeController extends BaseKeyPageController<SetTypeTable> implements Initializable {
    @FXML
    public BorderPane borderPane;
    @FXML
    public Button add;
    @FXML
    public Button delRow;
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
        findButton.setTooltip(GuiUtil.textTooltip(language("key.set.find")));
        add.setText(language("key.set.add"));
        delRow.setText(language("key.set.delete"));
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




    private void bindData() {
        total.textProperty().bind(Bindings.createStringBinding(() -> String.format(TOTAL,this.list.size()), this.list));
        size.textProperty().bind(Bindings.createStringBinding(() -> String.format(SIZE, Util.convertMemorySizeStr(this.list.stream().mapToLong(e -> e.getBytes().length).sum(),2)), this.list));
    }

    /**
     * 缓存list数据监听
     */
    private void listListener() {
        this.list.addListener((ListChangeListener<SetTypeTable>) change -> {
            while (change.next()) {
                //删除到最后一个元素时,key也被删了,需要关闭tab
                if (change.wasRemoved() && this.list.isEmpty()) {
                    super.parentController.getParentController().removeTabByKeys(Collections.singletonList(parameter.get().getKey()));
                    super.parentController.getParentController().delKey(parameter);
                }
            }
        });
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
                    Tuple2<AnchorPane, ByteArrayController> tuple2 = loadFxml("/fxml/ByteArrayView.fxml");
                    AnchorPane anchorPane = tuple2.t1();
                    byteArrayController = tuple2.t2();
                    byteArrayController.setByteArray(newValue.getBytes());
                    borderPane.setCenter(anchorPane);
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
            List<byte[]> bytes = this.exeRedis(j -> j.sscanAll(this.parameter.get().getKey().getBytes()));
            List<SetTypeTable> newList = new ArrayList<>();
            for (byte[] aByte : bytes) {
                newList.add(new SetTypeTable(aByte));
            }
            Platform.runLater(() -> {
                this.list.setAll(newList);
                GuiUtil.initSimpleTableView(tableView,new SetTypeTable());
                find(null);
                //设置默认选中第一行
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            });


        });

    }

    @Override
    protected Predicate<SetTypeTable> createNameFilter(String query) {
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
        byte[] byteArray = byteArrayController.getByteArray();
        int i = this.list.indexOf(lastSelect);
        async(() -> {
            exeRedis(j -> j.srem(this.getParameter().getKey().getBytes(),  lastSelect.getBytes()));
            exeRedis(j -> j.sadd(this.getParameter().getKey().getBytes(),  byteArray));
            lastSelect.setBytes(byteArray);
            Platform.runLater(() -> {
                //实际上list存的引用,lastSelect修改,list中的元素也会修改,重新set进去是为了触发更新事件
                this.list.set(i,lastSelect);
                tableView.refresh();
                byteArrayController.setByteArray(byteArray);
                GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_SAVE_SUCCESS));
            });
        });
    }






    /**
     * 删除选中行
     *
     * @param actionEvent  事件
     */
    @FXML
    public void delRow(ActionEvent actionEvent) {
        if (GuiUtil.alertRemoveRow()) {
            return;
        }
        async(() -> {
            exeRedis(j -> j.srem(this.getParameter().getKey().getBytes(), lastSelect.getBytes()));
            GuiUtil.remove2TableView(this.list,this.tableView,lastSelect);
        });
    }


    @FXML
    public void add(ActionEvent actionEvent) {
        Button source = (Button)actionEvent.getSource();
        Tuple2<AnchorPane, ByteArrayController> tuple2 = loadByteArrayView( "".getBytes());
        Tuple2<AnchorPane, AppendController> appendTuple2= loadFxml("/fxml/AppendView.fxml");
        Stage stage= GuiUtil.createSubStage(source.getText(),appendTuple2.t1(),root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(tuple2.t1());
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            byte[] byteArray = tuple2.t2().getByteArray();
            async(()->{
                exeRedis(j->j.sadd(this.parameter.get().getKey().getBytes(),byteArray));
                Platform.runLater(()->{
                    list.add(new SetTypeTable(byteArray));
                    find(null);
                    stage.close();
                });
            });
        });
    }
    @Override
    public void reloadInfo() {
        initInfo();
    }
}

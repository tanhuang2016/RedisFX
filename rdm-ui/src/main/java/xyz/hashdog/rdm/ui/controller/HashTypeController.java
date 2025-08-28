package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyController;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyPageController;
import xyz.hashdog.rdm.ui.entity.HashTypeTable;
import xyz.hashdog.rdm.ui.entity.ITable;
import xyz.hashdog.rdm.ui.entity.TopKeyTable;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 9:41
 */
public class HashTypeController extends BaseKeyPageController<HashTypeTable> implements Initializable {

    @FXML
    public BorderPane borderPane;
    @FXML
    public Button delRow;
    @FXML
    public Button add;

    /**
     * 最后一个选中的行对应的最新的value展示
     */
    private ByteArrayController byteArrayController;
    private ByteArrayController keyByteArrayController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindData();
        initListener();
        initButton();

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
        this.list.addListener((ListChangeListener<ITable>) change -> {
            while (change.next()) {
                //删除到最后一个元素时,key也被删了,需要关闭tab
                if (change.wasRemoved() && this.list.isEmpty()) {
                    super.parentController.parentController.removeTabByKeys(Collections.singletonList(parameter.get().getKey()));
                    super.parentController.parentController.delKey(parameter);
                }
            }
        });
    }





    private void bindData() {
        total.textProperty().bind(Bindings.createStringBinding(() -> String.format(TOTAL, this.list.size()), this.list));
        size.textProperty().bind(Bindings.createStringBinding(() -> String.format(SIZE, this.list.stream().mapToLong(e -> e.getBytes().length + e.getKeyBytes().length).sum()), this.list));
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
            if (newValue instanceof HashTypeTable newTable) {
                delRow.setDisable(false);
                save.setDisable(false);
                this.lastSelect = newTable;
                Platform.runLater(() -> {
                    Tuple2<AnchorPane, ByteArrayController> keyTuple2 = GuiUtil.loadByteArrayView(newTable.getKeyBytes(), this);
                    Tuple2<AnchorPane, ByteArrayController> valueTuple2 = GuiUtil.loadByteArrayView(newTable.getBytes(), this);
                    byteArrayController = valueTuple2.t2();
                    keyByteArrayController = keyTuple2.t2();
                    keyByteArrayController.setName("Key");
                    VBox vBox = (VBox) borderPane.getCenter();
                    vBox.getChildren().clear();
                    vBox.getChildren().add(keyTuple2.t1());
                    VBox.setVgrow(valueTuple2.t1(), Priority.ALWAYS);
                    vBox.getChildren().add(valueTuple2.t1());
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
            Map<byte[], byte[]> map = this.exeRedis(j -> j.hscanAll(this.parameter.get().getKey().getBytes()));
            List<HashTypeTable> newList = new ArrayList<>();
            map.forEach((k, v) -> newList.add(new HashTypeTable(k, v)));
            Platform.runLater(() -> {
                this.list.setAll(newList);
                GuiUtil.initSimpleTableView(tableView,new HashTypeTable());
                find(null);
                //设置默认选中第一行
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            });


        });

    }




    @Override
    protected Predicate<HashTypeTable> createNameFilter(String query) {
        String regex = query.replace("?", ".?").replace("*", ".*?");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return o -> pattern.matcher(o.getKey()).find();
    }

    /**
     * 保存值
     *
     * @param actionEvent
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        //修改后的key
        byte[] key = keyByteArrayController.getByteArray();
        byte[] value = byteArrayController.getByteArray();
        int i = this.list.indexOf(lastSelect);
        async(() -> {
            //key发生变化的情况,需要set新键值对,切删除老键值对
            if (!Arrays.equals(key,lastSelect.getKeyBytes())) {
                exeRedis(j -> j.hdel(this.getParameter().getKey().getBytes(), lastSelect.getKeyBytes()));
                lastSelect.setKeyBytes(key);
            }
            exeRedis(j -> j.hset(this.getParameter().getKey().getBytes(), key, value));
            lastSelect.setBytes(value);
            Platform.runLater(() -> {
                //实际上list存的引用,lastSelect修改,list中的元素也会修改,重新set进去是为了触发更新事件
                this.list.set(i,lastSelect);
                tableView.refresh();
                keyByteArrayController.setByteArray(key);
                byteArrayController.setByteArray(value);
                GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_SAVE_SUCCESS));
            });
        });
    }

    /**
     * 新增
     *
     * @param actionEvent
     */
    @FXML
    public void add(ActionEvent actionEvent) {
        Button source = (Button)actionEvent.getSource();
        Tuple2<AnchorPane, ByteArrayController> keyTuple2 = GuiUtil.loadByteArrayView("".getBytes(),this);
        Tuple2<AnchorPane, ByteArrayController> valueTuple2 = GuiUtil.loadByteArrayView("".getBytes(),this);
        keyTuple2.t2().setName("Key");
        VBox vBox = new VBox();
        vBox.getChildren().add(keyTuple2.t1());
        VBox.setVgrow(valueTuple2.t1(), Priority.ALWAYS);
        vBox.getChildren().add(valueTuple2.t1());
        Tuple2<AnchorPane, AppendController> appendTuple2= loadFxml("/fxml/AppendView.fxml");
        Stage stage= GuiUtil.createSubStage(source.getText(),appendTuple2.t1(),root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(vBox);
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            byte[] key = keyTuple2.t2().getByteArray();
            byte[] value = valueTuple2.t2().getByteArray();
            async(()->{
                exeRedis(j->j.hset(this.getParameter().getKey().getBytes(),key, value));
                Platform.runLater(()->{
                    //需要判断list是否存在该元素,由于是hash类型,只判断key是否存在就行,需要重新equals方法
                    HashTypeTable hashTypeTable = new HashTypeTable(key, value);
                    if(list.contains(hashTypeTable)){
                        int i = list.indexOf(hashTypeTable);
                        list.set(i,hashTypeTable);
                    }else{
                        list.add(hashTypeTable);
                    }
                    find(null);
                    stage.close();
                });
            });
        });
    }


    /**
     * 删除行
     *
     * @param actionEvent
     */
    public void delRow(ActionEvent actionEvent) {
        if (GuiUtil.alertRemove()) {
            return;
        }
        async(() -> {
            exeRedis(j -> j.hdel(this.getParameter().getKey().getBytes(), lastSelect.getKeyBytes()));
            GuiUtil.remove2TableView(this.list,this.tableView,lastSelect);
        });
    }


    @Override
    public void reloadInfo() {
        initInfo();
    }
}

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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.common.ValueTypeEnum;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyController;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyPageController;
import xyz.hashdog.rdm.ui.entity.HashTypeTable;
import xyz.hashdog.rdm.ui.entity.ListTypeTable;
import xyz.hashdog.rdm.ui.entity.StreamTypeTable;
import xyz.hashdog.rdm.ui.entity.ZsetTypeTable;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author th
 * @version 2.0.0
 * @since 2025/7/15 22:41
 */
public class StreamTypeController extends BaseKeyPageController<StreamTypeTable> implements Initializable {
    @FXML
    public BorderPane borderPane;

    @FXML
    public Button delRow;
    @FXML
    public Button add;
    @FXML
    public TextField id;
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
        this.list.addListener((ListChangeListener<StreamTypeTable>) change -> {
            while (change.next()) {
                //删除到最后一个元素时,key也被删了,需要关闭tab
                if (change.wasRemoved() && this.list.size() == 0) {
                    super.parentController.parentController.removeTabByKeys(Arrays.asList(parameter.get().getKey()));
                    super.parentController.parentController.delKey(parameter);
                }
            }
        });
    }





    private void bindData() {
        total.textProperty().bind(Bindings.createStringBinding(() -> String.format(TOTAL, this.list.size()), this.list));
        size.textProperty().bind(Bindings.createStringBinding(() -> String.format(SIZE, this.list.stream().mapToLong(e -> e.getBytes().length ).sum()), this.list));
    }

    /**
     * 表格监听
     */
    private void tableViewListener() {
        // 监听选中事件
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                delRow.setDisable(true);
            }
            if (newValue != null) {
                delRow.setDisable(false);
                this.lastSelect = newValue;
                Platform.runLater(() -> {
                    Tuple2<AnchorPane, ByteArrayController> valueTuple2 = GuiUtil.loadByteArrayView(newValue.getBytes(),this);
                    byteArrayController = valueTuple2.t2();
                    byteArrayController.setByteArray(newValue.getBytes(),ValueTypeEnum.JSON);
                    VBox vBox = (VBox) borderPane.getCenter();
                    VBox.setVgrow(valueTuple2.t1(), Priority.ALWAYS);
                    ObservableList<Node> children = vBox.getChildren();
                    children.set(children.size()-1,valueTuple2.t1());
                    id.setText(String.valueOf(newValue.getId()));

                });
            }
        });
    }



    /**
     * 初始化数据展示
     */
    protected void initInfo() {
        ThreadPool.getInstance().execute(() -> {
            long total = this.exeRedis(j -> j.xlen(this.parameter.get().getKey()));
            Map<String, String> map = this.exeRedis(j -> j.xrevrange(this.parameter.get().getKey(),"+","-", (int)total));
            List<StreamTypeTable> newList = new ArrayList<>();
            map.forEach((k, v) -> newList.add(new StreamTypeTable(k, v)));
            Platform.runLater(() -> {
                this.list.setAll(newList);
                GuiUtil.initSimpleTableView(tableView,new StreamTypeTable());
                find(null);
                //设置默认选中第一行
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            });


        });

    }




    @Override
    protected Predicate<StreamTypeTable> createNameFilter(String query) {
        String regex = query.replace("?", ".?").replace("*", ".*?");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return o -> pattern.matcher(o.getValue()).find();
    }



    /**
     * 新增
     *
     * @param actionEvent
     */
    @FXML
    public void add(ActionEvent actionEvent) {
        Button source = (Button)actionEvent.getSource();
        Tuple2<AnchorPane, ByteArrayController> tuple2 = GuiUtil.loadByteArrayView( "".getBytes(),this);
        tuple2.t2().setByteArray("".getBytes(),ValueTypeEnum.JSON);
        VBox vBox = new VBox();
        VBox.setVgrow(tuple2.t1(), Priority.ALWAYS);
        ObservableList<Node> children = vBox.getChildren();
        Label label = new Label("ID");
        label.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(label);
        HBox.setHgrow(label,Priority.ALWAYS);
        hBox.setPrefHeight(40);
        hBox.setMaxHeight(hBox.getPrefHeight());
        hBox.setMinHeight(hBox.getPrefHeight());
        hBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setAlignment(vBox,Pos.CENTER);
        children.add(hBox);
        TextField id = new TextField();
        id.setText("*");
        children.add(id);
        children.add(tuple2.t1());
        VBox.setVgrow(hBox,Priority.ALWAYS);
        Tuple2<AnchorPane, AppendController> appendTuple2= loadFxml("/fxml/AppendView.fxml");
        Stage stage= GuiUtil.createSubStage(source.getText(),appendTuple2.t1(),root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(vBox);
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            String v = id.getText();
            byte[] byteArray = tuple2.t2().getByteArray();
            async(()->{
                String idstr = exeRedis(j -> j.xadd(this.parameter.get().getKey(), v, new String(byteArray)));
                Platform.runLater(()->{
                    list.add(new StreamTypeTable(idstr,new String(byteArray)));
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
            exeRedis(j -> j.xdel(this.getParameter().getKey(), lastSelect.getId()));
            GuiUtil.remove2TableView(this.list,this.tableView,lastSelect);
        });
    }

    @Override
    public void reloadInfo() {
        initInfo();
    }
}

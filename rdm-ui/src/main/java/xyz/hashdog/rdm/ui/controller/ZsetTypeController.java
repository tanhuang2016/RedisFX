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
import xyz.hashdog.rdm.ui.entity.ZsetTypeTable;
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
public class ZsetTypeController extends BaseKeyContentController implements Initializable {
    private static final int ROWS_PER_PAGE = 32;
    @FXML
    public TableView<ZsetTypeTable> tableView;
    @FXML
    public BorderPane borderPane;
    @FXML
    public Label total;
    @FXML
    public Label size;
    protected static final String SIZE = "Size:%dB";
    protected static final String TOTAL = "Total:%d";
    @FXML
    public Button findButton;
    @FXML
    public CustomTextField findTextField;
    @FXML
    public Button save;
    @FXML
    public Button delRow;
    @FXML
    public Button add;
    @FXML
    public Pagination pagination;
    @FXML
    public TextField score;
    /**
     * 缓存所有表格数据
     */
    private ObservableList<ZsetTypeTable> list = FXCollections.observableArrayList();
    /**
     * 查询后的表格数据
     */
    private ObservableList<ZsetTypeTable> findList = FXCollections.observableArrayList();
    /**
     * 最后选中的行缓存
     */
    private ZsetTypeTable lastSelect;
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
        initTextField();
    }

    private void initButton() {
        initButtonStyles();
        initButtonIcon();
    }
    private void initTextField() {
        findTextField.setRight(findButton);
    }
    private void initButtonStyles() {
        save.getStyleClass().add(Styles.ACCENT);
        findButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,Styles.ROUNDED,Styles.SMALL);
        findButton.setCursor(Cursor.HAND);
        add.getStyleClass().addAll(
                Styles.BUTTON_OUTLINED, Styles.ACCENT
        );
        delRow.getStyleClass().addAll(
                Styles.BUTTON_OUTLINED, Styles.DANGER
        );
    }
    private void initButtonIcon() {
        findButton.setGraphic(new FontIcon(Feather.SEARCH));
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        userDataPropertyListener();
        tableViewListener();
        listListener();
        paginationListener();
    }

    /**
     * 分页监听
     * 数据显示,全靠分页监听
     */
    private void paginationListener() {
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int pageIndex = (int) newIndex;
            setCurrentPageIndex(pageIndex);

        });
    }

    /**
     * 可以手动触发分页
     *
     * @param pageIndex
     */
    private void setCurrentPageIndex(int pageIndex) {
        if (pageIndex < pagination.getPageCount() - 1) {
            List<ZsetTypeTable> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, (pageIndex + 1) * ROWS_PER_PAGE + 1);
            tableView.setItems(FXCollections.observableArrayList(pageList));
        } else {
            List<ZsetTypeTable> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, findList.size());
            tableView.setItems(FXCollections.observableArrayList(pageList));
        }

        tableView.refresh();

    }

    /**
     * 缓存list数据监听
     */
    private void listListener() {
        this.list.addListener((ListChangeListener<ZsetTypeTable>) change -> {
            while (change.next()) {
                //删除到最后一个元素时,key也被删了,需要关闭tab
                if (change.wasRemoved() && this.list.size() == 0) {
                    super.parentController.parentController.removeTabByKeys(Arrays.asList(parameter.get().getKey()));
                    super.parentController.parentController.delKey(parameter);
                }
            }
        });
    }

    /**
     * 父层传送的数据监听
     * 监听到key的传递
     */
    private void userDataPropertyListener() {
        super.parameter.addListener((observable, oldValue, newValue) -> {
            initInfo();
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
                save.setDisable(true);
            }
            if (newValue != null) {
                delRow.setDisable(false);
                save.setDisable(false);
                this.lastSelect = newValue;
                Platform.runLater(() -> {
                    Tuple2<AnchorPane, ByteArrayController> valueTuple2 = GuiUtil.loadByteArrayView(newValue.getBytes(),this);
                    byteArrayController = valueTuple2.t2();
                    VBox vBox = (VBox) borderPane.getCenter();
                    VBox.setVgrow(valueTuple2.t1(), Priority.ALWAYS);
                    ObservableList<Node> children = vBox.getChildren();
                    children.set(children.size()-1,valueTuple2.t1());
                    score.setText(String.valueOf(newValue.getScore()));

                });
            }
        });
    }



    /**
     * 初始化数据展示
     */
    private void initInfo() {
        ThreadPool.getInstance().execute(() -> {
            Long total = this.exeRedis(j -> j.zcard(this.parameter.get().getKey()));
            Map<Double, byte[]> map = this.exeRedis(j -> j.zrangeWithScores(this.parameter.get().getKey().getBytes(), 0L, total));
            List<ZsetTypeTable> newList = new ArrayList<>();
            map.forEach((k, v) -> newList.add(new ZsetTypeTable(k, v)));
            Platform.runLater(() -> {
                this.list.setAll(newList);
                ObservableList<TableColumn<ZsetTypeTable, ?>> columns = tableView.getColumns();
                TableColumn<ZsetTypeTable, Integer> c0 = (TableColumn) columns.get(0);
                c0.setCellValueFactory(
                        param -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(param.getValue()) + 1)
                );
                for (int i = 1; i < columns.size(); i++) {
                    TableColumn c1 = (TableColumn) columns.get(i);
                    c1.setCellValueFactory(
                            new PropertyValueFactory<ZsetTypeTable, String>(ZsetTypeTable.getProperties()[i])
                    );
                    c1.setCellFactory(param -> new GuiUtil.OneLineTableCell<>());
                }
                find(null);
                //设置默认选中第一行
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            });


        });

    }

    /**
     * 列表查询
     *
     * @param actionEvent
     */
    public void find(ActionEvent actionEvent) {
        String text = this.findTextField.getText();
        List<ZsetTypeTable> newList;
        if (DataUtil.isBlank(text)) {
            text = "*";
        }
        Predicate<ZsetTypeTable> nameFilter = createNameFilter(text);
        newList = this.list.stream().filter(nameFilter).collect(Collectors.toList());
        findList.clear();
        findList.addAll(newList);
        pagination.setPageCount((int) Math.ceil((double) findList.size() / ROWS_PER_PAGE));
        //当前页就是0页才需要手动触发,否则原事件触发不了
        if (pagination.getCurrentPageIndex() == 0) {
            this.setCurrentPageIndex(0);
        }
    }


    private Predicate<ZsetTypeTable> createNameFilter(String query) {
        String regex = query.replace("?", ".?").replace("*", ".*?");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return o -> pattern.matcher(o.getValue()).find();
    }

    /**
     * 保存值
     *
     * @param actionEvent
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        //修改后的value
        byte[] value = byteArrayController.getByteArray();
        Double score = Double.valueOf(this.score.getText());
        int i = this.list.indexOf(lastSelect);
        asynexec(() -> {
            //value发生变化的情况,需要先删后增
            if (!Arrays.equals(value,lastSelect.getBytes())) {
                exeRedis(j -> j.zrem(this.getParameter().getKey().getBytes(), lastSelect.getBytes()));
                lastSelect.setBytes(value);
            }
            exeRedis(j -> j.zadd(this.getParameter().getKey().getBytes(), score, value));
            lastSelect.setScore(score);
            Platform.runLater(() -> {
                //实际上list存的引用,lastSelect修改,list中的元素也会修改,重新set进去是为了触发更新事件
                this.list.set(i,lastSelect);
                tableView.refresh();
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
        Tuple2<AnchorPane, ByteArrayController> tuple2 = GuiUtil.loadByteArrayView( "".getBytes(),this);
        VBox vBox = new VBox();
        VBox.setVgrow(tuple2.t1(), Priority.ALWAYS);
        ObservableList<Node> children = vBox.getChildren();
        Label label = new Label("Score");
        label.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(label);
        HBox.setHgrow(label,Priority.ALWAYS);
        hBox.setPrefHeight(40);
        hBox.setMaxHeight(hBox.getPrefHeight());
        hBox.setMinHeight(hBox.getPrefHeight());
        hBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setAlignment(vBox,Pos.CENTER);
        children.add(hBox);
        TextField score = new TextField();
        children.add(score);
        children.add(tuple2.t1());
        VBox.setVgrow(hBox,Priority.ALWAYS);
        Tuple2<AnchorPane, AppendController> appendTuple2=loadFXML("/fxml/AppendView.fxml");
        Stage stage= GuiUtil.createSubStage(source.getText(),appendTuple2.t1(),root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(vBox);
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            double v = Double.parseDouble(score.getText());
            byte[] byteArray = tuple2.t2().getByteArray();
            asynexec(()->{
                exeRedis(j->j.zadd(this.parameter.get().getKey().getBytes(),v,byteArray));
                Platform.runLater(()->{
                    list.add(new ZsetTypeTable(v,byteArray));
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
        if (!GuiUtil.alertRemove()) {
            return;
        }
        asynexec(() -> {
            exeRedis(j -> j.zrem(this.getParameter().getKey().getBytes(), lastSelect.getBytes()));
            GuiUtil.remove2UI(this.list,this.tableView,lastSelect);
        });
    }
    @Override
    public void reloadInfo() {
        initInfo();
    }

}

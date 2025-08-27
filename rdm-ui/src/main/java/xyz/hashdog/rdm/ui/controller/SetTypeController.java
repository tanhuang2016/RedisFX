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
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.entity.SetTypeTable;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 9:52
 */
public class SetTypeController extends BaseKeyContentController implements Initializable {
    private static final int ROWS_PER_PAGE = 32;
    @FXML
    public TableView<SetTypeTable> tableView;
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
    public Button add;
    @FXML
    public Button delRow;
    @FXML
    public Pagination pagination;
    /**
     * 缓存所有表格数据
     */
    private ObservableList<SetTypeTable> list = FXCollections.observableArrayList();
    /**
     * 查询后的表格数据
     */
    private ObservableList<SetTypeTable> findList = FXCollections.observableArrayList();
    /**
     * 最后选中的行缓存
     */
    private SetTypeTable lastSelect;
    /**
     * 最后一个选中的行对应的最新的value展示
     */
    private ByteArrayController byteArrayController;


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
            int pageIndex=(int)newIndex;
            setCurrentPageIndex(pageIndex);

        });
    }

    /**
     * 可以手动触发分页
     * @param pageIndex
     */
    private void setCurrentPageIndex(int pageIndex) {
        if(pageIndex<pagination.getPageCount()-1){
            List<SetTypeTable> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, (pageIndex + 1) * ROWS_PER_PAGE+1);
            tableView.setItems(FXCollections.observableArrayList(pageList));
        }else{
            List<SetTypeTable> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, findList.size());
            tableView.setItems(FXCollections.observableArrayList(pageList));
        }

        tableView.refresh();

    }

    private void bindData() {
        total.textProperty().bind(Bindings.createStringBinding(() -> String.format(TOTAL,this.list.size()), this.list));
        size.textProperty().bind(Bindings.createStringBinding(() -> String.format(SIZE,this.list.stream().mapToLong(e -> e.getBytes().length).sum()), this.list));
    }

    /**
     * 缓存list数据监听
     */
    private void listListener() {
        this.list.addListener((ListChangeListener<SetTypeTable>) change -> {
            while (change.next()) {
                //删除到最后一个元素时,key也被删了,需要关闭tab
                if (change.wasRemoved() && this.list.size()==0) {
                    super.parentController.parentController.removeTabByKeys(Arrays.asList(parameter.get().getKey()));
                    super.parentController.parentController.delKey(parameter);
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
                    Tuple2<AnchorPane, ByteArrayController> tuple2 = loadFXML("/fxml/ByteArrayView.fxml");
                    AnchorPane anchorPane = tuple2.t1();
                    byteArrayController = tuple2.t2();
                    byteArrayController.setParentController(this);
                    byteArrayController.setByteArray(newValue.getBytes());
                    borderPane.setCenter(anchorPane);
                });
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

    /**
     * 初始化数据展示
     */
    private void initInfo() {
        ThreadPool.getInstance().execute(() -> {
            List<byte[]> bytes = this.exeRedis(j -> j.sscanAll(this.parameter.get().getKey().getBytes()));
            List<SetTypeTable> newList = new ArrayList<>();
            for (int i = 0; i < bytes.size(); i++) {
                newList.add(new SetTypeTable(bytes.get(i)));
            }
            Platform.runLater(() -> {
                this.list.setAll(newList);
                ObservableList<TableColumn<SetTypeTable, ?>> columns = tableView.getColumns();
                TableColumn<SetTypeTable, Integer> c0 = (TableColumn) columns.get(0);
                c0.setCellValueFactory(
                        param -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(param.getValue()) + 1)
                );
                for (int i = 1; i < columns.size(); i++) {
                    TableColumn c1 = (TableColumn) columns.get(i);
                    c1.setCellValueFactory(
                            new PropertyValueFactory<SetTypeTable, String>(SetTypeTable.getProperties()[i])
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

    private Predicate<SetTypeTable> createNameFilter(String query) {
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
        byte[] byteArray = byteArrayController.getByteArray();
        int i = this.list.indexOf(lastSelect);
        asynexec(() -> {
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
     * 列表查询
     *
     * @param actionEvent
     */
    public void find(ActionEvent actionEvent) {
        String text = this.findTextField.getText();
        List<SetTypeTable> newList;
        if (DataUtil.isBlank(text)) {
            text = "*";
        }
        Predicate<SetTypeTable> nameFilter = createNameFilter(text);
        newList = this.list.stream().filter(nameFilter).collect(Collectors.toList());
        findList.clear();
        findList.addAll(newList);
        pagination.setPageCount((int) Math.ceil((double) findList.size() / ROWS_PER_PAGE));
        //当前页就是0页才需要手动触发,否则原事件触发不了
        if(pagination.getCurrentPageIndex()==0){
            this.setCurrentPageIndex(0);
        }
    }





    /**
     * 删除选中行
     *
     * @param actionEvent
     */
    @FXML
    public void delRow(ActionEvent actionEvent) {
        if (GuiUtil.alertRemove()) {
            return;
        }
        asynexec(() -> {
            exeRedis(j -> j.srem(this.getParameter().getKey().getBytes(), lastSelect.getBytes()));
            GuiUtil.remove2UI(this.list,this.tableView,lastSelect);
        });
    }


    @FXML
    public void add(ActionEvent actionEvent) {
        Button source = (Button)actionEvent.getSource();
        Tuple2<AnchorPane, ByteArrayController> tuple2 = GuiUtil.loadByteArrayView( "".getBytes(),this);
        Tuple2<AnchorPane, AppendController> appendTuple2=loadFXML("/fxml/AppendView.fxml");
        Stage stage= GuiUtil.createSubStage(source.getText(),appendTuple2.t1(),root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(tuple2.t1());
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            byte[] byteArray = tuple2.t2().getByteArray();
            asynexec(()->{
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

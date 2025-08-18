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
import xyz.hashdog.rdm.ui.entity.ListTypeTable;
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
public class ListTypeController extends BaseKeyContentController implements Initializable {
    private static final int ROWS_PER_PAGE = 32;
    @FXML
    public TableView<ListTypeTable> tableView;
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
    public MenuItem addHead;
    @FXML
    public MenuItem addTail;
    @FXML
    public MenuItem delHead;
    @FXML
    public MenuItem delTail;
    @FXML
    public MenuItem delRow;
    @FXML
    public Pagination pagination;
    public SplitMenuButton add;
    public SplitMenuButton del;
    /**
     * 缓存所有表格数据
     */
    private ObservableList<ListTypeTable> list = FXCollections.observableArrayList();
    /**
     * 查询后的表格数据
     */
    private ObservableList<ListTypeTable> findList = FXCollections.observableArrayList();
    /**
     * 最后选中的行缓存
     */
    private ListTypeTable lastSelect;
    /**
     * 最后一个选中的行对应的最新的value展示
     */
    private ByteArrayController byteArrayController;

    private final static byte[] DEL_MARK = "del_mark".getBytes();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindData();
        initListener();
//        initPagination();
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
        del.getStyleClass().addAll(
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
            List<ListTypeTable> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, (pageIndex + 1) * ROWS_PER_PAGE+1);
            tableView.setItems(FXCollections.observableArrayList(pageList));
        }else{
            List<ListTypeTable> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, findList.size());
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
        this.list.addListener((ListChangeListener<ListTypeTable>) change -> {
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
                    Tuple2<AnchorPane, ByteArrayController> tuple2 = GuiUtil.loadByteArrayView(newValue.getBytes(),this);

                    AnchorPane anchorPane = tuple2.t1();
                    byteArrayController = tuple2.t2();
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
            long total = this.exeRedis(j -> j.llen(this.parameter.get().getKey()));
            List<byte[]> bytes = this.exeRedis(j -> j.lrange(this.parameter.get().getKey().getBytes(), 0, (int) total));
            List<ListTypeTable> newList = new ArrayList<>();
            for (int i = 0; i < bytes.size(); i++) {
                newList.add(new ListTypeTable(bytes.get(i)));
            }

            Platform.runLater(() -> {
                this.list.setAll(newList);
                ObservableList<TableColumn<ListTypeTable, ?>> columns = tableView.getColumns();
                TableColumn<ListTypeTable, Integer> c0 = (TableColumn) columns.get(0);
                c0.setCellValueFactory(
                        param -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(param.getValue()) + 1)
                );
                for (int i = 1; i < columns.size(); i++) {
                    TableColumn c1 = (TableColumn) columns.get(i);
                    c1.setCellValueFactory(
                            new PropertyValueFactory<ListTypeTable, String>(ListTypeTable.getProperties()[i])
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

    private Predicate<ListTypeTable> createNameFilter(String query) {
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
            exeRedis(j -> j.lset(this.getParameter().getKey().getBytes(), this.list.indexOf(lastSelect), byteArray));
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
        List<ListTypeTable> newList;
        if (DataUtil.isBlank(text)) {
            text = "*";
        }
        Predicate<ListTypeTable> nameFilter = createNameFilter(text);
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
     * 插入头
     * @param actionEvent
     */
    @FXML
    public void addHead(ActionEvent actionEvent) {
        this.add.setOnAction(this::addHead);
        this.add.setText(this.addHead.getText());
        this.add(actionEvent,0);
    }


    /**
     * 封装插入方法
     * @param actionEvent
     * @param index 下标
     */
    private void add(ActionEvent actionEvent, int index) {
        Object source1 = actionEvent.getSource();
        String text="";
        if(source1 instanceof MenuItem){
            MenuItem source = (MenuItem)actionEvent.getSource();
            text=source.getText();
        }else if(source1 instanceof MenuButton){
            MenuButton source = (MenuButton)actionEvent.getSource();
            text=source.getText();
        }
        Tuple2<AnchorPane, ByteArrayController> tuple2 =  GuiUtil.loadByteArrayView("".getBytes(),this);
        Tuple2<AnchorPane, AppendController> appendTuple2=loadFXML("/fxml/AppendView.fxml");
        Stage stage= GuiUtil.createSubStage(text,appendTuple2.t1(),root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(tuple2.t1());
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            byte[] byteArray = tuple2.t2().getByteArray();
            asynexec(()->{
                if(index==0){
                    exeRedis(j->j.lpush(this.parameter.get().getKey().getBytes(),byteArray));
                }else {
                    exeRedis(j->j.rpush(this.parameter.get().getKey().getBytes(),byteArray));
                }
                Platform.runLater(()->{
                    list.add(index,new ListTypeTable(byteArray));
                    find(null);
                    stage.close();
                });
            });
        });
    }

    /**
     * 插入尾
     * @param actionEvent
     */
    @FXML
    public void addTail(ActionEvent actionEvent) {
        this.add.setOnAction(this::addTail);
        this.add.setText(this.addTail.getText());
        this.add(actionEvent,list.size());
    }

    /**
     * 删除头数据
     *
     * @param actionEvent
     */
    @FXML
    public void delHead(ActionEvent actionEvent) {
        this.del.setOnAction(this::delHead);
        this.del.setText(this.delHead.getText());
        if (!GuiUtil.alertRemove()) {
            return;
        }
        asynexec(() -> {
            exeRedis(j -> j.lpop(this.getParameter().getKey()));
            ListTypeTable listTypeTable = list.get(0);
            GuiUtil.remove2UI(this.list,this.tableView,listTypeTable);
        });

    }

    /**
     * 删除尾数据
     *
     * @param actionEvent
     */
    @FXML
    public void delTail(ActionEvent actionEvent) {
        this.del.setOnAction(this::delTail);
        this.del.setText(this.delTail.getText());
        if (!GuiUtil.alertRemove()) {
            return;
        }
        asynexec(() -> {
            exeRedis(j -> j.rpop(this.getParameter().getKey()));
            ListTypeTable listTypeTable = list.get(list.size() - 1);
            GuiUtil.remove2UI(this.list,this.tableView,listTypeTable);
        });

    }


    /**
     * 删除选中行
     *
     * @param actionEvent
     */
    @FXML
    public void delRow(ActionEvent actionEvent) {
        this.del.setOnAction(this::delRow);
        this.del.setText(this.delRow.getText());
        if (!GuiUtil.alertRemove()) {
            return;
        }
        asynexec(() -> {
            exeRedis(j -> j.lset(this.getParameter().getKey().getBytes(), this.list.indexOf(lastSelect), DEL_MARK));
            exeRedis(j -> j.lrem(this.getParameter().getKey().getBytes(), 0, DEL_MARK));
            GuiUtil.remove2UI(this.list,this.tableView,lastSelect);
        });
    }


    public void close(ActionEvent actionEvent) {
        super.parentController.parentController.removeTabByKeys(Arrays.asList(parameter.get().getKey()));

    }
    @Override
    public void reloadInfo() {
        initInfo();
    }
}

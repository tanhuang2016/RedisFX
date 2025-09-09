package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyPageController;
import xyz.hashdog.rdm.ui.entity.ListTypeTable;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 9:52
 */
public class ListTypeController extends BaseKeyPageController<ListTypeTable> implements Initializable {
    @FXML
    public BorderPane borderPane;
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
    public SplitMenuButton add;
    public SplitMenuButton del;
    /**
     * 最后一个选中的行对应的最新的value展示
     */
    private ByteArrayController byteArrayController;

    private final static byte[] DEL_MARK = "del_mark".getBytes();

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
        findButton.setTooltip(GuiUtil.textTooltip(language("key.list.find")));
        add.setText(language("key.list.addHead"));
        addHead.setText(language("key.list.addHead"));
        addTail.setText(language("key.list.addTail"));
        del.setText(language("key.list.delRow"));
        delRow.setText(language("key.list.delRow"));
        delHead.setText(language("key.list.delHead"));
        delTail.setText(language("key.list.delTail"));
    }


    private void initButton() {
        initButtonStyles();
    }

    private void initButtonStyles() {
        add.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        del.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        tableViewListener();
        listListener();
    }


    private void bindData() {
        total.textProperty().bind(Bindings.createStringBinding(() -> String.format(TOTAL, this.list.size()), this.list));
        size.textProperty().bind(Bindings.createStringBinding(() -> String.format(SIZE, this.list.stream().mapToLong(e -> e.getBytes().length).sum()), this.list));
    }

    /**
     * 缓存list数据监听
     */
    private void listListener() {
        this.list.addListener((ListChangeListener<ListTypeTable>) change -> {
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
                    Tuple2<AnchorPane, ByteArrayController> tuple2 = loadByteArrayView(newValue.getBytes());
                    AnchorPane anchorPane = tuple2.t1();
                    byteArrayController = tuple2.t2();
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
        async(() -> {
            long total = this.exeRedis(j -> j.llen(this.parameter.get().getKey()));
            List<byte[]> bytes = this.exeRedis(j -> j.lrange(this.parameter.get().getKey().getBytes(), 0, (int) total));
            List<ListTypeTable> newList = new ArrayList<>();
            for (byte[] aByte : bytes) {
                newList.add(new ListTypeTable(aByte));
            }
            Platform.runLater(() -> {
                this.list.setAll(newList);
                GuiUtil.initSimpleTableView(tableView, new ListTypeTable());
                find(null);
                //设置默认选中第一行
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            });


        });

    }

    @Override
    protected Predicate<ListTypeTable> createNameFilter(String query) {
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
            exeRedis(j -> j.lset(this.getParameter().getKey().getBytes(), this.list.indexOf(lastSelect), byteArray));
            lastSelect.setBytes(byteArray);
            Platform.runLater(() -> {
                //实际上list存的引用,lastSelect修改,list中的元素也会修改,重新set进去是为了触发更新事件
                this.list.set(i, lastSelect);
                tableView.refresh();
                byteArrayController.setByteArray(byteArray);
                GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_SAVE_SUCCESS));
            });
        });
    }


    /**
     * 插入头
     *
     * @param actionEvent  事件
     */
    @FXML
    public void addHead(ActionEvent actionEvent) {
        this.add.setOnAction(this::addHead);
        this.add.setText(this.addHead.getText());
        this.add(actionEvent, 0);
    }


    /**
     * 封装插入方法
     *
     * @param actionEvent  事件
     * @param index       下标
     */
    private void add(ActionEvent actionEvent, int index) {
        Object source1 = actionEvent.getSource();
        String text = "";
        if (source1 instanceof MenuItem) {
            MenuItem source = (MenuItem) actionEvent.getSource();
            text = source.getText();
        } else if (source1 instanceof MenuButton) {
            MenuButton source = (MenuButton) actionEvent.getSource();
            text = source.getText();
        }
        Tuple2<AnchorPane, ByteArrayController> tuple2 = loadByteArrayView("".getBytes());
        Tuple2<AnchorPane, AppendController> appendTuple2 = loadFxml("/fxml/AppendView.fxml");
        Stage stage = GuiUtil.createSubStage(text, appendTuple2.t1(), root.getScene().getWindow());
        appendTuple2.t2().setCurrentStage(stage);
        appendTuple2.t2().setSubContent(tuple2.t1());
        stage.show();
        //设置确定事件咯
        appendTuple2.t2().ok.setOnAction(event -> {
            byte[] byteArray = tuple2.t2().getByteArray();
            async(() -> {
                if (index == 0) {
                    exeRedis(j -> j.lpush(this.parameter.get().getKey().getBytes(), byteArray));
                } else {
                    exeRedis(j -> j.rpush(this.parameter.get().getKey().getBytes(), byteArray));
                }
                Platform.runLater(() -> {
                    list.add(index, new ListTypeTable(byteArray));
                    find(null);
                    stage.close();
                });
            });
        });
    }

    /**
     * 插入尾
     *
     * @param actionEvent  事件
     */
    @FXML
    public void addTail(ActionEvent actionEvent) {
        this.add.setOnAction(this::addTail);
        this.add.setText(this.addTail.getText());
        this.add(actionEvent, list.size());
    }

    /**
     * 删除头数据
     *
     * @param actionEvent 事件
     */
    @FXML
    public void delHead(ActionEvent actionEvent) {
        this.del.setOnAction(this::delHead);
        this.del.setText(this.delHead.getText());
        if (GuiUtil.alertRemove()) {
            return;
        }
        async(() -> {
            exeRedis(j -> j.lpop(this.getParameter().getKey()));
            ListTypeTable listTypeTable = list.getFirst();
            GuiUtil.remove2TableView(this.list, this.tableView, listTypeTable);
        });

    }

    /**
     * 删除尾数据
     *
     * @param actionEvent  事件
     */
    @FXML
    public void delTail(ActionEvent actionEvent) {
        this.del.setOnAction(this::delTail);
        this.del.setText(this.delTail.getText());
        if (GuiUtil.alertRemove()) {
            return;
        }
        async(() -> {
            exeRedis(j -> j.rpop(this.getParameter().getKey()));
            ListTypeTable listTypeTable = list.getLast();
            GuiUtil.remove2TableView(this.list, this.tableView, listTypeTable);
        });

    }


    /**
     * 删除选中行
     *
     * @param actionEvent  事件
     */
    @FXML
    public void delRow(ActionEvent actionEvent) {
        this.del.setOnAction(this::delRow);
        this.del.setText(this.delRow.getText());
        if (GuiUtil.alertRemove()) {
            return;
        }
        async(() -> {
            exeRedis(j -> j.lset(this.getParameter().getKey().getBytes(), this.list.indexOf(lastSelect), DEL_MARK));
            exeRedis(j -> j.lrem(this.getParameter().getKey().getBytes(), 0, DEL_MARK));
            GuiUtil.remove2TableView(this.list, this.tableView, lastSelect);
        });
    }


    @FXML
    public void close(ActionEvent actionEvent) {
        super.parentController.getParentController().removeTabByKeys(Collections.singletonList(parameter.get().getKey()));

    }

    @Override
    public void reloadInfo() {
        initInfo();
    }
}

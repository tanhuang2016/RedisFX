package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.ui.common.ValueTypeEnum;
import redisfx.tanh.rdm.ui.controller.base.BaseKeyPageController;
import redisfx.tanh.rdm.ui.entity.StreamTypeTable;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import redisfx.tanh.rdm.ui.util.Util;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

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
        this.list.addListener((ListChangeListener<StreamTypeTable>) change -> {
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
            }
            if (newValue != null) {
                delRow.setDisable(false);
                this.lastSelect = newValue;
                Platform.runLater(() -> {
                    Tuple2<AnchorPane, ByteArrayController> valueTuple2 = loadByteArrayView(newValue.getBytes());
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
    @Override
    protected void initInfo() {
        async(() -> {
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
     * @param actionEvent 触发事件
     */
    @FXML
    public void add(ActionEvent actionEvent) {
        Button source = (Button)actionEvent.getSource();
        Tuple2<AnchorPane, ByteArrayController> tuple2 = loadByteArrayView( "".getBytes());
        tuple2.t2().setByteArray("".getBytes(),ValueTypeEnum.JSON);
        VBox vBox = new VBox();
        VBox.setVgrow(tuple2.t1(), Priority.ALWAYS);
        ObservableList<Node> children = vBox.getChildren();
        HBox hBox =createLabelHbox("ID");
        BorderPane.setAlignment(vBox,Pos.CENTER);
        TextField id = new TextField("*");
        children.addAll(hBox,id,tuple2.t1());
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
                try {
                    String idStr = exeRedis(j -> j.xadd(this.parameter.get().getKey(), v, new String(byteArray)));
                    Platform.runLater(()->{
                        list.add(new StreamTypeTable(idStr,new String(byteArray)));
                        find(null);
                        stage.close();
                        GuiUtil.messageAddSuccess();
                    });
                }catch (JsonSyntaxException e){
                    Platform.runLater(()->{
                        GuiUtil.messageError(e.getMessage());
                    });
                }

            });
        });
    }



    /**
     * 删除行
     *
     * @param actionEvent 触发事件
     */
    public void delRow(ActionEvent actionEvent) {
        if (GuiUtil.alertRemoveRowCancel()) {
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

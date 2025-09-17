/* SPDX-License-Identifier: MIT */

package xyz.hashdog.rdm.ui.sampler.page.custom;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.controller.setting.NewCustomConverterController;
import xyz.hashdog.rdm.ui.entity.CustomConverterTable;
import xyz.hashdog.rdm.ui.sampler.page.AbstractPage;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public final class CustomConverterPage extends AbstractPage {
    public static final String NAME = "main.setting.general.language";

    @Override
    public String getName() {
        return NAME;
    }

    public CustomConverterPage() throws IOException {
        super();
        addPageHeader();
        addFormattedText(language("main.setting.general.language.describe"));
        addNode(converterTable());

    }

    private Node converterTable() {
        TableView<CustomConverterTable> table = new TableView<>();
        TableColumn<CustomConverterTable, Boolean> enabled = new TableColumn<>("enabled");
        TableColumn<CustomConverterTable, Object> action = new TableColumn<>("action");
        table.getColumns().add(new TableColumn<>("#"));
        table.getColumns().add(new TableColumn<>("name"));
        table.getColumns().add(enabled);
        table.getColumns().add(action);

        table.getItems().addAll(
                new CustomConverterTable("UTF-8",true),
                new CustomConverterTable("UTF-16",true),
                new CustomConverterTable("UTF-32",true),
                new CustomConverterTable("GBK",true),
                new CustomConverterTable("GB2312",true),
                new CustomConverterTable("GB18030",true),
                new CustomConverterTable("Big5",true),
                new CustomConverterTable("Big5-HKSCS",true)
        );
        GuiUtil.initSimpleTableView(table,new CustomConverterTable());
        enabled.setCellFactory(param ->  getEnabledTableCell());
        action.setCellFactory(param ->  getActionTableCell());
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        Button add = new Button("新增",new FontIcon(Feather.PLUS));
        add.setOnAction(this::add);
        add.getStyleClass().addAll(Styles.FLAT);
        return new VBox(5,add,table);
    }

    private void add(ActionEvent actionEvent) {
        openAddOrUpdate(null);
    }

    private void openAddOrUpdate(CustomConverterTable currentRowData) {
        Tuple2<AnchorPane, NewCustomConverterController> tuple2 = GuiUtil.doLoadFxml("/fxml/setting/NewCustomConverterView.fxml");
        Stage subStage = GuiUtil.createSubStage(null, tuple2.t1(), this.getScene().getWindow());
        NewCustomConverterController controller = tuple2.t2();
        controller.setCurrentStage(subStage);
        controller.setParentController(this);
        subStage.show();
        subStage.setOnCloseRequest(event ->  controller.close());

    }

    private TableCell<CustomConverterTable, Boolean> getEnabledTableCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Boolean s, boolean b) {
                super.updateItem(s, b);
                if (!b) {
                    ToggleSwitch toggleSwitch = new ToggleSwitch();
                    toggleSwitch.setSelected(s);
                    setGraphic(toggleSwitch);
                    CustomConverterTable currentRowData =  getTableView().getItems().get(getIndex());
                    toggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        currentRowData.setEnabled(newValue);
                        System.out.println("切换: " + currentRowData.getName()+": "+newValue);
                    });
                }

            }
        };
    }
    private TableCell<CustomConverterTable, Object> getActionTableCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(null);
                    CustomConverterTable currentRowData =  getTableView().getItems().get(getIndex());
                    // 创建按钮等控件
                    Button editButton = new Button("编辑",new FontIcon(Feather.EDIT));
                    Button deleteButton = new Button("删除",new FontIcon(Material2AL.DELETE_OUTLINE));
                    editButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT);
                    deleteButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT);
                    // 为按钮添加事件处理器
                    editButton.setOnAction(event -> {
                        openAddOrUpdate(currentRowData);
                        System.out.println("编辑1: " + currentRowData.getName());
                        // 在这里添加编辑逻辑
                    });

                    deleteButton.setOnAction(event -> {
                        System.out.println("删除1: " + currentRowData.getName());
                        // 在这里添加删除逻辑
                    });
                    HBox hbox = new HBox(5, editButton, deleteButton);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);
                }
            }
        };
    }


}

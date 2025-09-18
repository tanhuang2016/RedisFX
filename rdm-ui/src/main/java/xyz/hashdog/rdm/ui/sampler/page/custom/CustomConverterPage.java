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
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;
import xyz.hashdog.rdm.ui.controller.setting.NewCustomConverterController;
import xyz.hashdog.rdm.ui.entity.CustomConverterTable;
import xyz.hashdog.rdm.ui.entity.config.CustomConverterSetting;
import xyz.hashdog.rdm.ui.entity.config.LanguageSetting;
import xyz.hashdog.rdm.ui.handler.convert.CustomInvokeConverter;
import xyz.hashdog.rdm.ui.handler.convert.ValueConverters;
import xyz.hashdog.rdm.ui.sampler.page.AbstractPage;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;
import java.util.List;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public final class CustomConverterPage extends AbstractPage {
    public static final String NAME = "main.setting.extension.converter";

    private TableView<CustomConverterTable> tableView;

    @Override
    public String getName() {
        return NAME;
    }

    public CustomConverterPage() throws IOException {
        super();
        addPageHeader();
        addFormattedText(language("main.setting.extension.converter.describe"));
        addNode(converterTable());

    }

    private Node converterTable() {
        tableView = new TableView<>();
        TableColumn<CustomConverterTable, Boolean> enabled = new TableColumn<>("enabled");
        TableColumn<CustomConverterTable, Object> action = new TableColumn<>("action");
        tableView.getColumns().add(new TableColumn<>("#"));
        tableView.getColumns().add(new TableColumn<>("name"));
        tableView.getColumns().add(enabled);
        tableView.getColumns().add(action);
        CustomConverterSetting configSettings = Applications.getConfigSettings(ConfigSettingsEnum.CONVERTER.name);
        List<CustomConverterTable> list = configSettings.getConverters().stream().map(e -> new CustomConverterTable(e.getName(), e.isEnabled())).toList();
        tableView.getItems().addAll(list);
        GuiUtil.initSimpleTableView(tableView,new CustomConverterTable());
        enabled.setCellFactory(param ->  getEnabledTableCell());
        action.setCellFactory(param ->  getActionTableCell());
        tableView.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        Button add = new Button(language("main.setting.extension.converter.add"),new FontIcon(Feather.PLUS));
        add.setOnAction(this::add);
        add.getStyleClass().addAll(Styles.FLAT);
        return new VBox(5,add,tableView);
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
        if(currentRowData==null){
            controller.setModel(BaseWindowController.ADD);
        }else {
            controller.setModel(BaseWindowController.UPDATE);
            controller.setName(currentRowData.getName());
        }
        subStage.show();
        subStage.setOnCloseRequest(event ->  controller.close());

    }

    private TableCell<CustomConverterTable, Boolean> getEnabledTableCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Boolean s, boolean b) {
                super.updateItem(s, b);
                if (b) {
                    setGraphic(null);
                } else {
                    ToggleSwitch toggleSwitch = new ToggleSwitch();
                    toggleSwitch.setSelected(s);
                    CustomConverterTable currentRowData = getTableView().getItems().get(getIndex());
                    toggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        CustomConverterSetting old = Applications.getConfigSettings(ConfigSettingsEnum.CONVERTER.name);
                        CustomInvokeConverter byName = old.getByName(currentRowData.getName());
                        byName.setEnabled(newValue);
                        updateConverter(byName);
                    });
                    setGraphic(toggleSwitch);
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
                    Button editButton = new Button(language("main.edit"),new FontIcon(Feather.EDIT));
                    editButton.setTooltip(GuiUtil.textTooltip(language("main.edit")));
                    Button deleteButton = new Button(language("server.delete"),new FontIcon(Material2AL.DELETE_OUTLINE));
                    deleteButton.setTooltip(GuiUtil.textTooltip(language("server.delete")));
                    editButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT);
                    deleteButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT);
                    // 为按钮添加事件处理器
                    editButton.setOnAction(event -> openAddOrUpdate(currentRowData));

                    deleteButton.setOnAction(event -> delete(currentRowData));
                    HBox hbox = new HBox(5, editButton, deleteButton);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);
                }
            }
        };
    }

    private void delete(CustomConverterTable currentRowData) {
        CustomConverterSetting old = Applications.getConfigSettings(ConfigSettingsEnum.CONVERTER.name);
        CustomInvokeConverter byName = old.getByName(currentRowData.getName());
        old.getConverters().remove(byName);
        CustomConverterSetting configSettings = new CustomConverterSetting();
        configSettings.setConverters(old.getConverters());
        Applications.putConfigSettings(configSettings.getName(), configSettings);
        tableView.getItems().remove(currentRowData);
        ValueConverters.getInstance().reLoad();
    }


    public void addConverter(CustomInvokeConverter converter) {
        CustomConverterTable customConverterTable = new CustomConverterTable(converter.getName(), converter.isEnabled());
        CustomConverterSetting old = Applications.getConfigSettings(ConfigSettingsEnum.CONVERTER.name);
        CustomConverterSetting configSettings = new CustomConverterSetting();
        configSettings.setConverters(old.getConverters());
        configSettings.getConverters().add(converter);
        Applications.putConfigSettings(configSettings.getName(), configSettings);
        tableView.getItems().add(customConverterTable);
        tableView.getSelectionModel().select(customConverterTable);
        ValueConverters.getInstance().reLoad();
    }

    public void updateConverter(CustomInvokeConverter converter) {
        CustomConverterSetting old = Applications.getConfigSettings(ConfigSettingsEnum.CONVERTER.name);
        CustomInvokeConverter byName = old.getByName(converter.getName());
        old.getConverters().set(old.getConverters().indexOf(byName),converter);
        CustomConverterSetting configSettings = new CustomConverterSetting();
        configSettings.setConverters(old.getConverters());
        Applications.putConfigSettings(configSettings.getName(), configSettings);
        CustomConverterTable item =findByName(converter.getName());
        CustomConverterTable newItem = new CustomConverterTable(converter.getName(), converter.isEnabled());
        int i = tableView.getItems().indexOf(item);
        //先删后插，是因为直接set会有图形异常
        tableView.getItems().remove(item);
        tableView.getItems().add(i,newItem);
        tableView.getSelectionModel().select(newItem);
        ValueConverters.getInstance().reLoad();
    }

    private CustomConverterTable findByName(String name) {
        for (CustomConverterTable item : tableView.getItems()) {
            if(item.getName().equals(name)){
                return item;
            }
        }
        return null;
    }
}

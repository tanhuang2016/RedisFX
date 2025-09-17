/* SPDX-License-Identifier: MIT */

package xyz.hashdog.rdm.ui.sampler.page.custom;

import atlantafx.base.util.BBCodeParser;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.controller.setting.LanguagePageController;
import xyz.hashdog.rdm.ui.entity.CustomConverterTable;
import xyz.hashdog.rdm.ui.entity.InfoTable;
import xyz.hashdog.rdm.ui.entity.TopKeyTable;
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
        table.getColumns().addAll(
                new TableColumn<>("#"),
                new TableColumn<>("name"),
                new TableColumn<>("enabled"),
                new TableColumn<>("action")
        );
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
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        VBox vBox = new VBox(table);
        return vBox;
    }


}

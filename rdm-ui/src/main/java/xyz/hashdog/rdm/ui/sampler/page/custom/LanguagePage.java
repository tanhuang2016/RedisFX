/* SPDX-License-Identifier: MIT */

package xyz.hashdog.rdm.ui.sampler.page.custom;

import atlantafx.base.util.BBCodeParser;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.controller.setting.LanguagePageController;
import xyz.hashdog.rdm.ui.sampler.page.AbstractPage;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public final class LanguagePage extends AbstractPage {
    public static final String NAME = "main.setting.general.language";
    private LanguagePageController controller;

    @Override
    public String getName() {
        return NAME;
    }

    public LanguagePage() throws IOException {
        super();

        addPageHeader();
        addFormattedText(language("main.setting.general.language.describe"));
        Tuple2<AnchorPane, LanguagePageController> tuple2 = GuiUtil.doLoadFxml("/fxml/setting/LanguagePage.fxml");
        controller=tuple2.t2();
        addNode(tuple2.t1());
    }


    public void resetLanguage() {
        PageHeader pageHeader = new PageHeader(this);
        this.userContent.getChildren().set(0, pageHeader);
        TextFlow formattedText = BBCodeParser.createFormattedText(language("main.setting.general.language.describe"));
        this.userContent.getChildren().set(1, formattedText);
        controller.resetLanguage();
    }
}

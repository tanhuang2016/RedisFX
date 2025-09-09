/* SPDX-License-Identifier: MIT */

package xyz.hashdog.rdm.ui.sampler.page.custom;

import javafx.scene.layout.AnchorPane;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.sampler.page.AbstractPage;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public final class AdvancedPage extends AbstractPage {
    public static final String NAME = "main.setting.global.advanced";

    @Override
    public String getName() {
        return NAME;
    }

    public AdvancedPage() throws IOException {
        super();

        addPageHeader();
        addFormattedText(language("main.setting.general.advanced.describe"));
        Tuple2<AnchorPane, Object> tuple2 = GuiUtil.doLoadFxml("/fxml/setting/AdvancedPage.fxml");
        addNode(tuple2.t1());
    }


    ///////////////////////////////////////////////////////////////////////////

}

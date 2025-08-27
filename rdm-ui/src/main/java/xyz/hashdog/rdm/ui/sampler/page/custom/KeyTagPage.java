/* SPDX-License-Identifier: MIT */

package xyz.hashdog.rdm.ui.sampler.page.custom;

import javafx.scene.layout.AnchorPane;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.sampler.page.AbstractPage;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public final class KeyTagPage extends AbstractPage {
    public static final String NAME = language("main.setting.global.key");

    @Override
    public String getName() {
        return NAME;
    }

    public KeyTagPage() throws IOException {
        super();

        addPageHeader();
        addFormattedText(language("main.setting.global.key.describe"));
        Tuple2<AnchorPane, Object> tuple2 = GuiUtil.doLoadFxml("/fxml/setting/KeyTagPage.fxml");
//        AnchorPane t1 = tuple2.getT1();
//        Node node = t1.getChildren().get(0);
        //todo 缩放的bug后面一起调整，现在先不管
        addNode(tuple2.t1());
    }


    ///////////////////////////////////////////////////////////////////////////

}

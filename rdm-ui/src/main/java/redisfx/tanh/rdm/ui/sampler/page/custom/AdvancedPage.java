/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.page.custom;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.ui.sampler.page.AbstractPage;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import java.io.IOException;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

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
        AnchorPane t1 = tuple2.t1();
        Node node = t1.getChildren().get(0);
        //todo 缩放的bug后面一起调整，现在先不管
        addNode(node);
//        addNode(tuple2.t1());
    }


    ///////////////////////////////////////////////////////////////////////////

}

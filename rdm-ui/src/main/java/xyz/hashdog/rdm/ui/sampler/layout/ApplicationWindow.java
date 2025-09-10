/* SPDX-License-Identifier: MIT */

package xyz.hashdog.rdm.ui.sampler.layout;


import atlantafx.base.controls.ModalPane;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import xyz.hashdog.rdm.ui.sampler.util.NodeUtils;

public final class ApplicationWindow  extends AnchorPane {

    public static final int MIN_WIDTH = 1200;
    public static final int SIDEBAR_WIDTH = 250;
    public static final String MAIN_MODAL_ID = "modal-pane";
    private static MainLayer mainLayer;


    public ApplicationWindow() {
        // this is the place to apply user custom CSS,
        // one level below the ':root'
        var body = new StackPane();
        body.getStyleClass().add("body");

        var modalPane = new ModalPane();
        modalPane.setId(MAIN_MODAL_ID);
        mainLayer = new MainLayer();
        body.getChildren().setAll(modalPane, mainLayer);
        NodeUtils.setAnchors(body, Insets.EMPTY);

        getChildren().setAll(body);
    }

    public static void resetLanguage() {
        mainLayer.resetLanguage();
    }
}

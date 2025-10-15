/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.page.general;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.paint.Color;
import redisfx.tanh.rdm.ui.sampler.layout.ModalDialog;
import redisfx.tanh.rdm.ui.sampler.util.JColorUtils;

class ContrastCheckerDialog extends ModalDialog {

    private final ContrastChecker contrastChecker;

    public ContrastCheckerDialog(ReadOnlyObjectProperty<Color> bgBaseColor) {
        super();

        this.contrastChecker = new ContrastChecker(bgBaseColor);

        contrastChecker.bgColorProperty().addListener((obs, old, val) -> updateStyle());
        contrastChecker.fgColorProperty().addListener((obs, old, val) -> updateStyle());

        getStyleClass().add("contrast-checker-dialog");
        header.setTitle("Contrast Checker");
        content.setBody(contrastChecker);
        content.setFooter(null);
    }

    private void updateStyle() {
        setStyle(String.format("-color-contrast-checker-bg:%s;-color-contrast-checker-fg:%s;",
            JColorUtils.toHexWithAlpha(contrastChecker.getFlatBgColor()),
            JColorUtils.toHexWithAlpha(contrastChecker.getSafeFgColor())
        ));
    }

    public ContrastChecker getContent() {
        return contrastChecker;
    }
}

package redisfx.tanh.rdm.ui.sampler.custom;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * @author th
 * @version 2.3.0
 * @since 2025/8/25 22:48
 */
public class HintTooltip extends Tooltip {
    public HintTooltip(String text,String hint) {
        super();
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.getChildren().add(new Label(text));
        Label label = new Label(hint);
        label.setStyle("-fx-text-fill: -color-fg-subtle;");
        hBox.getChildren().add(label);
        this.setGraphic(hBox);
    }




}

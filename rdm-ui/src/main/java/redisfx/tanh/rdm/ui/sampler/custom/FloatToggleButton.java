package redisfx.tanh.rdm.ui.sampler.custom;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import redisfx.tanh.rdm.ui.util.GuiUtil;

public class FloatToggleButton extends ToggleButton {

    static final Image image = GuiUtil.svgImage("/svg/statusDisabled/statusDisabled_red.svg",64,2) ;
    static final Cursor customCursor = new ImageCursor(image, image.getWidth()/2, image.getHeight()/2);

    public FloatToggleButton() {
        init();
    }



    public FloatToggleButton(String text) {
        super(text);
        init();
    }

    public FloatToggleButton(String text, Node graphic) {
        super(text, graphic);
        init();
    }

    private void init() {
        getStyleClass().addAll("float-toggle-button", Styles.FLAT);
        this.setCursor(Cursor.HAND);
        this.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // 被选中时的处理
                this.setCursor(customCursor);

            } else {
                // 未选中时的处理
                this.setCursor(Cursor.HAND);
            }
        });
    }

    @Override public void fire() {
        if (!isDisabled()) {
            //没选中，才进行选中
            if(!isSelected()){
                setSelected(!isSelected());
                fireEvent(new ActionEvent());
            }

        }
    }
}

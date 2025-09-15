package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

/**
 * 查看器文本类型抽象
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public abstract class AbstractTextViewer implements ValueViewer {
    @Override
    public ViewerTypeEnum type() {
        return ViewerTypeEnum.TEXT;
    }

    TextArea defaultPane(){
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        return textArea;
    }
}

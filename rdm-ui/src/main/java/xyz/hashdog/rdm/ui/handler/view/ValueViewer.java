package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import xyz.hashdog.rdm.ui.handler.CanHandle;

import java.util.List;

/**
 * 查看器
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public interface ValueViewer extends CanHandle {
    /**
     * 获取查看类型
     */
    ViewerTypeEnum getType();

    ViewerNode newViewerNode();

}

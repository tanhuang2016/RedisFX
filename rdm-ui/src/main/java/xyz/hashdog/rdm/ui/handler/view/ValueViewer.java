package xyz.hashdog.rdm.ui.handler.view;

import xyz.hashdog.rdm.ui.handler.CanHandle;

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
    ViewerTypeEnum type();

    ViewerNode newViewerNode();

    /**
     * 名称
     * @return 名称
     */
    String name();

}

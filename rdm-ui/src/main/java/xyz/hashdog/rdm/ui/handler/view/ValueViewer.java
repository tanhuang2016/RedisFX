package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;

import java.util.List;

/**
 * 查看器
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public interface ValueViewer {
    /**
     * 获取查看类型
     */
    ViewerTypeEnum getType();

    /**
     * 获取值
     */
    byte[] get();
    /**
     * 设置值
     */
    void set(byte[] value);
    /**
     * 获取查看器
     */
    Pane view();
    /**
     * 获取选项菜单
     */
    default List<MenuItem> options(){
        return null;
    }

}

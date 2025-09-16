package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

import java.util.Collections;
import java.util.List;

public interface ViewerNode {

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
    Node view();
    /**
     * 获取选项菜单
     */
    default List<MenuItem> options(){
       return Collections.emptyList();
    }
}

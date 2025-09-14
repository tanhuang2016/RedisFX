package xyz.hashdog.rdm.ui.handler.view;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValueViewers {

    private final Map<String, Class<?extends ValueViewer>> map;


    private ValueViewers() {
        map = Map.of(
                TextViewer.NAME, TextViewer.class,
                JsonViewer.NAME, JsonViewer.class,
                HexViewer.NAME, HexViewer.class,
                BinaryViewer.NAME, BinaryViewer.class,
                ImageViewer.NAME, ImageViewer.class
        );
    }

    /**
     * 获取单例实例
     * @return ValueConverters单例实例
     */
    public static ValueViewers getInstance() {
        return ValueViewers.ValueViewersHolder.INSTANCE;
    }
    /**
     * 内部类单例模式实现
     */
    private static class ValueViewersHolder {
        private static final ValueViewers INSTANCE = new ValueViewers();
    }
    /**
     * 获取所有转换器的名称
     * @return 所有转换器的名称列表
     */
    public List<String> names(){
        return new ArrayList<>(map.keySet());
    }
}

package xyz.hashdog.rdm.ui.handler.view;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ValueViewers {

    private final Map<String, Class<?extends ValueViewer>> map;


    private ValueViewers() {
        map = new LinkedHashMap<>();
        map.put(TextViewer.NAME, TextViewer.class);
        map.put(JsonViewer.NAME, JsonViewer.class);
        map.put(HexViewer.NAME, HexViewer.class);
        map.put(BinaryViewer.NAME, BinaryViewer.class);
        map.put(ImageViewer.NAME, ImageViewer.class);
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

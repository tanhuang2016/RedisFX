package xyz.hashdog.rdm.ui.handler.view;


import xyz.hashdog.rdm.ui.handler.convert.ValueConverter;

import java.util.*;

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

    public static ValueViewer viewerByValue(byte[] value) {
        //todo 需要给viewer专门做一个辅助工具，这样避免对象浪费，这个辅助工具可以直接创建viewer和判断是否这个类型，可以长期缓存
      return null;
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

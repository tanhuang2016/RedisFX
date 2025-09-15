package xyz.hashdog.rdm.ui.handler.view;


import xyz.hashdog.rdm.ui.handler.convert.Base64Converter;
import xyz.hashdog.rdm.ui.handler.convert.GzipConverter;
import xyz.hashdog.rdm.ui.handler.convert.NoneConverter;
import xyz.hashdog.rdm.ui.handler.convert.ValueConverter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueViewers {

    private final Map<String,ValueViewer> map;


    private ValueViewers() {
        map = Stream.of(new TextViewer(), new JsonViewer(), new HexViewer(), new BinaryViewer(), new ImageViewer())
                .collect(Collectors.toMap(
                        ValueViewer::name,
                        viewer -> viewer,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 获取单例实例
     * @return ValueConverters单例实例
     */
    public static ValueViewers getInstance() {
        return ValueViewers.ValueViewersHolder.INSTANCE;
    }

    public static ValueViewer viewerByValue(byte[] value) {
        List<ValueViewer> list = getInstance().map.values().stream()
                .sorted(Comparator.comparing(ValueViewer::order))
                .toList();
        for (ValueViewer viewer : list) {
            if(viewer.accept(value)){
                return viewer;
            }
        }
        return list.getLast();
    }

    public ViewerNode getViewerNodeByName(String newValue) {
        return map.get(newValue).newViewerNode();
    }

    public ValueViewer getByName(String name) {
        return map.get(name);
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

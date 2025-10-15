package redisfx.tanh.rdm.ui.handler.view;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * 查看器工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class ValueViewers {

    private final Map<String,ValueViewer> map;


    private ValueViewers() {
        map = Stream.of(new TextViewer(), new JsonViewer(), new XmlViewer(),new YmlViewer(),new HexViewer(), new BinaryViewer(), new ImageViewer())
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

    /**
     * 根据值获取对应的查看器
     * @param value 值
     * @return 对应的查看器
     */
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

    /**
     * 根据名称获取对应的查看器节点
     * @param newValue 名称
     * @return 对应的查看器节点
     */
    public ViewerNode getViewerNodeByName(String newValue) {
        return map.get(newValue).newViewerNode();
    }

    /**
     * 根据名称获取对应的查看器
     * @param name 名称
     * @return 对应的查看器
     */
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

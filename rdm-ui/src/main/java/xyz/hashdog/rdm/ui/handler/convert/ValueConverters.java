package xyz.hashdog.rdm.ui.handler.convert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueConverters {

    private final Map<String,ValueConverter> map;



    /**
     * 私有构造函数，防止外部实例化
     */
    private ValueConverters() {
        map = Stream.of(new Base64Converter(), new GzipConverter())
                .collect(Collectors.toMap(
                        ValueConverter::name,
                        valueConverter -> valueConverter,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 获取单例实例
     * @return ValueConverters单例实例
     */
    public static ValueConverters getInstance() {
        return ValueConvertersHolder.INSTANCE;
    }
    /**
     * 内部类单例模式实现
     */
    private static class ValueConvertersHolder {
        private static final ValueConverters INSTANCE = new ValueConverters();
    }
    /**
     * 获取所有转换器的名称
     * @return 所有转换器的名称列表
     */
    public List<String> names(){
        return new ArrayList<>(map.keySet());
    }
}

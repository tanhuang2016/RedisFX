package xyz.hashdog.rdm.ui.handler.convert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 编解码工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class ValueConverters {

    private final Map<String,ValueConverter> map;



    /**
     * 私有构造函数，防止外部实例化
     */
    private ValueConverters() {
        map = Stream.of(new NoneConverter(),new Base64Converter(), new GzipConverter())
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
     * 根据值获取对应的转换器
     * @param value 值
     * @return 对应的转换器
     */
    public static ValueConverter converterByValue(byte[] value) {
        List<ValueConverter> list = getInstance().map.values().stream()
                .sorted(Comparator.comparing(ValueConverter::order))
                .toList();
        for (ValueConverter valueConverter : list) {
            if(valueConverter.accept(value)){
                return valueConverter;
            }
        }
        return list.getLast();
    }

    /**
     * 根据名称获取对应的转换器
     * @param newValue 名称
     * @return 对应的转换器
     */
    public ValueConverter getByName(String newValue) {
        return map.get(newValue);
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

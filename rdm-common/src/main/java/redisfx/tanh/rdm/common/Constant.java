package redisfx.tanh.rdm.common;


import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 13:12
 */
public class Constant {
    /**
     * 字符集常量,这里设置了Local依然不会有区别,所以直接搞为常量吧
     */
    public static  final List<String> CHARSETS = Collections.unmodifiableList(Charset.availableCharsets().values().stream().map(e -> e.displayName(Locale.ENGLISH)).collect(Collectors.toList()));

}

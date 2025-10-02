package redisfx.tanh.rdm.ui.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * key类型标签枚举
 * @author th
 */

public enum KeyTypeTagEnum {
    STRING("string",Constant.COLOR_STRING),
    LIST("list",Constant.COLOR_LIST),
    HASH("hash",Constant.COLOR_HASH),
    SET("set",Constant.COLOR_SET),
    ZSET("zset",Constant.COLOR_ZSET),
    JSON("json",Constant.COLOR_JSON),
    STREAM("stream",Constant.COLOR_STREAM),

    UNKNOWN("unknown",Constant.COLOR_UNKNOWN);
    public final String tag;
    public final String color;
    KeyTypeTagEnum(String tag, String color) {
        this.tag = tag;
        this.color = color;
    }



    public String getTag() {
        return tag;
    }

    public String getColor() {
        return color;
    }

    /**
     * 获取所有tag
     * @return  tags
     */
    public static List<String> tags() {
        return Arrays.stream(KeyTypeTagEnum.values())
                .map(KeyTypeTagEnum::getTag)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有颜色
     * @return  colors
     */
    public static List<String> colors() {
        return Arrays.stream(KeyTypeTagEnum.values())
                .map(KeyTypeTagEnum::getColor)
                .collect(Collectors.toList());
    }
}

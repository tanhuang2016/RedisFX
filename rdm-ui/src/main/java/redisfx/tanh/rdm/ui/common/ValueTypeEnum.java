package redisfx.tanh.rdm.ui.common;

import redisfx.tanh.rdm.ui.handler.*;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/26 21:22
 */
public enum ValueTypeEnum {
    TEXT("Text"),
    JSON("Json"),
    HEX("Hex"),
    BINARY("Binary"),
    IMAGE("Image"),
    ;


    public final String name;

    ValueTypeEnum(String name) {
        this.name = name;
    }

    public static ValueTypeEnum getByName(String newValue) {
        for (ValueTypeEnum value : ValueTypeEnum.values()) {
            if(value.name.equals(newValue)){
                return value;
            }
        }
        return null;
    }
}

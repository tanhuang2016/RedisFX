package xyz.hashdog.rdm.ui.handler.convert;

import java.util.Base64;
/**
 * 标砖base64编解码转换工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class Base64Converter implements DataConverter{
    @Override
    public byte[] encode(byte[] data) {
        return Base64.getEncoder().encode(data);
    }

    @Override
    public byte[] decode(byte[] data) {
        return Base64.getDecoder().decode(data);
    }
}

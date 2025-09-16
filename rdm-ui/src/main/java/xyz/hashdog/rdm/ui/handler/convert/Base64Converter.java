package xyz.hashdog.rdm.ui.handler.convert;

import java.util.Base64;
/**
 * 标砖base64编解码转换工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class Base64Converter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        return Base64.getEncoder().encode(data);
    }

    @Override
    public byte[] decode(byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    @Override
    public boolean accept(byte[] data) {
        //不对base64校验，需要全字符校验，造成不必要的性能消耗
       return false;
    }

    @Override
    public String name() {
        return "Base64";
    }
}

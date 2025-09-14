package xyz.hashdog.rdm.ui.handler.convert;

/**
 * 编解码转换工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public interface DataConverter {

    /**
     * 编码
     * @param data 原始数据
     * @return 编码后的数据
     */
    byte[] encode(byte[] data);
    /**
     * 解码
     * @param data 被编码的数据
     * @return 解码后的数据
     */
    byte[] decode(byte[] data);
}

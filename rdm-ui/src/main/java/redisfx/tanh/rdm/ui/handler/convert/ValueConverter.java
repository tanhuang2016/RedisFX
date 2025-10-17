package redisfx.tanh.rdm.ui.handler.convert;

import redisfx.tanh.rdm.ui.handler.CanHandle;

/**
 * 编解码转换工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public interface ValueConverter  extends CanHandle {

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

    /**
     * 名称
     * @return 名称
     */
    String name();

    /**
     * 是否启用
     * @return true为启用
     */
   default boolean isEnabled(){
       return true;
   }

    /**
     * 是否可用
     * @return true为可用
     */
    default boolean isAvailable() {
        return true;
    }
}

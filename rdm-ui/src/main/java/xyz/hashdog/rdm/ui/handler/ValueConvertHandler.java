package xyz.hashdog.rdm.ui.handler;

import javafx.scene.Parent;

import java.nio.charset.Charset;

/**
 *  * 数据进来先责任链方式试探是否属于该处理类型
 *  * 是该出里类型，提供处理方式，默认直接展示16进制数据
 *  * 提供自定义上边栏按钮，按钮提供常用的模板导入，到处，弹框查看等
 *  * 数据的输入，输出由处理器直接提供
 * @author th
 * @version 1.0.0
 * @since 2023/8/8 21:48
 */
public interface ValueConvertHandler {

    /**
     * 文本转二进制
     * @param text
     * @param charset
     * @return
     */
    byte[] text2Byte(String text, Charset charset);

    /**
     * 二进制转文本
     * @param bytes
     * @param charset
     * @return
     */
    String byte2Text(byte[] bytes, Charset charset);

    /**
     * 查看窗口的控件
     * @param bytes
     * @param charset
     * @return
     */
    default Parent view(byte[] bytes, Charset charset){
        return null;
    }

    /**
     * 是否查看控件
     * @return
     */
    default boolean isView(){
        return false;
    }
}

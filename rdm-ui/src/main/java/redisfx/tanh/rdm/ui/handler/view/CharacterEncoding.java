package redisfx.tanh.rdm.ui.handler.view;

import java.nio.charset.Charset;

/**
 * 文本字符编码
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public interface CharacterEncoding {
    /**
     * 改变字符编码
     * @param charset 编码
     */
   void change(Charset charset);
   /**
     * 初始化字符编码
     * @param charset 编码
     */
   void init(Charset charset);

    /**
     * 获取文本
     * @return 文本
     */
    String text();
}

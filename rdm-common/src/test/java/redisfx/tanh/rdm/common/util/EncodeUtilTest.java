package redisfx.tanh.rdm.common.util;

import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/25 22:56
 */
public class EncodeUtilTest {

    @Test
    public void containsSpecialCharactersTest() {
        System.out.println(EncodeUtil.containsSpecialCharacters("123".getBytes(Charset.forName("gbk"))));
        System.out.println(EncodeUtil.containsSpecialCharacters("123".getBytes()));
        System.out.println(EncodeUtil.containsSpecialCharacters("你好".getBytes()));
        byte[] gbks = "你好".getBytes(Charset.forName("gbk"));
        System.out.println(EncodeUtil.containsSpecialCharacters(gbks));
    }








}

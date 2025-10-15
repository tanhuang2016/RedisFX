package redisfx.tanh.rdm.ui.handler;

import redisfx.tanh.rdm.common.util.FileUtil;

import java.nio.charset.Charset;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/8 22:48
 */
@Deprecated
public class HexConvertHandler implements ValueConvertHandler{

    @Override
    public byte[] text2Byte(String text, Charset charset) {
        return FileUtil.hexStringToByteArray(text);
    }

    @Override
    public String byte2Text(byte[] bytes, Charset charset) {
        return FileUtil.byte2HexString(bytes);
    }


}

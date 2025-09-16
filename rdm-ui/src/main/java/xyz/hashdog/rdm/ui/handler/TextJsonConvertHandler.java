package xyz.hashdog.rdm.ui.handler;

import xyz.hashdog.rdm.common.util.DataUtil;

import java.nio.charset.Charset;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/8 22:48
 */
@Deprecated
public class TextJsonConvertHandler implements ValueConvertHandler{

    @Override
    public byte[] text2Byte(String text, Charset charset) {
        return DataUtil.json2Byte(text,charset,false);
    }

    @Override
    public String byte2Text(byte[] bytes, Charset charset) {
        if(DataUtil.isBlank(new String(bytes))){
            return "";
        }
        return DataUtil.formatJson(bytes,charset,true);
    }


}

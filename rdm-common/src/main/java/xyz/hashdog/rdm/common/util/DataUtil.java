package xyz.hashdog.rdm.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 16:30
 */
public class DataUtil {


    /**
     * 获取uuid
     * @return uuid
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 判断字符是否为空
     * @param str 需要判断的字符串
     * @return 是否为空
     */
    public static boolean isBlank(String str) {
        return str==null||str.isEmpty();
    }
    /**
     * 判断字符是否不为空
     * @param str 需要判断的字符串
     * @return 是否不为空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为空
     * @param str 需要判断的
     * @return 是否为空
     */
    public static boolean isNotEmpty(String str) {
        if(str==null){
            return false;
        }
        if(str.isEmpty()){
            return false;
        }
        return true;
    }

    /**
     * 获取系统所有字体
     * @param locale 本地配置
     * @return 字体
     */
    public static List<String> getFonts(Locale locale) {
        // 获取本地图形环境
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // 获取所有可用的字体
        Font[] allFonts = ge.getAllFonts();
        return Stream.of(allFonts).map(e->e.getFontName(locale)).collect(Collectors.toList());
    }

    /**
     * json字符串格式化
     *
     * @param value   待格式化的字符串
     * @param charset 字符集
     * @return 格式化后的json
     */
    public static String formatJson(byte[] value, Charset charset,boolean isFormat) {
        String s = new String(value, charset);
        // 创建一个 GsonBuilder 来配置 Gson 的格式化选项
        GsonBuilder gsonBuilder = new GsonBuilder();
        if(isFormat){
            // 启用格式化输出
            gsonBuilder.setPrettyPrinting();
        }
        Gson gson = gsonBuilder.create();
        // 使用 Gson 格式化 JSON 字符串
        return gson.toJson(gson.fromJson(s, Object.class));

    }

    /**
     * json转byte[]
     * @param value json字符串
     * @param charset 字符集
     * @param isFormat true是需要格式化
     * @return byte[]
     */
    public static byte[] json2Byte(String value, Charset charset,boolean isFormat) {
       return formatJson(value.getBytes(charset),charset,isFormat).getBytes();

    }
}

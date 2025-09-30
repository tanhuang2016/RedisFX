package xyz.hashdog.rdm.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
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
     * 字符串为空时返回默认值
     * @param str 字符串
     * @param o 默认值
     * @return 字符串
     */
    public static String ifEmpty(String str, String o) {
        if(str==null||str.isEmpty()){
            return o;
        }
        return str;
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


    /**
     * 将XML字符串格式化为易读格式
     *
     * @param xml 待格式化的XML字符串
     * @return 格式化后的XML字符串
     */
    public static String formatXml(String xml) {
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(
                    new ByteArrayInputStream(xml.getBytes()));

            TransformerFactory transformerFactory =
                    javax.xml.transform.TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // 设置格式化输出属性
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");

            StringWriter writer = new StringWriter();
            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            // 解析失败时返回原始字符串
            return xml;
        }
    }

    /**
     * 将XML字符串压缩成单行
     *
     * @param xml 待压缩的XML字符串
     * @return 压缩后的单行XML字符串
     */
    public static String compressXml(String xml) {
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(
                    new java.io.ByteArrayInputStream(xml.getBytes()));
            TransformerFactory transformerFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // 设置压缩输出属性
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.STANDALONE, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(writer));

            // 移除所有换行符和多余空格
            return writer.toString().replace("\n", "").replace("\r", "").trim();
        } catch (Exception e) {
            // 简单的字符串压缩作为备选方案
            return xml.replace("\n", "").replace("\r", "").trim();
        }
    }
}

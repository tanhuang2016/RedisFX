package xyz.hashdog.rdm.redis.imp.console;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * redis返回协议类型及其解析策略
 * Redis网络协议主要基于‌RESP
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 23:24
 */
public enum ReaderParseEnum {

    /**
     * 对于简单字符串（Simple Strings），回复的第一个字节是“+”
     */
    SIMPLE_STRINGS('+', (l, r) -> new ArrayList<String>() {{
        add(l.substring(1));
    }}),
    /**
     * 对于错误（Errors ），回复的第一个字节是“-”
     */
    ERRORS('-', (l, r) -> new ArrayList<String>() {{
        add(l.substring(1));
    }}),
    /**
     * 对于整数（Integers ），回复的第一个字节是“：”
     */
    INTEGERS(':', (l, r) -> new ArrayList<String>() {{
        add(l.substring(1));
    }}),
    /**
     * 对于批量字符串（Bulk Strings），回复的第一个字节是“$”
     * 多行数据，这里用循环获取，后续如果有问题根据$符号返回的字符长度，固定获取数据reader.read(data, 0, length);
     */
    BULK_STRINGS('$', (l, r) -> {
        String newl=null;
        List<String> res = new ArrayList<>();
        int len = Integer.parseInt(l.substring(1));
        if(len==-1){
            res.add(newl);
            return res;
        }
        // 分配缓冲区
        char[] data = new char[len];
        // 读取指定长度的字节
        int read = r.read(data, 0, len);
        if (read != len) {
            throw new IOException("Incomplete bulk string data");
        }

        // 跳过 \r\n
        int crlf1 = r.read();
        int crlf2 = r.read();
        if (crlf1 != '\r' || crlf2 != '\n') {
            throw new IOException("Expected \\r\\n after bulk string data");
        }

        // 转换为字符串返回
        res.add(new String(data));
        return res;
    }),
    /**
     * 对于数组（Arrays ），回复的第一个字节是“*”
     */
    ARRAYS('*', (l, r) -> {
        List<String> result = new ArrayList<>();
        //返回的次数
        int count = Integer.parseInt(l.substring(1));
        if (count == -1) {
            result.add(null);
            return result;
        }
        for (int i = 0; i < count; i++) {
            String next = r.readLine();
            ReaderParseEnum readerParseEnum = ReaderParseEnum.byLine(next);
            if(readerParseEnum!=null){
                List<String> parse = readerParseEnum.readerParser.parse(next, r);
                result.addAll(parse);
            }else {
                List<String> parse = List.of(r.readLine());
                result.addAll(parse);
            }
        }
        return result;
    }),
    ;

    /**
     * 根据响应的单行数据,进行判断对应那个枚举
     *
     * @param line 一行字符
     * @return 解析类型
     */
    public static ReaderParseEnum getByLine(String line) {
        ReaderParseEnum readerParseEnum = byLine(line);
        if(readerParseEnum==null){
            char c = line.charAt(0);
            throw new RuntimeException("no this mark:" + c);
        }
        return readerParseEnum;
    }

    public static ReaderParseEnum byLine(String line) {
        char c = line.charAt(0);
        for (ReaderParseEnum value : ReaderParseEnum.values()) {
            if (value.mark == c) {
                return value;
            }
        }
      return null;
    }

    /**
     * 返回的协议标记
     */
    public final char mark;
    /**
     * 解析器
     */
    public final ReaderParser readerParser;


    ReaderParseEnum(char mark, ReaderParser readerParser) {
        this.mark = mark;
        this.readerParser = readerParser;
    }
}

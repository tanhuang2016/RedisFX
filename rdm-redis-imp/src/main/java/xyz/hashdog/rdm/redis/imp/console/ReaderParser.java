package xyz.hashdog.rdm.redis.imp.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 23:24
 */
public interface ReaderParser {
    /**
     * 解析
     * @param line 一行数据
     * @param reader 读取器
     * @return 解析结果
     */
    List<String> parse(String line, BufferedReader reader) throws IOException;
}

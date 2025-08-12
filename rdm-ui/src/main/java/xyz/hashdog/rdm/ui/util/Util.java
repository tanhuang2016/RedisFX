package xyz.hashdog.rdm.ui.util;

import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.entity.InfoTable;

import java.util.ArrayList;
import java.util.List;

public class Util extends xyz.hashdog.rdm.redis.imp.Util {
    public static List<InfoTable> parseInfoOutput(String infoOutput) {
        List<InfoTable> infoTables = new ArrayList<>();
        if (infoOutput == null || infoOutput.isEmpty()) {
            return infoTables;
        }
        String[] lines = infoOutput.split("\n");
        String currentSection = "";
        for (String line : lines) {
            line = line.trim();
            // 跳过空行和注释行（除了section标题）
            if (line.isEmpty()) {
                continue;
            }
            if(line.startsWith("#")){
                currentSection = line.substring(1);
                continue;
            }
            String[] split = line.split(":",2);
            // 创建InfoTable对象
            InfoTable infoTable = new InfoTable(split[0], currentSection.trim(), split[1]);
            infoTables.add(infoTable);
        }

        return infoTables;
    }

    /**
     * 将字节单位的内存大小转换为合适的单位
     * @param bytes 字节数
     * @return Tuple2<Double, String> 第一个元素是转换后的数值，第二个元素是单位名称
     */
    public static Tuple2<Double, String> convertMemorySize(long bytes) {
        if (bytes < 1024) {
            return new Tuple2<>((double) bytes, "B");
        } else if (bytes < 1024 * 1024) {
            return new Tuple2<>(bytes / 1024.0, "KB");
        } else if (bytes < 1024 * 1024 * 1024) {
            return new Tuple2<>(bytes / (1024.0 * 1024), "MB");
        } else {
            return new Tuple2<>(bytes / (1024.0 * 1024 * 1024), "GB");
        }
    }

    public static Tuple2<Double, String> convertMemorySize(String bytesStr) {
        try {
            long bytes = Long.parseLong(bytesStr);
            return convertMemorySize(bytes);
        } catch (NumberFormatException e) {
            // 如果解析失败，返回默认值
            return new Tuple2<>(0.0, "B");
        }
    }


}

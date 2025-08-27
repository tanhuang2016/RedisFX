package xyz.hashdog.rdm.ui.util;

import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.entity.InfoTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author th
 */
public class Util extends xyz.hashdog.rdm.redis.imp.Util {
    /**
     * 解析Redis的INFO命令输出
     * @param infoOutput INFO命令的输出结果
     * @return 一个包含INFO命令输出的InfoTable对象的列表
     */
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
    /**
     * 将字节单位的内存大小转换为合适的单位
     * @param bytes 字节数
     * @param scala 保留小数点位数
     * @return 字符串
     */
    public static String convertMemorySizeStr(long bytes,int scala) {
        Tuple2<Double, String> tuple2 = convertMemorySize(bytes);
        return format(tuple2.t1(),scala)+ tuple2.t2();
    }

    /**
     * 将字节单位的内存大小转换为合适的单位
     * @param bytesStr 字节数字符串
     * @return Tuple2<Double, String> 第1个元素是转换后的数值，第2个元素是单位名称
     */
    public static Tuple2<Double, String> convertMemorySize(String bytesStr) {
        try {
            long bytes = Long.parseLong(bytesStr);
            return convertMemorySize(bytes);
        } catch (NumberFormatException e) {
            // 如果解析失败，返回默认值
            return new Tuple2<>(0.0, "B");
        }
    }


    /**
     * 拼接字符串
     * @param separator 分隔符
     * @param number 从0拼接至number下标
     * @param parts 字符串数组
     * @return 拼接后的字符串
     */
    public static String join(String separator, int number, String[] parts) {
        if(number<=-1){
            return null;
        }
        return String.join(separator, Arrays.copyOfRange(parts, 0, number + 1));
    }
}

package xyz.hashdog.rdm.ui.util;

import xyz.hashdog.rdm.ui.entity.InfoTable;

import java.util.ArrayList;
import java.util.List;

public class Util {
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
            InfoTable infoTable = new InfoTable(split[0], currentSection, split[1]);
            infoTables.add(infoTable);
        }

        return infoTables;
    }
}

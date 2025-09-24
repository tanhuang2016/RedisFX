package xyz.hashdog.rdm.redis.imp.util;

import com.google.gson.*;

import java.io.*;
import java.util.*;

public class RedisCommandHelpParser {

    public static void main(String[] args) throws IOException {
        //https://github.com/redis/redis-doc/blob/master/commands.json 用这个json解析命令
        String file="C:\\Users\\Administrator\\Downloads\\original.json";
        String file2="C:\\Users\\Administrator\\Downloads\\commands.json";
        List<RedisCommandHelp> commands = RedisCommandHelpParser.parseCommands(new FileInputStream(file));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(commands);
        try (FileWriter writer = new FileWriter(file2)) {
            writer.write(json);
        }
    }
    
    /**
     * 解析 commands.json 文件，生成命令列表
     * @param jsonInputStream commands.json 文件输入流
     * @return 命令列表
     */
    public static List<RedisCommandHelp> parseCommands(InputStream jsonInputStream) {
        List<RedisCommandHelp> commands = new ArrayList<>();
        
        try {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(jsonInputStream)).getAsJsonObject();
            
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String commandName = entry.getKey();
                JsonObject commandObj = entry.getValue().getAsJsonObject();
                
                String signature = formatCommandSignature(commandName, commandObj);
                String group = commandObj.has("group") ? commandObj.get("group").getAsString() : "";
                String summary = commandObj.has("summary") ? commandObj.get("summary").getAsString() : "";
                int arity = commandObj.has("arity") ? commandObj.get("arity").getAsInt() : 0;
                
                RedisCommandHelp command = new RedisCommandHelp(commandName, signature, group, summary, arity);
                commands.add(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 按命令名称排序
        commands.sort(Comparator.comparing(RedisCommandHelp::getName));
        return commands;
    }
    
    /**
     * 格式化命令签名为 "COMMAND [param1] [param2]" 格式
     */
    private static String formatCommandSignature(String commandName, JsonObject commandObj) {
        StringBuilder signature = new StringBuilder();
        signature.append(commandName.toUpperCase());
        
        if (commandObj.has("arguments") && !commandObj.get("arguments").isJsonNull()) {
            JsonArray arguments = commandObj.getAsJsonArray("arguments");
            formatArguments(signature, arguments);
        }
        
        return signature.toString();
    }
    
    /**
     * 格式化参数
     */
    private static void formatArguments(StringBuilder signature, JsonArray arguments) {
        for (int i = 0; i < arguments.size(); i++) {
            JsonObject arg = arguments.get(i).getAsJsonObject();
            
            // 处理嵌套的命令结构
            if (arg.has("command")) {
                formatSubCommand(signature, arg);
            } else {
                formatSimpleArgument(signature, arg);
            }
        }
    }
    
    /**
     * 处理子命令结构
     */
    private static void formatSubCommand(StringBuilder signature, JsonObject arg) {
        String command = arg.get("command").getAsString();
        boolean isOptional = arg.has("optional") && arg.get("optional").getAsBoolean();
        
        // 构建子命令签名
        StringBuilder subCommandSignature = new StringBuilder(command);
        
        if (arg.has("arguments")) {
            JsonArray subArgs = arg.getAsJsonArray("arguments");
            formatArguments(subCommandSignature, subArgs);
        }
        
        String formatted = subCommandSignature.toString().trim();
        if (isOptional) {
            formatted = "[" + formatted + "]";
        }
        
        signature.append(" ").append(formatted);
    }
    
    /**
     * 处理简单参数
     */
    private static void formatSimpleArgument(StringBuilder signature, JsonObject arg) {
        String name = getArgumentName(arg);
        boolean isOptional = arg.has("optional") && arg.get("optional").getAsBoolean();
        boolean isMultiple = arg.has("multiple") && arg.get("multiple").getAsBoolean();
        
        String formatted = name;
        if (isMultiple) {
            formatted = name + " [" + name + " ...]";
        }
        
        // 大多数参数在Redis命令中都是必需的，但为了提示用户，我们用方括号表示
        formatted = "[" + formatted + "]";
        
        signature.append(" ").append(formatted);
    }
    
    /**
     * 获取参数名称
     */
    private static String getArgumentName(JsonObject arg) {
        if (arg.has("display_text")) {
            return arg.get("display_text").getAsString();
        } else if (arg.has("name")) {
            return arg.get("name").getAsString();
        }
        return "arg";
    }
}

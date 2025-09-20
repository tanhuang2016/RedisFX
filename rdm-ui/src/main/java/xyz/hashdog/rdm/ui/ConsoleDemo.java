package xyz.hashdog.rdm.ui;

import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.RedisContext;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.client.RedisConsole;
import xyz.hashdog.rdm.redis.imp.RedisFactory;

import java.io.IOException;
import java.util.List;

public class ConsoleDemo {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "terminal".equals(args[0])) {
            // 从环境变量获取连接信息
            String host = System.getenv("REDIS_HOST") != null ? System.getenv("REDIS_HOST") : "localhost";
            int port = System.getenv("REDIS_PORT") != null ? Integer.parseInt(System.getenv("REDIS_PORT")) : 6379;
            String password = System.getenv("REDIS_PASSWORD"); // 密码通过环境变量传递
            int db = System.getenv("REDIS_DB") != null ? Integer.parseInt(System.getenv("REDIS_DB")) : 0;

            runRedisTerminal(host, port, password, db);
        } else {
            // 启动模式 - 打开终端窗口
            openTerminalWindow();
        }
    }

    private static void openTerminalWindow() throws IOException {
        // 获取当前的Redis连接信息
        String host = "localhost";
        int port = 6379;
        String password = null; // 如果有密码
        int db = 0;

        String classPath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + "\\bin\\java.exe";
        String className = ConsoleDemo.class.getName();

        // 构造命令
        String cmd = String.format("\"%s\" -cp \"%s\" %s terminal",
                                  javaBin, classPath, className);

        // 通过ProcessBuilder设置环境变量并启动新进程
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",
                                              "start", "\"Redis Terminal\"",
                                              "cmd.exe", "/k", cmd);

        // 为新进程设置环境变量
        pb.environment().put("REDIS_HOST", host);
        pb.environment().put("REDIS_PORT", String.valueOf(port));
        if (password != null && !password.isEmpty()) {
            pb.environment().put("REDIS_PASSWORD", password);
        }
        pb.environment().put("REDIS_DB", String.valueOf(db));

        pb.start();
    }

    private static void runRedisTerminal(String host, int port, String password, int db) throws Exception {
        xyz.hashdog.rdm.redis.RedisFactory redisFactory = new RedisFactory();
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost(host);
        redisConfig.setPort(port);
        if (password != null && !password.isEmpty()) {
            redisConfig.setAuth(password);
        }
        // 如果RedisConfig支持设置数据库
        // redisConfig.setDatabase(db);

        RedisContext redisContext = redisFactory.createRedisContext(redisConfig);
        RedisClient redisClient = redisContext.newRedisClient();
        RedisConsole redisConsole = redisClient.getRedisConsole();

        System.out.println("=================================");
        System.out.println("Redis Terminal Client");
        System.out.println("Connected to: " + host + ":" + port);
        System.out.println("Type 'exit' to quit");
        System.out.println("=================================");

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String command;

        System.out.print("redis> ");
        while (scanner.hasNextLine()) {
            command = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(command)) {
                System.out.println("Bye!");
                break;
            }

            if (!command.isEmpty()) {
                try {
                    List<String> response = redisConsole.sendCommand(command);
                    for (String line : response) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    System.err.println("(error) " + e.getMessage());
                }
            }

            System.out.print("redis> ");
        }

        scanner.close();
    }
}

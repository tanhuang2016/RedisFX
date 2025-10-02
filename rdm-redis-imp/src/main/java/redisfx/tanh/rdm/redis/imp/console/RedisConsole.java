package redisfx.tanh.rdm.redis.imp.console;

import redisfx.tanh.rdm.redis.imp.util.Util;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用RedisConsole实现,不管什么类型的RedisClient,都只要实现不同的SocketAcquirer,
 * 就可以获取对应的socket进行交互通信
 *
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 21:31
 */
public class RedisConsole implements redisfx.tanh.rdm.redis.client.RedisConsole {

    /**
     * socket获取器
     */
    private final SocketAcquirer socketAcquirer;

    private final AutoCloseable connection;

    public RedisConsole(AutoCloseable connection, SocketAcquirer socketAcquirer) {
        this.connection = connection;
        this.socketAcquirer = socketAcquirer;
    }

    @Override
    public List<String> sendCommand(String cmd) {
        try (Socket socket = socketAcquirer.getSocket();) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            // 获取输入输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(cmd + "\r\n");
            writer.flush();
            return parseResult(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Util.close(connection);
        }
    }

    /**
     * 解析到结果集
     * 需要根据RESP解析需要的结果
     *
     * @param reader 输入流
     * @return 结果集
     * @throws IOException 输入流异常
     */
    private List<String> parseResult(BufferedReader reader) throws IOException {
        String line;
        if ((line = reader.readLine()) != null) {
            return ReaderParseEnum.getByLine(line).readerParser.parse(line, reader);
        }
        return new ArrayList<>();
    }
}

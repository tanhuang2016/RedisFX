package xyz.hashdog.rdm.ui.handler.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.hashdog.rdm.ui.Main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 自定义外部调用编解码工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/14 22:48
 */
public class CustomInvokeConverter implements ValueConverter {
    private static final Logger log = LoggerFactory.getLogger(CustomInvokeConverter.class);
    private final String name;
    private final Invoker encode;
    private final Invoker decode;

    public CustomInvokeConverter(String name, Invoker encode, Invoker decode) {
        this.name = name;
        this.encode = encode;
        this.decode = decode;
    }

    @Override
    public byte[] encode(byte[] data) {
        return encode.invoke(data);
    }

    @Override
    public byte[] decode(byte[] data) {
        return decode.invoke(data);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean accept(byte[] data) {
        return false;
    }

    public static class Invoker {

        /**
         * 默认缓冲区大小
         */
        private final static int DEFAULT_BUFFER_SIZE = 8192;
        /**
         * 命令
         */
        private final String cmd;
        /**
         * 文件路径
         */
        private final String filePath;
        /**
         * 默认用stdio的方式io数据
         * false就用filePath的方式传数据
         */
        private final boolean useCmd;
        public Invoker(String cmd, String filePath, boolean useCmd) {
            this.cmd = cmd;
            this.filePath = filePath;
            this.useCmd = useCmd;
        }
        /**
         * 执行命令 处理数据
         * @param data 参数
         * @return 结果
         */
        public byte[] invoke(byte[] data)  {
            try {
                if(useCmd){
                    String[] parts = cmd.split("\\s+");
                    ProcessBuilder pb = new ProcessBuilder(parts);
                    Process process = pb.start();
                    // 发送二进制数据到标准输入
                    try (OutputStream os = process.getOutputStream()) {
                        // 发送你二进制数据
                        os.write(data);
                        os.flush();
                    }
                    // 读取响应
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try (InputStream is = process.getInputStream()) {
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    // 重要：读取错误输出以便调试
                    StringBuilder errorOutput = new StringBuilder();
                    try (InputStream es = process.getErrorStream()) {
                        byte[] errorBuffer = new byte[1024];
                        int errorBytesRead;
                        while ((errorBytesRead = es.read(errorBuffer)) != -1) {
                            errorOutput.append(new String(errorBuffer, 0, errorBytesRead));
                        }
                    }
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        log.error("Script execution failed with exit code: {}, error output: {}",
                                exitCode, errorOutput);
                        throw new RuntimeException("Script execution failed with exit code: " +
                                exitCode + ", error: " + errorOutput);
                    }
                    return outputStream.toByteArray();
                }
            }catch (Exception e){
                log.error("CustomInvokeConverter invoke",e);
                throw new RuntimeException(e);
            }

            return null;
        }
    }
}

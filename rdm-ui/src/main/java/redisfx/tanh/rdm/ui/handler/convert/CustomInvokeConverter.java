package redisfx.tanh.rdm.ui.handler.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.common.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

/**
 * 自定义外部调用编解码工具
 *
 * @author th
 * @version 2.3.6
 * @since 2025/9/14 22:48
 */
public class CustomInvokeConverter implements ValueConverter {
    private static final Logger log = LoggerFactory.getLogger(CustomInvokeConverter.class);
    /**
     * 名称
     */
    private  String name;
    /**
     * 是否启用
     */
    private  boolean enabled;
    /**
     * 编码
     */
    private  Invoker encode;
    /**
     * 解码
     */
    private  Invoker decode;

    public CustomInvokeConverter(String name, Invoker encode, Invoker decode,boolean enabled) {
        this.name = name;
        this.encode = encode;
        this.decode = decode;
        this.enabled = enabled;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Invoker getEncode() {
        return encode;
    }

    public void setEncode(Invoker encode) {
        this.encode = encode;
    }

    public Invoker getDecode() {
        return decode;
    }

    public void setDecode(Invoker decode) {
        this.decode = decode;
    }

    public static class Invoker {

        /**
         * 默认缓冲区大小
         */
        private final static int DEFAULT_BUFFER_SIZE = 8192;
        /**
         * 命令
         */
        private  String cmd;
        /**
         * io文件所在目录
         * 输入数据默认文件是redis-fx.input
         * 输出为redis-fx.output
         */
        private  String ioDir;
        /**
         * 默认用stdio的方式io数据
         * false就用filePath的方式传数据
         */
        private  boolean useCmd;

        public Invoker(String cmd, String ioDir, boolean useCmd) {
            this.cmd = cmd;
            this.ioDir = ioDir;
            this.useCmd = useCmd;
        }

        /**
         * 执行命令 处理数据
         *
         * @param data 参数
         * @return 结果
         */
        public byte[] invoke(byte[] data) {
            try {
                //step1文件方式，先写入数据
                if (!useCmd) {
                    FileUtil.byteWrite2file(data,  Paths.get(ioDir, "redis-fx.input").toString());
                }
                //step2. 创建进程执行命令
                String[] parts = cmd.split("\\s+");
                log.info("cmd:{}", cmd);
                ProcessBuilder pb = new ProcessBuilder(parts);
                Process process = pb.start();
                //step3. stdio方式，就需要传数据
                if (useCmd) {
                    // 发送二进制数据到标准输入
                    try (OutputStream os = process.getOutputStream()) {
                        // 发送你二进制数据
                        os.write(data);
                        os.flush();
                    }
                }
                // step4读取响应
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
                // step5拿到结果
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error("Script execution failed with exit code: {}, error output: {}",
                            exitCode, errorOutput);
                    throw new RuntimeException("Script execution failed with exit code: " +
                            exitCode + ", error: " + errorOutput);
                }
                // step6返回数据
                if (useCmd) {
                    return outputStream.toByteArray();
                } else {
                    return FileUtil.file2byte( Paths.get(ioDir, "redis-fx.output").toString());
                }

            } catch (Exception e) {
                log.error("CustomInvokeConverter invoke", e);
                throw new RuntimeException(e);
            }

        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public String getIoDir() {
            return ioDir;
        }

        public void setIoDir(String ioDir) {
            this.ioDir = ioDir;
        }

        public boolean isUseCmd() {
            return useCmd;
        }

        public void setUseCmd(boolean useCmd) {
            this.useCmd = useCmd;
        }
    }
}

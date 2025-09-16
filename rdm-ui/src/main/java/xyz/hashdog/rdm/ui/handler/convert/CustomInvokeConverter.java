package xyz.hashdog.rdm.ui.handler.convert;
/**
 * 自定义外部调用编解码工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/14 22:48
 */
public class CustomInvokeConverter implements ValueConverter {
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
         * 命令
         */
        private final String cmd;
        /**
         * 文件路径
         */
        private final String filePath;
        /**
         * 默认用stdio的方式io数据
         * 是否通过文件的方式io数据
         */
        private final boolean isFile;
        public Invoker(String cmd, String filePath, boolean isFile) {
            this.cmd = cmd;
            this.filePath = filePath;
            this.isFile = isFile;
        }
        /**
         * 执行命令 处理数据
         * @param data 参数
         * @return 结果
         */
        public byte[] invoke(byte[] data) {
            return null;
        }
    }
}

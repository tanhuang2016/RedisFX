package redisfx.tanh.rdm.ui.handler.convert;


import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Zstd编解码转换工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/17 22:48
 */
public class ZstdConverter implements ValueConverter{
    private static Class<?> zstdInputStreamClass;
    private static Class<?> zstdOutputStreamClass;
    private static boolean zstdAvailable;

    static {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            zstdInputStreamClass = Class.forName("com.github.luben.zstd.ZstdInputStream", true, loader);
            zstdOutputStreamClass = Class.forName("com.github.luben.zstd.ZstdOutputStream", true, loader);
            zstdAvailable = true;
        } catch (ClassNotFoundException e) {
            zstdAvailable = false;
        }
    }

    @Override
    public boolean isAvailable() {
        return zstdAvailable;
    }

    @Override
    public byte[] encode(byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//             ZstdOutputStream zstdOutputStream = new ZstdOutputStream(byteArrayOutputStream)
             FilterOutputStream zstdOutputStream = (FilterOutputStream) zstdOutputStreamClass
                     .getConstructor(OutputStream.class)
                     .newInstance(byteArrayOutputStream)
        ) {
            zstdOutputStream.write(data);
            zstdOutputStream.close(); // 必须先关闭ZstdOutputStream以确保数据被完全写入
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress data using Zstd", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        if (!zstdAvailable) {
            throw new RuntimeException("Zstd library not available");
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
//             FilterInputStream zstdInputStream = new ZstdInputStream(byteArrayInputStream);
             FilterInputStream zstdInputStream = (FilterInputStream) zstdInputStreamClass
                     .getConstructor(InputStream.class)
                     .newInstance(byteArrayInputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zstdInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress data using Zstd", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
        if (!zstdAvailable) {
            return false;
        }
        if (data == null || data.length < 4) {
            return false;
        }
        // Zstd有特定的文件头魔术字节: 0x28, 0xB5, 0x2F, 0xFD
        return (data[0] == (byte) 0x28) &&
                (data[1] == (byte) 0xB5) &&
                (data[2] == (byte) 0x2F) &&
                (data[3] == (byte) 0xFD);
    }

    @Override
    public String name() {
        return "Zstd";
    }
}

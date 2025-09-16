package xyz.hashdog.rdm.ui.handler.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Gzip编解码转换工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class GzipConverter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compress data using GZIP", e);
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress data using GZIP", e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
        if (data == null || data.length < 2) {
            return false;
        }
        // GZIP文件头魔数: 0x1F 0x8B
        // 只需要检查前两个字节
        return (data[0] == (byte) 0x1F) && (data[1] == (byte) 0x8B);
    }

    @Override
    public String name() {
        return "Gzip";
    }
}

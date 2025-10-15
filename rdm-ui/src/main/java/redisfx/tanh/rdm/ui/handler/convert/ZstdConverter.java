package redisfx.tanh.rdm.ui.handler.convert;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Zstd编解码转换工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/17 22:48
 */
public class ZstdConverter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZstdOutputStream zstdOutputStream = new ZstdOutputStream(byteArrayOutputStream)) {
            zstdOutputStream.write(data);
            zstdOutputStream.close(); // 必须先关闭ZstdOutputStream以确保数据被完全写入
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress data using Zstd", e);
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ZstdInputStream zstdInputStream = new ZstdInputStream(byteArrayInputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zstdInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress data using Zstd", e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
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

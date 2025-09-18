package xyz.hashdog.rdm.ui.handler.convert;

import org.brotli.dec.BrotliInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Brotli编解码转换工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/17 22:48
 */
public class BrotliConverter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        return data;
    }

    @Override
    public byte[] decode(byte[] data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            // 使用 BrotliInputStream 进行解压
            BrotliInputStream brotliInputStream = new BrotliInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int len;
            while ((len = brotliInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            brotliInputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode data with Brotli", e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
        return false;
    }

    @Override
    public String name() {
        return "Brotli(only decode)";
    }
}

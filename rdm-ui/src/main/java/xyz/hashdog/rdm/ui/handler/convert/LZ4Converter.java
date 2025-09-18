package xyz.hashdog.rdm.ui.handler.convert;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * LZ4编解码转换工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/17 22:48
 */
public class LZ4Converter implements ValueConverter{
    private final LZ4Factory factory = LZ4Factory.fastestInstance();
    @Override
    public byte[] encode(byte[] data) {
        try {
            LZ4Compressor compressor = factory.fastCompressor();
            int maxCompressedLength = compressor.maxCompressedLength(data.length);
            byte[] compressed = new byte[maxCompressedLength];
            int compressedLength = compressor.compress(data, 0, data.length, compressed, 0, maxCompressedLength);

            // 返回实际压缩后的数据
            byte[] result = new byte[compressedLength];
            System.arraycopy(compressed, 0, result, 0, compressedLength);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to compress data using LZ4", e);
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        try {
            // 使用LZ4SafeDecompressor，它不需要预先知道解压后的大小
            LZ4SafeDecompressor decompressor = factory.safeDecompressor();
            // 由于我们不知道原始大小，我们需要估计一个足够大的缓冲区
            // 通常压缩数据解压后会变大，所以我们使用一个合理的估计
            byte[] decompressed = new byte[Math.max(1024, data.length * 4)];
            int decompressedLength = decompressor.decompress(data, decompressed);
            // 返回实际解压后的数据
            byte[] result = new byte[decompressedLength];
            System.arraycopy(decompressed, 0, result, 0, decompressedLength);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress data using LZ4", e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
        return false;
    }

    @Override
    public String name() {
        return "LZ4";
    }
}

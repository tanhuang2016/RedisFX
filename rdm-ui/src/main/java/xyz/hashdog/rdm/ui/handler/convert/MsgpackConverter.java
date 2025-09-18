package xyz.hashdog.rdm.ui.handler.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

/**
 * Msgpack编解码转换工具
 * @author th
 * @version 2.3.6
 * @since 2025/9/17 22:48
 */
public class MsgpackConverter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        try {
            // 使用MessagePack创建打包器
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(out);

            // 将原始字节数组作为二进制数据打包
            packer.packBinaryHeader(data.length);
            packer.writePayload(data);
            packer.close();

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode data with MessagePack", e);
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        try {
            // 使用MessagePack创建解包器
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
            // 读取二进制数据
            if (unpacker.hasNext()) {
                int payloadLen = unpacker.unpackBinaryHeader();
                byte[] payload = new byte[payloadLen];
                unpacker.readPayload(payload);
                unpacker.close();
                return payload;
            } else {
                unpacker.close();
                return new byte[0];
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode data with MessagePack", e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
        try {
            // 尝试解码以验证数据是否为有效的msgpack格式
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
            boolean result = unpacker.hasNext();
            unpacker.close();
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String name() {
        return "Msgpack";
    }
}

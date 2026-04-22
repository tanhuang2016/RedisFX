package redisfx.tanh.rdm.ui.handler.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Java原生序列化编解码转换工具
 * @author th
 * @version 2.3.6
 * @since 2026/4/22
 */
public class JavaSerializerConverter implements ValueConverter {

    @Override
    public byte[] encode(byte[] data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(data);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize data", e);
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            }
            return new byte[0];
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize data", e);
        }
    }

    @Override
    public boolean accept(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        // Java序列化魔数: AC ED 00 05
        return data[0] == (byte) 0xAC && data[1] == (byte) 0xED;
    }

    @Override
    public String name() {
        return "Java Serializer";
    }
}

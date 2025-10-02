package redisfx.tanh.rdm.ui.handler.convert;

/**
 * 默认的无需转换
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class NoneConverter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        return data;
    }

    @Override
    public byte[] decode(byte[] data) {
        return data;
    }

    @Override
    public String name() {
        return "None";
    }

    @Override
    public boolean accept(byte[] data) {
        return true;
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }
}

package xyz.hashdog.rdm.ui.handler.convert;

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
}

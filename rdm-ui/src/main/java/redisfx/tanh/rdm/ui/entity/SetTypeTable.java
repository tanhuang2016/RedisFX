package redisfx.tanh.rdm.ui.entity;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 22:35
 */
public class SetTypeTable implements ITable{

    private byte[] bytes;
    private String value;

    public SetTypeTable() {
    }

    public SetTypeTable(byte[] bytes) {
        this.bytes = bytes;
        this.value = new String(bytes);
    }
    @Override
    public  String[] getProperties() {
        return new String[]{"#row", "value"};
    }


    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        this.value= new String(bytes);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

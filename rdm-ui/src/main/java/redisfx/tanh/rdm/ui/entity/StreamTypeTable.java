package redisfx.tanh.rdm.ui.entity;

/**
 * @author th
 * @version 2.0.0
 * @since 2025/7/14 22:35
 */
public class StreamTypeTable extends AbstractRowTable implements ITable{


    private String id;
    private byte[] bytes;
    private String value;

    public StreamTypeTable() {
    }

    public StreamTypeTable(String id, String value) {
        this.id=id;

        this.bytes = value.getBytes();
        this.value = value;
    }
    // 获取所有属性名称
    @Override
    public  String[] getProperties() {
        return new String[]{"row", "id","value"};
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        this.value= new String(bytes);
    }


}

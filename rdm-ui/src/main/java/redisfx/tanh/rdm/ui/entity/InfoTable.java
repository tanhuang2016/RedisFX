package redisfx.tanh.rdm.ui.entity;


/**
 * @author th
 * @version 2.2.0
 * @since 2025/8/9 21:35
 */
public class InfoTable implements ITable {

    private String key;
    private String type;
    private String value;

    public InfoTable() {
    }

    // 获取所有属性名称
    public  String[] getProperties() {
        return new String[]{"#row", "type","key","value"};
    }


    public InfoTable(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

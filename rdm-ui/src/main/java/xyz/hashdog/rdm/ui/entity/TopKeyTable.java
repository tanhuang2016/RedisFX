package xyz.hashdog.rdm.ui.entity;


/**
 * @author th
 * @version 2.2.0
 * @since 2025/8/6 22:35
 */
public class TopKeyTable implements ITable {

    private String key;
    private String type;
    private String ttl;
    private String size;
    private String length;

    public TopKeyTable() {
    }

    // 获取所有属性名称
    public  String[] getProperties() {
        return new String[]{"#row", "type","key","ttl","size","length"};
    }


    public TopKeyTable(String key, String type, String ttl, String size, String length) {
        this.key = key;
        this.type = type;
        this.ttl = ttl;
        this.size = size;
        this.length = length;
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

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}

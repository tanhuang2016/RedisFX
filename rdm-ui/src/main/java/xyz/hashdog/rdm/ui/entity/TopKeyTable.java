package xyz.hashdog.rdm.ui.entity;


/**
 * @author th
 * @version 2.2.0
 * @since 2025/8/6 22:35
 */
public class TopKeyTable implements ITable {

    private String key;
    private String type;
    private Long ttl;
    private Long size;
    private Long length;

    public TopKeyTable() {
    }

    // 获取所有属性名称
    public  String[] getProperties() {
        return new String[]{"#row", "type","key","ttl","size","length"};
    }


    public TopKeyTable(String key, String type, Long ttl, Long size, Long length) {
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

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }
}

package xyz.hashdog.rdm.redis.imp.util;

public class RedisCommandHelp {

    private String name;
    private String signature;
    private String group;
    private String summary;
    private int arity; // 参数数量

    // 构造函数
    public RedisCommandHelp(String name, String signature, String group, String summary, int arity) {
        this.name = name;
        this.signature = signature;
        this.group = group;
        this.summary = summary;
        this.arity = arity;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getArity() { return arity; }
    public void setArity(int arity) { this.arity = arity; }

    @Override
    public String toString() {
        return signature;
    }
}

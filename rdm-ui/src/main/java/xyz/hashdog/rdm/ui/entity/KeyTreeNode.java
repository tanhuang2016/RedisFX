package xyz.hashdog.rdm.ui.entity;


import xyz.hashdog.rdm.ui.common.Constant;

/**
 * @author th
 */
public class KeyTreeNode {

    private KeyTreeNode parent;
    private String name;
    public String key;
    private int childKeyCount;
    /**
     * 需要时包装类型，copy的时候需要
     */
    private Boolean isLeaf;
    private String type;
    private Boolean initialized;
    /**
     * 新增的key时间,用于排序，让新增的key排在第一，可以减少不必要地加载
     */
    private long newKeyTime;
    private String prefix;
    private int level;

    public static KeyTreeNode leaf(String key) {
        KeyTreeNode keyTreeNode = new KeyTreeNode();
        keyTreeNode.setLeaf(true);
        keyTreeNode.setKey(key);
        keyTreeNode.setInitialized(false);
        return keyTreeNode;
    }
    public static KeyTreeNode leaf(String key, String type) {
        KeyTreeNode leaf = leaf(key);
        leaf.setType(type);
        return leaf;
    }
    public static KeyTreeNode newLeaf(String key, String type) {
        KeyTreeNode leaf = leaf(key,type);
        leaf.setNewKeyTime(System.currentTimeMillis());
        return leaf;
    }
    public static KeyTreeNode dir(String name) {
        KeyTreeNode dir = new KeyTreeNode();
        dir.setLeaf(false);
        dir.setName(name);
        dir.setInitialized(false);
        return dir;
    }


    public long getNewKeyTime() {
        return newKeyTime;
    }

    public void setNewKeyTime(long newKeyTime) {
        this.newKeyTime = newKeyTime;
    }

    @Override
    public String toString() {
        if (isLeaf) {
            return key;
        } else {
            return name + " (" + childKeyCount + ")";
        }
    }

    public String getName() {
        if (isLeaf) {
            return key;
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getChildKeyCount() {
        return childKeyCount;
    }

    public void setChildKeyCount(int childKeyCount) {
        this.childKeyCount = childKeyCount;
    }

    public Boolean getLeaf() {
        return isLeaf;
    }

    public void setLeaf(Boolean leaf) {
        isLeaf = leaf;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public KeyTreeNode getParent() {
        return parent;
    }

    public void setParent(KeyTreeNode parent) {
        this.parent = parent;
        this.prefix = parent.getPrefix() + Constant.KEY_SEPARATOR + getName();
        this.level=this.prefix.split(Constant.KEY_SEPARATOR).length;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getPrefix() {
        if(prefix==null){
           return getName();
        }
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void addChildKeyCount() {
        this.childKeyCount+=1;
        if(this.parent!=null){
            parent.addChildKeyCount();
        }
    }

    public void subChildKeyCount() {
        this.childKeyCount-=1;
        if(this.parent!=null){
            parent.subChildKeyCount();
        }

    }
}

package xyz.hashdog.rdm.ui.entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import xyz.hashdog.rdm.common.util.TUtil;

public class KeyTreeNode {
    private String name;
    public String key;
    private int childKeyCount;
    private Boolean isLeaf;
    private String type;

    public static KeyTreeNode leaf(String key) {
        KeyTreeNode keyTreeNode = new KeyTreeNode();
        keyTreeNode.setLeaf(true);
        keyTreeNode.setKey(key);
        return keyTreeNode;
    }
    public static KeyTreeNode dir(String name) {
        KeyTreeNode dir = new KeyTreeNode();
        dir.setLeaf(false);
        dir.setName(name);
        return dir;
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
        return name;
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
}

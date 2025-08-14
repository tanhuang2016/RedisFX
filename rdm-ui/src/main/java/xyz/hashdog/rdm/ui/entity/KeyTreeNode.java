package xyz.hashdog.rdm.ui.entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KeyTreeNode {
    private String name;
    public StringProperty key=new SimpleStringProperty();
    private int childKeyCount;
    private boolean isLeaf;
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
            return key.get();
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



    public int getChildKeyCount() {
        return childKeyCount;
    }

    public void setChildKeyCount(int childKeyCount) {
        this.childKeyCount = childKeyCount;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }
}

package xyz.hashdog.rdm.ui.entity;

import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * 库单选框实体
 * @author th
 * @version 1.0.0
 * @since 2023/7/22 15:27
 */
public class DBNode {
    /**
     * 名称
     */
    private final SimpleObjectProperty<String> name;
    /**
     * 库号
     */
    private int db;
    private int dbSize;

    public DBNode(int db,int dbSize) {
        this.db = db;
        this.dbSize=dbSize;
        String name = "DB"+db+"["+dbSize+"]";
        this.name = new SimpleObjectProperty<>(name);
    }


    public int getDb() {
        return db;
    }

    public int getDbSize() {
        return dbSize;
    }

    public void setDbSize(int dbSize) {
        this.dbSize = dbSize;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public String getName() {
        return name.get();
    }

    public SimpleObjectProperty<String> nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String toString() {
        return name.get();
    }
}

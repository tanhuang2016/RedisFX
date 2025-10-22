package redisfx.tanh.rdm.ui.entity;

/**
 * 需要分页的用这个抽象类，自定义行号
 * @author th
 * @version 2.3.6
 * @since 2025/9/16 22:35
 */
public abstract class AbstractRowTable {

    private int row;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}

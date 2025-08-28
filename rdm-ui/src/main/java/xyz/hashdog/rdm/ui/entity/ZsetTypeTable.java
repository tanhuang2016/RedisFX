package xyz.hashdog.rdm.ui.entity;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/3 22:35
 */
public class ZsetTypeTable implements ITable{


    private double score;
    private byte[] bytes;
    private String value;

    public ZsetTypeTable() {
    }

    public ZsetTypeTable(double score, byte[] bytes) {
        this.score=score;

        this.bytes = bytes;
        this.value = new String(bytes);
    }
    @Override
    public  String[] getProperties() {
        return new String[]{"#row", "score","value"};
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        this.value= new String(bytes);
    }


}

package redisfx.tanh.rdm.ui.entity;


/**
 * @author th
 * @version 2.2.0
 * @since 2025/8/9 21:35
 */
public class SubscribeTable extends AbstractRowTable implements ITable {

    private String time;
    private String channel;
    private String message;

    public SubscribeTable() {
    }

    // 获取所有属性名称
    @Override
    public  String[] getProperties() {
        return new String[]{"row", "time","channel","message"};
    }


    public SubscribeTable(String time, String channel, String message) {
        this.time = time;
        this.channel = channel;
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

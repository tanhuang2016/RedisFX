package redisfx.tanh.rdm.redis;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 10:38
 */
public class Message {
    /**
     * 是否成功
     */
   private boolean success;
    /**
     * 消息
     */
   private String message;

    public Message() {
    }

    public Message(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}

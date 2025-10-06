package redisfx.tanh.rdm.ui.exceptions;

/**
 *
 * 一般异常,需要自定义message的异常
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 10:30
 */
public class GeneralException extends RuntimeException {

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralException(Throwable cause) {
        super(cause);
    }
}

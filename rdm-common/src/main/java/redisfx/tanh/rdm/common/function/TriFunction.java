package redisfx.tanh.rdm.common.function;

/**
 * 三参数函数
 * @author th
 * @since 2025/8/17 17:05
 */
public interface TriFunction<T, U, V, R> {
    /**
     * 三个参数函数
     */
    R apply(T t, U u, V v);
}

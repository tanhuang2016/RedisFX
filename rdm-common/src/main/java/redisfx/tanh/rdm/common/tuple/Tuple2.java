package redisfx.tanh.rdm.common.tuple;

/**
 * 元组
 *
 * @author th
 * @version 1.0.0
 * @since 2023/8/1 12:43
 */
//public record Tuple2<T1, T2>(T1 t1, T2 t2) {

//}

public class Tuple2<T1, T2> {
    private final T1 t1;
    private final T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T1 t1() {
        return t1;
    }

    public T2 t2() {
        return t2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return t1.equals(tuple2.t1) && t2.equals(tuple2.t2);
    }

    @Override
    public int hashCode() {
        return t1.hashCode() * 31 + t2.hashCode();
    }

    @Override
    public String toString() {
        return "Tuple2{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                '}';
    }
}

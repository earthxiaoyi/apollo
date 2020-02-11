package cn.com.apollo.common.spi;

/**
 * spi 持有对象
 * @author jiaming
 * @param <T>
 */
public class Holder<T> {

    private volatile T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

package bitbot.server.scripting;

/**
 *
 * @author z
 */
public class MutableClass<T> {
    // T stands for "Type"
    private T t;

    public void set(T t) { this.t = t; }
    public T get() { return t; }
}

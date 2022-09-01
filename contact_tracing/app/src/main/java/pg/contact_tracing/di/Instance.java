package pg.contact_tracing.di;

public final class Instance<T> {
    private final T instance;

    private Instance(T instance) {
        this.instance = instance;
    }

    public static <T> Instance<T> of(T instance) {
        return new Instance<>(instance);
    }

    public <T> T get() {
        return (T) instance;
    }
}
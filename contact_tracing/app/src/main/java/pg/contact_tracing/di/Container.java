package pg.contact_tracing.di;

import java.util.HashMap;
import java.util.Map;

import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;

public class Container {
    protected static Container container;
    private final Map<Class<?>, Instance<?>> instances;

    protected Container() {
        instances = new HashMap<>();
    }

    public static Container getInstance() {
        if (Container.container == null)
            Container.container = new Container();
        return Container.container;
    }

    public <T> void register(Class<T> type, T instance) {
        this.instances.put(type, Instance.of(instance));
    }

    public Instance<?> resolve(Class<?> type) throws InstanceNotRegisteredDIException {
        if (!instances.containsKey(type))
            throw new InstanceNotRegisteredDIException(type.toString());
        return instances.get(type);
    }
}

package pg.contact_tracing.di;

import android.content.Context;

import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.repositories.UserContactsRepository;
import pg.contact_tracing.repositories.UserInformationsRepository;

public class DI {
    private static Container container;

    public static void registerDependencies(Context context) {
        container = Container.getInstance();
        container.register(UserInformationsRepository.class, new UserInformationsRepository(context));
        container.register(UserContactsRepository.class, new UserContactsRepository(context));
    }

    public static <T> T resolve(Class<T> type) throws InstanceNotRegisteredDIException {
        Instance<T> instance = (Instance<T>) container.resolve(type);
        return instance.get();
    }
}

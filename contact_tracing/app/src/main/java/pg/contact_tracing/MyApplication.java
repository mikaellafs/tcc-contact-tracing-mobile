package pg.contact_tracing;
import android.app.Application;

import pg.contact_tracing.di.DI;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DI.registerDependencies(this);
    }
}

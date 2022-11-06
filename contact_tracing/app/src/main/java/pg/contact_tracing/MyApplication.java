package pg.contact_tracing;
import android.app.Application;

import pg.contact_tracing.di.DI;
import pg.contact_tracing.utils.broadcastReceivers.BluetoothBroadcastReceiver;
import pg.contact_tracing.utils.broadcastReceivers.LocationBroadcastReceiver;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DI.registerDependencies(this);
        BluetoothBroadcastReceiver.register(this);
        LocationBroadcastReceiver.register(this);
    }
}

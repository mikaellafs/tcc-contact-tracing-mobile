package pg.contact_tracing.services.managers;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import pg.contact_tracing.R;
import pg.contact_tracing.services.core.BeaconService;

public class BeaconServiceManager {
    public static final String BEACON_SERVICE_MANAGER_LOG = "BEACON_SERVICE_MANAGER";

    public void start(Context context) {
        Intent serviceIntent = new Intent(context, BeaconService.class);

        ContextCompat.startForegroundService(context, serviceIntent);
        Log.i(BEACON_SERVICE_MANAGER_LOG,"Start service");
    }
    public void stop(Context context) {
        Intent serviceIntent = new Intent(context, BeaconService.class);
        context.stopService(serviceIntent);
        Log.i(BEACON_SERVICE_MANAGER_LOG,"Stop service");
    }

    public boolean isTracing() {
        boolean isRunning = BeaconService.isRunning;
        Log.i(BEACON_SERVICE_MANAGER_LOG,"Service is running: " + isRunning);

        return isRunning;
    }
}

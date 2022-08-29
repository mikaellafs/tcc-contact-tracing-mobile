package pg.contact_tracing.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import pg.contact_tracing.R;
import pg.contact_tracing.services.BeaconService;

public class BeaconServiceManager {
    public static final String BEACON_SERVICE_USECASE = "BEACON_SERVICE_USECASE";

    public void start(Context context) {
        Intent serviceIntent = new Intent(context, BeaconService.class);

        serviceIntent.putExtra(
                context.getString(R.string.beacon_notification_subtitle_field),
                context.getString(R.string.beacon_notification_subtitle)
        );

        ContextCompat.startForegroundService(context, serviceIntent);
        Log.i(BEACON_SERVICE_USECASE,"Start service");
    }
    public void stop(Context context) {
        Intent serviceIntent = new Intent(context, BeaconService.class);
        context.stopService(serviceIntent);
        Log.i(BEACON_SERVICE_USECASE,"Stop service");
    }

    public boolean isTracing() {
        boolean isRunning = BeaconService.isRunning;
        Log.i(BEACON_SERVICE_USECASE,"Service is running: " + isRunning);

        return isRunning;
    }
}

package pg.contact_tracing.utils.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import pg.contact_tracing.services.managers.BeaconServiceManager;
import pg.contact_tracing.utils.NotificationBroadcastCenter;

public class LocationBroadcastReceiver extends BroadcastReceiver {
    private static final String LOCATION_RECEIVER_LOG = "LOCATION_RECEIVER";
    private BeaconServiceManager manager;

    public LocationBroadcastReceiver() {
        manager = new BeaconServiceManager();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (!action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            return;
        }
        Log.i(LOCATION_RECEIVER_LOG, "Location manager status changed");

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (manager.isTracing() && !isGpsEnabled && !isNetworkEnabled) {
            Log.i(LOCATION_RECEIVER_LOG, "Cannot trace contacts without location on");
            manager.stop(context);
            NotificationBroadcastCenter.sendNotification(context, NotificationBroadcastCenter.Event.BEACON_SERVICE_FAILED, "Location is off");
        }
    }

    public static void register(Context context) {
        LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
        IntentFilter intent = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        context.registerReceiver(receiver, intent);
    }
}
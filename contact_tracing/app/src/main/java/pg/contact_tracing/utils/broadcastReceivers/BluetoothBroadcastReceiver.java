package pg.contact_tracing.utils.broadcastReceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import pg.contact_tracing.services.managers.BeaconServiceManager;
import pg.contact_tracing.utils.NotificationBroadcastCenter;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private static final String BLUETOOTH_RECEIVER_LOG = "BLUETOOTH_RECEIVER";
    private BeaconServiceManager manager;

    public BluetoothBroadcastReceiver() {
        manager = new BeaconServiceManager();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                Log.i(BLUETOOTH_RECEIVER_LOG, "Bluetooth is off");
                if (manager.isTracing()) {
                    Log.i(BLUETOOTH_RECEIVER_LOG, "Can't keep tracing without bluetooth on");
                    manager.stop(context);
                    NotificationBroadcastCenter.sendNotification(context, NotificationBroadcastCenter.Event.BEACON_SERVICE_FAILED, "Bluetooth is off");
                }
            }
        }
    }

    public static void register(Context context) {
        BluetoothBroadcastReceiver receiver = new BluetoothBroadcastReceiver();
        IntentFilter intent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver, intent);
    }
}

package pg.contact_tracing.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationBroadcastCenter {
    public enum Event {
        RISK_NOTIFICATION {
            @Override
            public String toString() {
                return "RISK_NOTIFICATION";
            }
        },
        NOT_RISK_NOTIFICATION {
            @Override
            public String toString() {
                return "NOT_RISK_NOTIFICATION";
            }
        },
        BEACON_SERVICE_FAILED {
            @Override
            public String toString() {
                return "BEACON_SERVICE_FAILED";
            }
        },
        MQTT_SERVICE_FAILED {
            @Override
            public String toString() {
                return "MQTT_SERVICE_FAILED";
            }
        },
    }

    public static void sendNotification(Context context, Event event, String message) {
        Intent intent = new Intent(event.toString());
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void registerReceiver(Context context, Event event, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                new IntentFilter(event.toString()));
    }
}

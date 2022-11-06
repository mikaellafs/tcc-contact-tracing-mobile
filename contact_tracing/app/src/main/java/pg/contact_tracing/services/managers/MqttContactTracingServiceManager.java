package pg.contact_tracing.services.managers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

import pg.contact_tracing.services.core.MqttContactTracingService;

public class MqttContactTracingServiceManager {
    public static final String MQTT_SERVICE_MANAGER_LOG = "MQTT_CONTACT_TRACING_SERVICE_MANAGER";
    private static final int RETRY_MQTT_SERVICE_INTERVAL = 5 * 1000 * 60; // 5 minutes

    public void start(Context context) {
        Intent serviceIntent = new Intent(context, MqttContactTracingService.class);

        ContextCompat.startForegroundService(context, serviceIntent);
        Log.i(MQTT_SERVICE_MANAGER_LOG,"Start service");
    }
    public void stop(Context context) {
        Intent serviceIntent = new Intent(context, MqttContactTracingService.class);
        context.stopService(serviceIntent);
        Log.i(MQTT_SERVICE_MANAGER_LOG,"Stop service");
    }

    public boolean isRunning() {
        boolean isRunning = MqttContactTracingService.isRunning;
        Log.i(MQTT_SERVICE_MANAGER_LOG,"Service is running: " + isRunning);

        return isRunning;
    }

    public void retryStartServiceLoop(Context context) {
        new Timer().schedule(new TimerTask(){
            @Override
            public void run(){
                Log.i(MQTT_SERVICE_MANAGER_LOG, "Try to start MQTT service");
                boolean isTracing = new BeaconServiceManager().isTracing();
                if (isRunning() || !isTracing) {
                    Log.i(MQTT_SERVICE_MANAGER_LOG, "Do not retry starting MQTT service");
                    return;
                }

                new MqttContactTracingServiceManager().start(context);
                retryStartServiceLoop(context);
            }
        }, RETRY_MQTT_SERVICE_INTERVAL);
    }
}

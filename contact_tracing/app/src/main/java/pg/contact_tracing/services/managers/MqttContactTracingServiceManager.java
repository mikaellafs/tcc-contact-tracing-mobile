package pg.contact_tracing.services.managers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import pg.contact_tracing.services.core.MqttContactTracingService;

public class MqttContactTracingServiceManager {
    public static final String MQTT_CONTACT_TRACING_SERVICE_MANAGER_LOG = "MQTT_CONTACT_TRACING_SERVICE_MANAGER";

    public void start(Context context) {
        Intent serviceIntent = new Intent(context, MqttContactTracingService.class);

        ContextCompat.startForegroundService(context, serviceIntent);
        Log.i(MQTT_CONTACT_TRACING_SERVICE_MANAGER_LOG,"Start service");
    }
    public void stop(Context context) {
        Intent serviceIntent = new Intent(context, MqttContactTracingService.class);
        context.stopService(serviceIntent);
        Log.i(MQTT_CONTACT_TRACING_SERVICE_MANAGER_LOG,"Stop service");
    }

    public boolean isRunning() {
        boolean isRunning = MqttContactTracingService.isRunning;
        Log.i(MQTT_CONTACT_TRACING_SERVICE_MANAGER_LOG,"Service is running: " + isRunning);

        return isRunning;
    }
}

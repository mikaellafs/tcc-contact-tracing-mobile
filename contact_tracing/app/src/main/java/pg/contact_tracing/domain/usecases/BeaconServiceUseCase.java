package pg.contact_tracing.domain.usecases;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import pg.contact_tracing.infra.services.BeaconService;

public class BeaconServiceUseCase {
    public void start(Context context) {
        Intent serviceIntent = new Intent(context, BeaconService.class);
        serviceIntent.putExtra("inputExtra", "@strings/");
        ContextCompat.startForegroundService(context, serviceIntent);
    }
    public void stop(Context context) {
        Intent serviceIntent = new Intent(context, BeaconService.class);
        context.stopService(serviceIntent);
    }
}

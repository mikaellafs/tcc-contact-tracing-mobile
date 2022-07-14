package pg.contact_tracing.infra.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import pg.contact_tracing.MainActivity;
import pg.contact_tracing.R;
import pg.contact_tracing.domain.errors.UserInformationNotFoundException;
import pg.contact_tracing.domain.usecases.UserContactsUseCase;
import pg.contact_tracing.domain.usecases.UserInformationsUseCase;

public class BeaconService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String LOG_KEY_TRANSMIT = "BEACON_SERVICE_TRANSMIT";
    public static final String LOG_KEY_MONITOR = "BEACON_SERVICE_MONITOR";

    private UserContactsUseCase userContactsUseCase;
    private UserInformationsUseCase userInformationsUseCase;
    private String userID;
    private int appManufacturer;

    @Override
    public void onCreate() {
        super.onCreate();
        userInformationsUseCase = new UserInformationsUseCase(this);
        userContactsUseCase = new UserContactsUseCase();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            userID = userInformationsUseCase.getUUID();
            appManufacturer = userInformationsUseCase.getAppManufacturer();
        } catch (UserInformationNotFoundException e) {
            stopSelf();
        }

        Notification notification = createNotification(intent);

        startForeground(1, notification);

        transmitBeacon();
        monitorBeacons();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.beacon_service_channel),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private Notification createNotification(Intent intent) {
        String subtitle = intent.getStringExtra(getString(R.string.beacon_notification_subtitle_field));
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.beacon_notification_title))
                .setContentText(subtitle)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        return notification;
    }

    private void transmitBeacon(){
        Beacon beacon = new Beacon.Builder()
                .setId1(userID)
                .setId2("1")
                .setId3("2")
                .setManufacturer(appManufacturer)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT);
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Log.e(LOG_KEY_TRANSMIT, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(LOG_KEY_TRANSMIT, "Advertisement start succeeded.");
            }
        });
    }

    private void monitorBeacons() {
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    userContactsUseCase.saveBeacon(beacons);

                    for (Beacon beacon: beacons) {
                        Log.i(LOG_KEY_MONITOR, "didRangeBeaconsInRegion, beacon = " + beacon.toString());

                        if (beacon.getDistance() <= 1.0) {
                            Log.i(LOG_KEY_MONITOR, "Very close beacon: " + beacon.getDistance());
                        } else {
                            Log.i(LOG_KEY_MONITOR, "Far beacon, discard: " + beacon.getDistance());
                        }
                    }
                }
            }
        });

        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        identifiers.add(null);
        beaconManager.startRangingBeacons(new Region("aaa", identifiers));
    }
}

//    AsyncTask.execute(new Runnable() {
//        @Override public void run() { }
//    });
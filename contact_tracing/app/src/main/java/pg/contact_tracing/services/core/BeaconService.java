package pg.contact_tracing.services.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
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

import pg.contact_tracing.di.DI;
import pg.contact_tracing.ui.activities.MainActivity;
import pg.contact_tracing.R;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.utils.UserContactsManager;
import pg.contact_tracing.repositories.UserInformationsRepository;

public class BeaconService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String BEACON_SERVICE_LOG = "BEACON_SERVICE";
    public static final String BEACON_SERVICE_TRANSMIT_LOG = "BEACON_SERVICE_TRANSMIT";
    public static final String BEACON_SERVICE_MONITOR_LOG = "BEACON_SERVICE_MONITOR";

    public static boolean isRunning;

    private static long SCAN_PERIOD_INTERVAL = 15000; // 15 sec
    private UserContactsManager userContactsManager;
    private UserInformationsRepository userInformationsRepository;
    private String userID;
    private int appManufacturer;

    private BeaconTransmitter beaconTransmitter;
    private BeaconManager beaconManager;
    private Region region;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            userInformationsRepository = DI.resolve(UserInformationsRepository.class);
        } catch (Exception e) {
            Log.e(BEACON_SERVICE_LOG, "Failed to resolve userInformation Repository: " + e.toString());
        }
        userContactsManager = new UserContactsManager();

        // Beacon transmitter
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT);
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

        // Beacon monitoring
        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        identifiers.add(null);
        region = new Region("aaa", identifiers);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        BeaconService.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            userID = userInformationsRepository.getUUID();
            appManufacturer = userInformationsRepository.getAppManufacturer();
        } catch (UserInformationNotFoundException e) {
            Log.e(BEACON_SERVICE_LOG, "Failed to get user information: " + e.toString());
            stopSelf();
        }

        Notification notification = createNotification(intent);

        startForeground(1, notification);

        transmitBeacon();
        monitorBeacons();

        BeaconService.isRunning = true;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconTransmitter.stopAdvertising();
        beaconManager.stopRangingBeacons(region);

        Log.i(BEACON_SERVICE_LOG, "Beacon service destroyed");
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
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

        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Log.e(BEACON_SERVICE_TRANSMIT_LOG, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(BEACON_SERVICE_TRANSMIT_LOG, "Advertisement start succeeded.");
            }
        });
    }

    private void monitorBeacons() {
        Log.i(BEACON_SERVICE_MONITOR_LOG, "Monitor beacons");

        try {
            beaconManager.setForegroundScanPeriod(SCAN_PERIOD_INTERVAL);
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            Log.e(BEACON_SERVICE_MONITOR_LOG, "Failed to update scan interval: " + e.toString());
        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.i(BEACON_SERVICE_MONITOR_LOG, "Beacons find: " + beacons.size());

                if (beacons.size() > 0) {
                    for (Beacon beacon: beacons) {
                        saveBeacon(beacon);

                        Log.i(BEACON_SERVICE_MONITOR_LOG, "didRangeBeaconsInRegion, beacon = " + beacon.toString());

                        if (beacon.getDistance() <= 1.0) {
                            Log.i(BEACON_SERVICE_MONITOR_LOG, "Very close beacon: " + beacon.getDistance());
                        } else {
                            Log.i(BEACON_SERVICE_MONITOR_LOG, "Far beacon, discard: " + beacon.getDistance());
                        }
                    }
                }
            }
        });


        beaconManager.startRangingBeacons(region);

    }

    private void saveBeacon(Beacon beacon) {
        AsyncTask.execute(new Runnable() {
            @Override public void run() {
                userContactsManager.saveBeacon(beacon, getApplicationContext());
            }
        });
    }
}


package pg.contact_tracing.services.core;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import pg.contact_tracing.di.DI;
import pg.contact_tracing.utils.NotificationBroadcastCenter;
import pg.contact_tracing.utils.NotificationCreator;
import pg.contact_tracing.utils.UserContactsManager;
import pg.contact_tracing.repositories.UserInformationsRepository;

public class BeaconService extends Service {
    public static final String BEACON_SERVICE_LOG = "BEACON_SERVICE";
    public static final String BEACON_SERVICE_TRANSMIT_LOG = "BEACON_SERVICE_TRANSMIT";
    public static final String BEACON_SERVICE_MONITOR_LOG = "BEACON_SERVICE_MONITOR";

    public static boolean isRunning;

    private static final long SCAN_PERIOD_INTERVAL = 15000; // 15 sec
    private static final int id = 1;

    private UserContactsManager userContactsManager;
    private UserInformationsRepository userInformationsRepository;
    private String userID = "";
    private int appManufacturer;

    private BeaconTransmitter beaconTransmitter;
    private BeaconManager beaconManager;
    private Region region;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            userInformationsRepository = DI.resolve(UserInformationsRepository.class);
            userID = userInformationsRepository.getID();
        } catch (Exception e) {
            Log.e(BEACON_SERVICE_LOG, "Failed to resolve userInformation Repository: " + e);
        }
        userContactsManager = new UserContactsManager();

        // Beacon transmitter
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT);
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

        // Beacon monitoring
        ArrayList<Identifier> identifiers = new ArrayList<>();
        identifiers.add(null);
        region = new Region(userID, identifiers);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        BeaconService.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appManufacturer = userInformationsRepository.getAppManufacturer();

        Notification notification = NotificationCreator.foregroundServiceNotification;

        if (notification == null) {
            stopSelf();

            NotificationBroadcastCenter.sendNotification(
                    this,
                    NotificationBroadcastCenter.Event.BEACON_SERVICE_FAILED,
                    "Could not start tracing contacts"
            );
            return START_NOT_STICKY;
        }

        startForeground(id, notification);

        transmitBeacon();
        monitorBeacons();

        BeaconService.isRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconTransmitter.stopAdvertising();
        beaconManager.stopRangingBeacons(region);
        isRunning = false;

        Log.i(BEACON_SERVICE_LOG, "Beacon service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void transmitBeacon(){
        Beacon beacon = new Beacon.Builder()
                .setId1(userID)
                .setId2("1")
                .setId3("2")
                .setManufacturer(appManufacturer)
                .setTxPower(-70)
                .setDataFields(Collections.singletonList(0L)) // Remove this for beacon layouts without d: fields
                .build();

        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Log.e(BEACON_SERVICE_TRANSMIT_LOG, "Advertisement start failed with code: " + errorCode);
                stopSelf();

                NotificationBroadcastCenter.sendNotification(
                        BeaconService.this,
                        NotificationBroadcastCenter.Event.BEACON_SERVICE_FAILED,
                        "Could start transmitting beacons"
                );
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
            beaconManager.setForegroundBetweenScanPeriod(SCAN_PERIOD_INTERVAL);
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            Log.e(BEACON_SERVICE_MONITOR_LOG, "Failed to update scan interval: " + e);
        }

        beaconManager.addRangeNotifier((beacons, region) -> {
            Log.i(BEACON_SERVICE_MONITOR_LOG, "Beacons found: " + beacons.size());

            if (beacons.size() > 0) {
                for (Beacon beacon: beacons) {
                    saveBeacon(beacon);

                    Log.i(BEACON_SERVICE_MONITOR_LOG, "didRangeBeaconsInRegion, beacon = " + beacon.toString());

                    if (beacon.getDistance() <= 2.0) {
                        Log.i(BEACON_SERVICE_MONITOR_LOG, "Very close beacon: " + beacon.getDistance());
                    } else {
                        Log.i(BEACON_SERVICE_MONITOR_LOG, "Far beacon, discard: " + beacon.getDistance());
                    }
                }
            }
        });


        beaconManager.startRangingBeacons(region);

    }

    private void saveBeacon(Beacon beacon) {
        Log.i(BEACON_SERVICE_MONITOR_LOG, "Save beacon received: " + beacon.getId1());
        AsyncTask.execute(() -> userContactsManager.saveBeacon(beacon, getApplicationContext()));
    }
}


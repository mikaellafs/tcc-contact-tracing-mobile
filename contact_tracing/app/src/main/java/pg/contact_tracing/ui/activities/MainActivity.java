package pg.contact_tracing.ui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import pg.contact_tracing.R;
import pg.contact_tracing.ui.fragments.WarningBanner;
import pg.contact_tracing.utils.BeaconServiceManager;
import pg.contact_tracing.utils.NotificationBroadcastCenter;

public class MainActivity extends AppCompatActivity {
    private static final String MAIN_ACTIVITY_LOG = "MAIN_ACTIVITY";
    private Switch tracingSwitch;
    private TextView tracingTitle;
    private TextView tracingSubtitle;
    private ImageView tracingImage;

    private WarningBanner warningBanner;

    BeaconServiceManager beaconServiceManager;

    private String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private String bluetoothAdvertisePermission = Manifest.permission.BLUETOOTH_ADVERTISE;
    private String bluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN;
    private String bluetoothConnectPermission = Manifest.permission.BLUETOOTH_CONNECT;

    BroadcastReceiver userNotAtRiskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideWarningBanner();
        }
    };

    BroadcastReceiver userAtRiskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showWarningBanner(intent.getStringExtra("message"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconServiceManager = new BeaconServiceManager();

        tracingTitle = findViewById(R.id.title_is_tracing);
        tracingSubtitle = findViewById(R.id.subtitle_is_tracing);
        tracingImage = findViewById(R.id.tracing_image);
        tracingSwitch = findViewById(R.id.tracing_switch);

        setSwitchAction();
        setWarningBanner();
        setReceiversNotification();
    }

    private void setSwitchAction() {
        boolean isTracing = beaconServiceManager.isTracing();
        tracingSwitch.setChecked(isTracing);

        tracingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = getApplicationContext();

                if (isChecked) startTracing(context); else stopTracing(context);
            }
        });
    }

    private void setReceiversNotification() {
        Log.i(MAIN_ACTIVITY_LOG, "Setting receivers about user at risk to show or hide banner");

        // Listen to event user not at risk - hide banner
        NotificationBroadcastCenter.registerReceiver(this, NotificationBroadcastCenter.Event.NOT_RISK_NOTIFICATION, userNotAtRiskReceiver);

        // Listen to event user at risk - show banner
        NotificationBroadcastCenter.registerReceiver(this, NotificationBroadcastCenter.Event.RISK_NOTIFICATION, userAtRiskReceiver);
    }

    private void hideWarningBanner() {
        Log.i(MAIN_ACTIVITY_LOG, "Hide warning banner fragment");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(warningBanner);
        ft.commit();
    }

    private void showWarningBanner(String message) {
        Log.i(MAIN_ACTIVITY_LOG, "Show warning banner fragment");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(warningBanner);
        ft.commit();

        warningBanner.setMessage(message);
    }

    private void setWarningBanner() {
        warningBanner = new WarningBanner();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.banner, warningBanner);

        ft.commit();
    }

    private void enableBluetooth() {
        Log.i(MAIN_ACTIVITY_LOG, "Enable bluetooth");
        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (!btAdapter.isEnabled()) {
            // Show message that bluetooth was turned on
            // TODO: GET PERMISSIONS BEFORE
            btAdapter.enable();
        }
    }

    private boolean checkAndRequestPermissions(Context context) {
        Log.i(MAIN_ACTIVITY_LOG, "Checking permissions");
        ArrayList<String> permissionsNotGranted = new ArrayList<>();
        String[] permissions;

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion > 28) {
            permissions = new String[]{locationPermission, bluetoothAdvertisePermission, bluetoothScanPermission, bluetoothConnectPermission};
        } else {
            permissions = new String[]{locationPermission};
        }

        for (String permission : permissions) {
            boolean isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;

            if (!isGranted) {
                Log.i(MAIN_ACTIVITY_LOG, "Permission: " + permission + " is not granted");
                permissionsNotGranted.add(permission);
            }
        }

        if (!permissionsNotGranted.isEmpty())
            ActivityCompat.requestPermissions(this, permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]), 1);
        else
            Log.i(MAIN_ACTIVITY_LOG, "All permissions required are granted");

        return permissionsNotGranted.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(MAIN_ACTIVITY_LOG, "Request permissions result");

        Context context = getApplicationContext();

        for (int isGranted : grantResults) {
            if (isGranted == PackageManager.PERMISSION_GRANTED)
                continue;

            tracingSwitch.setChecked(false);
            Toast.makeText(context,"Não é possível iniciar o rastreamento sem as permissões necessárias",Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTracing(context)) {
            tracingSwitch.setChecked(true);
        }
    }

    public boolean startTracing(Context context) {
        if (!checkAndRequestPermissions(context)) {
            return false;
        }
        enableBluetooth();

        Log.i(MAIN_ACTIVITY_LOG, "Start tracing...");
        beaconServiceManager.start(context);
        setToTracingMode();
        return true;
    }

    public void stopTracing(Context context) {
        Log.i(MAIN_ACTIVITY_LOG, "Stop tracing...");
        beaconServiceManager.stop(context);
        setToNotTracingMode();
    }

    private void setToTracingMode() {
        Log.i(MAIN_ACTIVITY_LOG, "Setting activity to tracing mode");
        tracingTitle.setText(R.string.main_tracing_active_title);
        tracingSubtitle.setText(R.string.main_tracing_active_subtitle);
        tracingImage.setBackgroundResource(R.drawable.illustration_tracing);
    }

    private void setToNotTracingMode() {
        Log.i(MAIN_ACTIVITY_LOG, "Setting activity to NOT tracing mode");
        tracingTitle.setText(R.string.main_tracing_inactive_title);
        tracingSubtitle.setText(R.string.main_tracing_inactive_subtitle);
        tracingImage.setBackgroundResource(R.drawable.illustration_not_tracing);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userAtRiskReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userNotAtRiskReceiver);
        super.onDestroy();
    }
}
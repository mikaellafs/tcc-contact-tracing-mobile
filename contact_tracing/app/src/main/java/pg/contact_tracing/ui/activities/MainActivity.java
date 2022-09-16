package pg.contact_tracing.ui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
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

import java.util.ArrayList;

import pg.contact_tracing.R;
import pg.contact_tracing.ui.fragments.WarningBanner;
import pg.contact_tracing.utils.BeaconServiceManager;

public class MainActivity extends AppCompatActivity {
    private static final String MAIN_ACTIVITY_LOG = "MAIN_ACTIVITY";
    private Switch tracingSwitch;
    private TextView tracingTitle;
    private TextView tracingSubtitle;
    private ImageView tracingImage;

    BeaconServiceManager beaconServiceManager;

    private String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private String bluetoothAdvertisePermission = Manifest.permission.BLUETOOTH_ADVERTISE;
    private String bluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN;
    private String bluetoothConnectPermission = Manifest.permission.BLUETOOTH_CONNECT;

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

    private void setWarningBanner() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.banner, new WarningBanner());
        ft.commit();
    }

    private void enableBluetooth() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (!btAdapter.isEnabled()) {
            // Show message that bluetooth was turned on
            btAdapter.enable();
        }
    }

    private boolean checkAndRequestPermissions(Context context) {
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

            if (!isGranted)
                permissionsNotGranted.add(permission);
        }

        if (!permissionsNotGranted.isEmpty())
            ActivityCompat.requestPermissions(this, permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]), 1);

        return permissionsNotGranted.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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

        beaconServiceManager.start(context);
        setToTracingMode();
        return true;
    }

    public void stopTracing(Context context) {
        beaconServiceManager.stop(context);
        setToNotTracingMode();
    }

    private void setToTracingMode() {
        tracingTitle.setText(R.string.main_tracing_active_title);
        tracingSubtitle.setText(R.string.main_tracing_active_subtitle);
        tracingImage.setBackgroundResource(R.drawable.illustration_tracing);
    }

    private void setToNotTracingMode() {
        tracingTitle.setText(R.string.main_tracing_inactive_title);
        tracingSubtitle.setText(R.string.main_tracing_inactive_subtitle);
        tracingImage.setBackgroundResource(R.drawable.illustration_not_tracing);
    }
}
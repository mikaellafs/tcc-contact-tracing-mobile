package pg.contact_tracing.ui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
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

import pg.contact_tracing.R;
import pg.contact_tracing.ui.fragments.WarningBanner;
import pg.contact_tracing.utils.BeaconServiceManager;

public class MainActivity extends AppCompatActivity {
    private Switch tracingSwitch;
    private TextView tracingTitle;
    private TextView tracingSubtitle;
    private ImageView tracingImage;

    BeaconServiceManager beaconServiceManager;

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

    private boolean checkAndAskLocationPermission(Context context) {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(context, locationPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{locationPermission}, 1);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Context context = getApplicationContext();
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(context, locationPermission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Não é possível iniciar o rastreamento sem a permissão",Toast.LENGTH_SHORT).show();
        } else {
            if (startTracing(context)) {
                tracingSwitch.setChecked(true);
            }
        }
    }

    public boolean startTracing(Context context) {
        Log.i("MAIN_ACTIVITY_TRACING", "start tracing");
        if (!checkAndAskLocationPermission(context)) {
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
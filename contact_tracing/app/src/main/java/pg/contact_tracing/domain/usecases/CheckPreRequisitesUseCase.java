package pg.contact_tracing.domain.usecases;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CheckPreRequisitesUseCase {
    Activity screen;

    public CheckPreRequisitesUseCase(Activity screen) {
        this.screen = screen;
    }

    public boolean checkAndAskLocationPermission(Context context) {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(context, locationPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(screen, new String[]{locationPermission}, 1);
            return false;
        }

        return true;
    }

    public boolean checkAndAskBluetooth(Context context) {
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (!btAdapter.isEnabled()) {
            btAdapter.enable();
        }

        return true;
    }
}

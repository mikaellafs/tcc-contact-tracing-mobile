package pg.contact_tracing.domain.usecases;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

public class CheckPreRequisitesUseCase {
    public boolean checkAndAskLocationPermission() {
        // TODO: Implement check and ask location permission
        return false;
    }

    public boolean checkAndAskBluetooth() {
        // TODO: Implement check and ask bluetooth
//        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        BluetoothAdapter btAdapter = btManager.getAdapter();
//        if (btAdapter.isEnabled()) {
//            boolean isSupported = btAdapter.isMultipleAdvertisementSupported();
//
//        } else {
//            // Turn on bluetooth
//        }

        return false;
    }
}

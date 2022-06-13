package pg.contact_tracing;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

import pg.contact_tracing.databinding.ActivityMainBinding;
import pg.contact_tracing.infra.services.BeaconService;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Beacon transmitter
//        Beacon beacon = new Beacon.Builder()
//                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
//                .setId2("1")
//                .setId3("2")
//                .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
//                .setTxPower(-59)
//                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
//                .build();
//
//        // Change the layout below for other beacon types
//        BeaconParser beaconParser = new BeaconParser()
//                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
//        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
//        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
//
//            @Override
//            public void onStartFailure(int errorCode) {
//                Log.e("BATATA", "Advertisement start failed with code: "+errorCode);
//            }
//
//            @Override
//            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//                Log.i("BATATA", "Advertisement start succeeded.");
//            }
//        });
        startService();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, BeaconService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, BeaconService.class);
        stopService(serviceIntent);
    }
}
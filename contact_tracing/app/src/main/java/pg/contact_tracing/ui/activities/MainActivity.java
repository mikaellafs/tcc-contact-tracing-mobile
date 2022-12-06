package pg.contact_tracing.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Calendar;
import java.util.Date;

import pg.contact_tracing.R;
import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.ApiResult;
import pg.contact_tracing.models.ECSignature;
import pg.contact_tracing.models.Report;
import pg.contact_tracing.repositories.GrpcApiRepository;
import pg.contact_tracing.repositories.UserInformationsRepository;
import pg.contact_tracing.services.managers.MqttContactTracingServiceManager;
import pg.contact_tracing.ui.fragments.ReportDateDialog;
import pg.contact_tracing.ui.fragments.WarningBanner;
import pg.contact_tracing.services.managers.BeaconServiceManager;
import pg.contact_tracing.utils.CryptoManager;
import pg.contact_tracing.utils.NotificationBroadcastCenter;
import pg.contact_tracing.utils.NotificationCreator;
import pg.contact_tracing.utils.UserContactsManager;
import pg.contact_tracing.utils.adapters.ReportAdapter;

public class MainActivity extends AppCompatActivity implements ReportDateDialog.ReportDateDialogListener {
    private static final String MAIN_ACTIVITY_LOG = "MAIN_ACTIVITY";
    private Switch tracingSwitch;
    private TextView tracingTitle;
    private TextView tracingSubtitle;
    private ImageView tracingImage;

    private WarningBanner warningBanner;

    BeaconServiceManager beaconServiceManager;
    MqttContactTracingServiceManager mqttServiceManager;
    NotificationCreator notificationCreator;
    UserContactsManager userContactsManager;

    private final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String bluetoothAdvertisePermission = Manifest.permission.BLUETOOTH_ADVERTISE;
    private final String bluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN;
    private final String bluetoothConnectPermission = Manifest.permission.BLUETOOTH_CONNECT;

    BroadcastReceiver userNotAtRiskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                hideWarningBanner();
            } catch (Exception e) {
                Log.e(MAIN_ACTIVITY_LOG, "Hiding banner failed");
            }
        }
    };

    BroadcastReceiver userAtRiskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showWarningBanner(intent.getStringExtra("message"));
        }
    };

    BroadcastReceiver beaconServiceFailedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mqttServiceManager.stop(MainActivity.this);
            Log.i(MAIN_ACTIVITY_LOG, "Beacon service failed: "+ intent.getStringExtra("message"));

            tracingSwitch.setChecked(false);
            Toast.makeText(context, "Não foi possível iniciar o rastreamento, tente novamente mais tarde", Toast.LENGTH_SHORT).show();
        }
    };


    BroadcastReceiver mqttServiceFailedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(MAIN_ACTIVITY_LOG, "Mqtt service failed: "+ intent.getStringExtra("message"));

            // Schedule try to start later
            mqttServiceManager.retryStartServiceLoop(context);

            Toast.makeText(context, "Rastreamento iniciado parcialmente, verifique sua conexão.", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconServiceManager = new BeaconServiceManager();
        mqttServiceManager = new MqttContactTracingServiceManager();
        notificationCreator = new NotificationCreator();
        userContactsManager = new UserContactsManager();

        tracingTitle = findViewById(R.id.title_is_tracing);
        tracingSubtitle = findViewById(R.id.subtitle_is_tracing);
        tracingImage = findViewById(R.id.tracing_image);
        tracingSwitch = findViewById(R.id.tracing_switch);

        setSwitchAction();
        setReportAction();

        String message = userContactsManager.getBannerMessageIfAtRisk();
        Log.i(MAIN_ACTIVITY_LOG, "Banner message: " + message);
        setWarningBanner(message == null ? "" : message);
        if (message == null) {
            hideWarningBanner();
        }
        

        setReceiversNotification();
    }

    private void setSwitchAction() {
        boolean isTracing = beaconServiceManager.isTracing();
        tracingSwitch.setChecked(isTracing);

        tracingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = getApplicationContext();

                if (isChecked) startTracing(context);
                else stopTracing(context);
            }
        });
    }

    private void setReportAction() {
        Button reportButton = findViewById(R.id.report_infection_button);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDateDialog();
            }
        });
    }

    private void setReceiversNotification() {
        Log.i(MAIN_ACTIVITY_LOG, "Setting broadcast receivers");

        // Listen to event user not at risk - hide banner
        NotificationBroadcastCenter.registerReceiver(this, NotificationBroadcastCenter.Event.NOT_RISK_NOTIFICATION, userNotAtRiskReceiver);

        // Listen to event user at risk - show banner
        NotificationBroadcastCenter.registerReceiver(this, NotificationBroadcastCenter.Event.RISK_NOTIFICATION, userAtRiskReceiver);

        // Listen to event beacon service has failed
        NotificationBroadcastCenter.registerReceiver(this, NotificationBroadcastCenter.Event.BEACON_SERVICE_FAILED, beaconServiceFailedReceiver);

        // Listen to event mqtt service has failed
        NotificationBroadcastCenter.registerReceiver(this, NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED, mqttServiceFailedReceiver);
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
        ft.commitAllowingStateLoss();

        warningBanner.setMessage(message);
    }

    private void setWarningBanner(String message) {
        warningBanner = new WarningBanner(message);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.banner, warningBanner);
        ft.commit();
    }

    private void enableBluetooth() {
        Log.i(MAIN_ACTIVITY_LOG, "Enable bluetooth");
        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        boolean hasBluetoothConnectPermission = ActivityCompat.checkSelfPermission(this, bluetoothConnectPermission) == PackageManager.PERMISSION_GRANTED;
        int sdkVersion = Build.VERSION.SDK_INT;

        if (!btAdapter.isEnabled() && (sdkVersion <= 30 || hasBluetoothConnectPermission)) {
                btAdapter.enable();
            return;
        }
    }

    private boolean checkIfLocationIsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsEnabled || isNetworkEnabled;
    }

    private boolean checkAndRequestLocation() {
        Log.i(MAIN_ACTIVITY_LOG, "Enable location");

        boolean isLocationEnabled = checkIfLocationIsEnabled();
        if (!isLocationEnabled) {
            Log.i(MAIN_ACTIVITY_LOG, "Location is not enabled");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ative a localização");
            builder.setMessage("O aplicativo precisa que a localização esteja ativa para rastrear contatos. Não se preocupe, a sua localização não será compartilhada.");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();

            tracingSwitch.setChecked(false);
            return false;
        }

        return true;
    }

    private boolean checkAndRequestPermissions(Context context) {
        Log.i(MAIN_ACTIVITY_LOG, "Checking permissions");
        ArrayList<String> permissionsNotGranted = new ArrayList<>();
        String[] permissions;

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion > 30) {
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
        boolean isLocationEnabled = checkIfLocationIsEnabled();

        if (!isLocationEnabled) {
            Log.i(MAIN_ACTIVITY_LOG, "Location is not enabled, can't start tracing");
            Toast.makeText(context,"Não é possível iniciar o rastreamento sem ativar a localização",Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTracing(context)) {
            tracingSwitch.setChecked(true);
        }
    }

    public boolean startTracing(Context context) {
        if (!checkAndRequestPermissions(context) || !checkAndRequestLocation()) {
            return false;
        }
        enableBluetooth();

        Log.i(MAIN_ACTIVITY_LOG, "Start tracing...");

        Notification notification = notificationCreator.createNotification(
                context,
                getString(R.string.contact_tracing_service_channel),
                getString(R.string.beacon_service_channel),
                getString(R.string.beacon_notification_title),
                getString(R.string.beacon_notification_subtitle),
                null
        );
        NotificationCreator.foregroundServiceNotification = notification;
        beaconServiceManager.start(context);
        mqttServiceManager.start(context);

        setToTracingMode();
        return true;
    }

    public void stopTracing(Context context) {
        Log.i(MAIN_ACTIVITY_LOG, "Stop tracing...");
        beaconServiceManager.stop(context);
        mqttServiceManager.stop(context);
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

    public void showReportDateDialog() {
        ReportDateDialog reportDateDialog = new ReportDateDialog();
        reportDateDialog.show(getSupportFragmentManager(), "ReportDateDialog");
    }

    private void reportInfection(Date dateStartSymptoms, Date dateDiagnostic) {
        Log.i(MAIN_ACTIVITY_LOG, "Report infection, diagnostic date: " + dateDiagnostic.toString());
        try {
            GrpcApiRepository apiRepo = DI.resolve(GrpcApiRepository.class);
            UserInformationsRepository userInfoRepo = DI.resolve(UserInformationsRepository.class);
            CryptoManager cryptoManager = DI.resolve(CryptoManager.class);

            String id = userInfoRepo.getID();
            Report report = new Report(id, dateStartSymptoms, dateDiagnostic, getNow());
            String reportString = ReportAdapter.toJSONObject(report).toString();

            ECSignature signature = cryptoManager.sign(reportString);
            ApiResult result = apiRepo.reportInfection(report, signature);

            if (result.getCode() != 200) {
                Log.i(MAIN_ACTIVITY_LOG, "Failed to report infection, status " + result.getCode() + ":" + result.getMessage());
                Toast.makeText(MainActivity.this, "Não foi possível reportar. Tente novamente mais tarde", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(MAIN_ACTIVITY_LOG, "Infection reported successfully :" + result.getMessage());
                Toast.makeText(MainActivity.this, "Diagnótisco reportado!", Toast.LENGTH_SHORT).show();
            }
        } catch (InstanceNotRegisteredDIException e) {
            Log.e(MAIN_ACTIVITY_LOG, "Failed to report infection: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Não foi possível reportar. Tente novamente mais tarde", Toast.LENGTH_SHORT).show();
        } catch (UserInformationNotFoundException e) {
            Log.e(MAIN_ACTIVITY_LOG, "Public key not found, must restart app: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Ocorreu um erro, por favor reinicie o app.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(MAIN_ACTIVITY_LOG, "An error ocurred:" + e.getMessage());
            Toast.makeText(MainActivity.this, "Falha na conexão, tente novamente mais tarde", Toast.LENGTH_SHORT).show();
        }
    }

    private Date getNow() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    public void onReportDialogPositiveClick(ReportDateDialog dialog) {
        Log.i(MAIN_ACTIVITY_LOG, "Report date dialog positive click");
        Calendar start = dialog.getStartDate();
        Calendar diagnostic = dialog.getDiagnosticDate();

        if (start == null || diagnostic == null ) {
            Log.e(MAIN_ACTIVITY_LOG, "Date diagnostic or date start is null");
            Toast.makeText(this, "Preencha todas as datas", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(MAIN_ACTIVITY_LOG, "Start symptoms date: " + start.getTime().toString() + ", diagnostic date: " + diagnostic.getTime());
        reportInfection(start.getTime(), diagnostic.getTime());
        dialog.dismiss();
    }

    @Override
    public void onReportDialogNegativeClick(ReportDateDialog dialog) {
        Log.i(MAIN_ACTIVITY_LOG, "Report date dialog negative click");
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userAtRiskReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userNotAtRiskReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(beaconServiceFailedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mqttServiceFailedReceiver);
        super.onDestroy();
    }
}
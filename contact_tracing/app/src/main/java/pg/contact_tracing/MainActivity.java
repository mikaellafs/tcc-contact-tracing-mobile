package pg.contact_tracing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import pg.contact_tracing.presentation.MainViewModel;
import pg.contact_tracing.presentation.fragments.WarningBanner;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private Switch tracingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.viewModel = new MainViewModel(this);

        tracingSwitch = findViewById(R.id.tracing_switch);
        tracingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = getApplicationContext();

                if (isChecked) {
                    if (!viewModel.startTracing(context)) tracingSwitch.setChecked(false);
                } else viewModel.stopTracing(context);
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.banner, new WarningBanner());
        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Context context = getApplicationContext();
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(context, locationPermission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Não é possível iniciar o rastreamento sem a permissão",Toast.LENGTH_SHORT).show();
        } else {
            if (viewModel.startTracing(context)) {
                tracingSwitch.setChecked(true);
            }
        }
    }
}
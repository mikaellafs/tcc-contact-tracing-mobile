package pg.contact_tracing;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import pg.contact_tracing.presentation.MainViewModel;
import pg.contact_tracing.presentation.fragments.WarningBanner;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.viewModel = new MainViewModel(this);

        Switch tracingSwitch = (Switch) findViewById(R.id.tracing_switch);
        tracingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = getApplicationContext();

                if (isChecked) viewModel.startTracing(context);
                else viewModel.stopTracing(context);
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.banner, new WarningBanner());
        ft.commit();
    }
}
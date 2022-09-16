package pg.contact_tracing.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import pg.contact_tracing.R;
import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.ECSignature;
import pg.contact_tracing.repositories.UserInformationsRepository;

public class LauchingActivity extends AppCompatActivity {
    static private final String LAUCHING_ACTIVITY_LOG = "LAUCHING_ACTIVITY";
    static private final int LAUCHING_SCREEN_DELAY= 2000; //2 seconds

    private UserInformationsRepository userInformationsRepository;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lauching);

        button = findViewById(R.id.register_button);

        try {
            userInformationsRepository = DI.resolve(UserInformationsRepository.class);
        } catch(InstanceNotRegisteredDIException e) {
            Log.e(LAUCHING_ACTIVITY_LOG, "User information repository not registered");
            System.exit(1);
        }

        new Timer().schedule(new TimerTask(){
            @Override
            public void run(){
                registerOrHomeScreen();
            }
        }, LAUCHING_SCREEN_DELAY);
    }

    private void registerOrHomeScreen() {
        registerUser();
        // Check if there's a key pair saved in app memory
        try {
            userInformationsRepository.getPublicKey();
            userInformationsRepository.getPrivateKey();
            Log.i(LAUCHING_ACTIVITY_LOG, "Key pair found");

            // TODO: Check if user is registered (server pk is saved)

            goToHomeScreen();
        } catch (UserInformationNotFoundException e) {
            Log.i(LAUCHING_ACTIVITY_LOG, "Key pair NOT found");
            runOnUiThread(() -> setToRegisterScreen());
        }
    }

    private void setToRegisterScreen() {
        Log.i(LAUCHING_ACTIVITY_LOG, "Set to register screen");
        TextView title = findViewById(R.id.lauching_title);
        TextView subtitle = findViewById(R.id.lauching_subtitle);
        ConstraintLayout button_layout = findViewById(R.id.lauching_button_layout);

        title.setText(R.string.lauching_title_register);
        subtitle.setText(R.string.lauching_subtitle_register);

        // Set button action
        button.setOnClickListener(v -> {
            Log.i(LAUCHING_ACTIVITY_LOG, "Register button tapped, create a key pair.");
            // block actions and show loading in button
            button.setClickable(false);

            registerUser();
        });

        button.setVisibility(View.VISIBLE);
        button_layout.setVisibility(View.VISIBLE);
    }

    private void registerUser() {

    }

    private void goToHomeScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
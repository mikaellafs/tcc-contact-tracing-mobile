package pg.contact_tracing.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;

import pg.contact_tracing.R;
import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.ApiResult;
import pg.contact_tracing.models.ECSignature;
import pg.contact_tracing.models.User;
import pg.contact_tracing.repositories.GrpcApiRepository;
import pg.contact_tracing.repositories.UserInformationsRepository;
import pg.contact_tracing.utils.CryptoManager;

public class LauchingActivity extends AppCompatActivity {
    static private final String LAUCHING_ACTIVITY_LOG = "LAUCHING_ACTIVITY";
    static private final int LAUCHING_SCREEN_DELAY= 2000; //2 seconds

    private UserInformationsRepository userInformationsRepository;
    private GrpcApiRepository apiRepository;
    private CryptoManager cryptoManager;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lauching);

        button = findViewById(R.id.register_button);

        try {
            userInformationsRepository = DI.resolve(UserInformationsRepository.class);
            cryptoManager = DI.resolve(CryptoManager.class);
            apiRepository = DI.resolve(GrpcApiRepository.class);
        } catch(InstanceNotRegisteredDIException e) {
            Log.e(LAUCHING_ACTIVITY_LOG, e.getMessage());
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
        button.setOnClickListener(v -> registerButtonAction());

        button.setVisibility(View.VISIBLE);
        button_layout.setVisibility(View.VISIBLE);
    }

    private void registerButtonAction() {
        Log.i(LAUCHING_ACTIVITY_LOG, "Register button tapped, create a key pair.");
        button.setClickable(false);
        // TODO: SHOW LOADING BUTTON

        // TODO: ASK PASSWORD
        try {
            ApiResult result = registerUser("");
            // TODO: REMOVE LOADING BUTTON

            if (result.getCode() != 200) {
                Toast.makeText(getApplicationContext(), result.getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }

            goToHomeScreen();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
            userInformationsRepository.clearInfos();
            Toast.makeText(getApplicationContext(),"Este dispositivo não suporta os recursos necessários para começar.",Toast.LENGTH_SHORT).show();
        } catch (UserInformationNotFoundException | InvalidKeyException e) {
            userInformationsRepository.clearInfos();
            Toast.makeText(getApplicationContext(),"Algo deu errado, tente novamente mais tarde.",Toast.LENGTH_SHORT).show();
        }
    }

    private ApiResult registerUser(String password)
            throws UserInformationNotFoundException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        String pk = cryptoManager.generateKeyPair();
        String id = userInformationsRepository.getUUID();

        User user = new User(id, pk);
        ECSignature signature = cryptoManager.sign(id);
        return apiRepository.registerUser(user, signature, password);
    }

    private void goToHomeScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
package pg.contact_tracing.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import pg.contact_tracing.ui.fragments.PasswordDialog;
import pg.contact_tracing.utils.CryptoManager;
import pg.contact_tracing.utils.adapters.UserAdapter;

public class LauchingActivity extends AppCompatActivity {
    static private final String LAUCHING_ACTIVITY_LOG = "LAUCHING_ACTIVITY";
    static private final int LAUCHING_SCREEN_DELAY= 2000; //2 seconds

    private UserInformationsRepository userInformationsRepository;
    private GrpcApiRepository apiRepository;
    private CryptoManager cryptoManager;

    private Button button;
    ConstraintLayout button_layout;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lauching);

        button = findViewById(R.id.register_button);
        loading = findViewById(R.id.register_progress_bar);

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
                // goToHomeScreen();
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

            // Check if user is registered
            userInformationsRepository.getServerPublicKey();
            Log.i(LAUCHING_ACTIVITY_LOG, "Server pk found");

            goToHomeScreen();
        } catch (UserInformationNotFoundException e) {
            Log.i(LAUCHING_ACTIVITY_LOG, "Key not found: " + e.getMessage());
            runOnUiThread(() -> setToRegisterScreen());
        }
    }

    private void setToRegisterScreen() {
        Log.i(LAUCHING_ACTIVITY_LOG, "Set to register screen");
        TextView title = findViewById(R.id.lauching_title);
        TextView subtitle = findViewById(R.id.lauching_subtitle);
        button_layout = findViewById(R.id.lauching_button_layout);

        title.setText(R.string.lauching_title_register);
        subtitle.setText(R.string.lauching_subtitle_register);

        // Set button action
        button.setOnClickListener(v -> registerButtonAction());

        button.setVisibility(View.VISIBLE);
        button_layout.setVisibility(View.VISIBLE);
    }

    private void registerButtonAction() {
        Log.i(LAUCHING_ACTIVITY_LOG, "Register button tapped, create a key pair.");
        runOnUiThread(() -> showLoading());
        try {
            ApiResult result = registerUser();

            if (result.getCode() != 200) {
                Toast.makeText(getApplicationContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            userInformationsRepository.saveID(result.getuserId());
            goToHomeScreen();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
            userInformationsRepository.clearInfos();
            Log.e(LAUCHING_ACTIVITY_LOG, e.getMessage());
            Toast.makeText(getApplicationContext(),"Este dispositivo não suporta os recursos necessários para começar.",Toast.LENGTH_SHORT).show();
        } catch (UserInformationNotFoundException | InvalidKeyException e) {
            userInformationsRepository.clearInfos();
            Log.e(LAUCHING_ACTIVITY_LOG, e.getMessage());
            Toast.makeText(getApplicationContext(),"Algo deu errado, tente novamente mais tarde.",Toast.LENGTH_SHORT).show();
        } catch (io.grpc.StatusRuntimeException e) {
            userInformationsRepository.clearInfos();
            Log.e(LAUCHING_ACTIVITY_LOG, "Grpc Runtime Exception: " + e.getMessage());
            Toast.makeText(getApplicationContext(),"Falha na conexão com servidor, tente novamente mais tarde.",Toast.LENGTH_SHORT).show();
        } finally {
            runOnUiThread(() ->hideLoading());
        }
    }

    private ApiResult registerUser()
            throws UserInformationNotFoundException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        byte[] pkBytes = cryptoManager.generateKeyPair();
//        String pk = Hex.encodeHexString(pkBytes);
        String pk = new String(Hex.encodeHex(pkBytes));
        Log.i(LAUCHING_ACTIVITY_LOG, "Pk hexadecimal: " + pk);

        String id = userInformationsRepository.getDeviceID();

        User user = new User(id, pk);
        return apiRepository.registerUser(user);
    }

    private void goToHomeScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void showLoading() {
        button.setClickable(false);
        button_layout.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        button.setClickable(true);
        button_layout.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }
}
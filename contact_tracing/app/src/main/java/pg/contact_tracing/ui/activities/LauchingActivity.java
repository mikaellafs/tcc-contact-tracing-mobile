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

public class LauchingActivity extends AppCompatActivity implements PasswordDialog.PasswordDialogListener {
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
        button.setOnClickListener(v -> showPasswordDialog());

        button.setVisibility(View.VISIBLE);
        button_layout.setVisibility(View.VISIBLE);
    }

    private void registerButtonAction(String password) {
        Log.i(LAUCHING_ACTIVITY_LOG, "Register button tapped, create a key pair.");
        showLoading();
        try {
            ApiResult result = registerUser(password);

            if (result.getCode() != 200) {
                Toast.makeText(getApplicationContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            userInformationsRepository.saveServerPublicKey(result.getServerPk());
            goToHomeScreen();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
            userInformationsRepository.clearInfos();
            Toast.makeText(getApplicationContext(),"Este dispositivo não suporta os recursos necessários para começar.",Toast.LENGTH_SHORT).show();
        } catch (UserInformationNotFoundException | InvalidKeyException e) {
            userInformationsRepository.clearInfos();
            Toast.makeText(getApplicationContext(),"Algo deu errado, tente novamente mais tarde.",Toast.LENGTH_SHORT).show();
        } catch (io.grpc.StatusRuntimeException e) {
            userInformationsRepository.clearInfos();
            Toast.makeText(getApplicationContext(),"Falha na conexão com servidor, tente novamente mais tarde.",Toast.LENGTH_SHORT).show();
        } finally {
            hideLoading();
        }
    }

    private ApiResult registerUser(String password)
            throws UserInformationNotFoundException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        String pk = cryptoManager.generateKeyPair();
        String id = userInformationsRepository.getID();

        User user = new User(id, pk, password);
        String userJsonStr = UserAdapter.toJSONObject(user).toString();

        ECSignature signature = cryptoManager.sign(userJsonStr);
        return apiRepository.registerUser(user, signature);
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

    // Password dialog

    public void showPasswordDialog() {
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.show(getSupportFragmentManager(), "PasswordDialog");
    }

    @Override
    public void onPasswordDialogPositiveClick(PasswordDialog dialog) {
        Log.i(LAUCHING_ACTIVITY_LOG, "Password dialog register click");

        boolean isValid = validateFields(dialog);
        if (!isValid) return;

        registerButtonAction(dialog.getPassword());
        dialog.dismiss();
    }

    @Override
    public void onPasswordDialogNegativeClick(PasswordDialog dialog) {
        Log.i(LAUCHING_ACTIVITY_LOG, "Password dialog negative click");
    }

    private boolean validateFields(PasswordDialog dialog) {
        String password = dialog.getPassword();
        String repeatPassword = dialog.getRepeatPassword();

        if (!password.equals(repeatPassword)) {
            dialog.setPrompt(R.string.password_dialog_prompt_different_password);
            dialog.showPrompt();
            Log.i(LAUCHING_ACTIVITY_LOG, "Different password inputs");
            return false;
        }

        if (password.length() < 6 || password.length() > 16) {
            dialog.setPrompt(R.string.password_dialog_prompt_password_size);
            dialog.showPrompt();
            Log.i(LAUCHING_ACTIVITY_LOG, "Password too short or too long");
            return false;
        }

        Log.i(LAUCHING_ACTIVITY_LOG, "Password validated");
        dialog.hidePrompt();
        return true;
    }
}
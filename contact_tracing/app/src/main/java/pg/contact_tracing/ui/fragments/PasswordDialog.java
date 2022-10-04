package pg.contact_tracing.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import pg.contact_tracing.R;

public class PasswordDialog extends DialogFragment {
    public interface PasswordDialogListener {
        void onPasswordDialogPositiveClick(PasswordDialog dialog);
        void onPasswordDialogNegativeClick(PasswordDialog dialog);
    }

    private EditText passwordField;
    private EditText repeatPasswordField;
    private TextView prompt;

    PasswordDialogListener listener;

    public PasswordDialog() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_dialog, container, false);

        passwordField = view.findViewById(R.id.password_textfield);
        repeatPasswordField = view.findViewById(R.id.repeat_password_textfield);
        prompt = view.findViewById(R.id.password_prompt);

        Button continueButton = view.findViewById(R.id.continue_register_button);
        continueButton.setOnClickListener(v -> listener.onPasswordDialogPositiveClick(this));

        Button cancelButton = view.findViewById(R.id.cancel_register_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               listener.onPasswordDialogNegativeClick(PasswordDialog.this);
               dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the PasswordDialogListener so we can send events to the host
            listener = (PasswordDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Activity must implement PasswordDialogListener");
        }
    }

    public String getPassword() {
        return passwordField.getText().toString();
    }

    public String getRepeatPassword() {
        return repeatPasswordField.getText().toString();
    }

    public void setPrompt(String message) {
        prompt.setText(message);
    }

    public void setPrompt(int id) {
        String message = getResources().getString(id);
        prompt.setText(message);
    }

    public void showPrompt() {
        prompt.setVisibility(View.VISIBLE);
    }

    public void hidePrompt() {
        prompt.setVisibility(View.GONE);
    }
}
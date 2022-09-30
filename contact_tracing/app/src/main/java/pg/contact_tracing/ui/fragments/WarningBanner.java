package pg.contact_tracing.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pg.contact_tracing.R;

public class WarningBanner extends Fragment {
    TextView subtitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subtitle = getView().findViewById(R.id.warning_subtitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.warning_banner, container, false);
    }

    public void setMessage(String message) {
        subtitle.setText(message);
    }
}
package pg.contact_tracing.presentation;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import pg.contact_tracing.R;
import pg.contact_tracing.domain.usecases.BeaconServiceUseCase;
import pg.contact_tracing.domain.usecases.CheckPreRequisitesUseCase;

public class MainViewModel extends ViewModel {
    final private TextView tracingTitle;
    final private TextView tracingSubtitle;
    final private ImageView tracingImage;

    final private BeaconServiceUseCase beaconServiceUseCase;
    final private CheckPreRequisitesUseCase checkPreRequisitesUseCase;

    public MainViewModel(Activity screen) {
        beaconServiceUseCase = new BeaconServiceUseCase();
        checkPreRequisitesUseCase = new CheckPreRequisitesUseCase(screen);

        tracingTitle = screen.findViewById(R.id.title_is_tracing);
        tracingSubtitle = screen.findViewById(R.id.subtitle_is_tracing);
        tracingImage = screen.findViewById(R.id.tracing_image);

        // TODO: check if service is running
        // if running {
        // setToTracingMode()
        // else setToNotTracingMode()
    }

    public boolean startTracing(Context context) {
        if (!checkPreRequisitesUseCase.checkAndAskLocationPermission(context)) {
            return false;
        }
        checkPreRequisitesUseCase.checkAndAskBluetooth(context);

        beaconServiceUseCase.start(context);
        setToTracingMode();
        return true;
    }

    public void stopTracing(Context context) {
        beaconServiceUseCase.stop(context);
        setToNotTracingMode();
    }

    private void setToTracingMode() {
        tracingTitle.setText(R.string.main_tracing_active_title);
        tracingSubtitle.setText(R.string.main_tracing_active_subtitle);
        tracingImage.setBackgroundResource(R.drawable.illustration_tracing);
    }

    private void setToNotTracingMode() {
        tracingTitle.setText(R.string.main_tracing_inactive_title);
        tracingSubtitle.setText(R.string.main_tracing_inactive_subtitle);
        tracingImage.setBackgroundResource(R.drawable.illustration_not_tracing);
    }
}
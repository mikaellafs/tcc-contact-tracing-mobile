package pg.contact_tracing.presentation;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModel;

import pg.contact_tracing.R;
import pg.contact_tracing.domain.usecases.BeaconServiceUseCase;

public class MainViewModel extends ViewModel {
    private TextView tracingTitle;
    private TextView tracingSubtitle;
    private ImageView tracingImage;

    private BeaconServiceUseCase beaconServiceUseCase;

    public MainViewModel(Activity screen) {
        beaconServiceUseCase = new BeaconServiceUseCase();

        tracingTitle = (TextView) screen.findViewById(R.id.title_is_tracing);
        tracingSubtitle = (TextView) screen.findViewById(R.id.subtitle_is_tracing);
        tracingImage = (ImageView) screen.findViewById(R.id.tracing_image);

        // TODO: check if service is running
        // if running {
        // setToTracingMode()
        // else setToNotTracingMode()
    }

    public void startTracing(Context context) {
        beaconServiceUseCase.start(context);
        setToTracingMode();
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
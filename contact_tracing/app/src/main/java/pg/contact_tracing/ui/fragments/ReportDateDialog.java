package pg.contact_tracing.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import pg.contact_tracing.R;

public class ReportDateDialog extends DialogFragment {
    public interface ReportDateDialogListener {
        void onReportDialogPositiveClick(ReportDateDialog dialog);
        void onReportDialogNegativeClick(ReportDateDialog dialog);
    }

    private static final String REPORT_DATE_DIALOG_LOG = "REPORT_DATE_DIALOG";

    private TextView startDateField;
    private TextView diagnosticDateField;
    private TextView prompt;

    private Calendar startDate;
    private Calendar diagnosticDate;

    private ReportDateDialogListener listener;

    DatePickerDialog.OnDateSetListener listenerStart = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (!isDateValid(year, month, day)) {
                Log.i(REPORT_DATE_DIALOG_LOG, "A data de início dos sintomas não pode ser no futuro.");
                showPromptErrorMessage(getString(R.string.report_infection_dialog_prompt_start));
                return;
            }

            hidePromptErrorMessage();
            startDate.set(Calendar.YEAR, year);
            startDate.set(Calendar.MONTH,month);
            startDate.set(Calendar.DAY_OF_MONTH,day);
            updateLabel(startDateField, startDate);
        }
    };

    DatePickerDialog.OnDateSetListener listenerDiagnostic = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (!isDateValid(year, month, day)) {
                Log.i(REPORT_DATE_DIALOG_LOG, "A data do diagnóstico não pode ser no futuro.");
                showPromptErrorMessage(getString(R.string.report_infection_dialog_prompt_diagnostic));
                return;
            }

            hidePromptErrorMessage();
            diagnosticDate.set(Calendar.YEAR, year);
            diagnosticDate.set(Calendar.MONTH,month);
            diagnosticDate.set(Calendar.DAY_OF_MONTH,day);
            updateLabel(diagnosticDateField, diagnosticDate);
        }
    };

    public ReportDateDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_date_dialog, container, false);
        startDateField = view.findViewById(R.id.start_symp_date_field);
        diagnosticDateField = view.findViewById(R.id.diagnostic_date_field);
        prompt = view.findViewById(R.id.report_prompt);

        startDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDate = Calendar.getInstance();
                new DatePickerDialog(getContext(), listenerStart, startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        diagnosticDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diagnosticDate = Calendar.getInstance();
                new DatePickerDialog(getContext(), listenerDiagnostic, diagnosticDate.get(Calendar.YEAR), diagnosticDate.get(Calendar.MONTH), diagnosticDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Button continueButton = view.findViewById(R.id.continue_report_button);
        continueButton.setOnClickListener(v -> listener.onReportDialogPositiveClick(this));

        Button cancelButton = view.findViewById(R.id.cancel_report_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listener.onReportDialogNegativeClick(ReportDateDialog.this);
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
            listener = (ReportDateDialog.ReportDateDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Activity must implement ReportDateDialogListener");
        }
    }

    private void updateLabel(TextView editText, Calendar date){
        String myFormat="dd/MM/yy";
        DateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(dateFormat.format(date.getTime()));
    }

    private boolean isDateValid(int year, int month, int day) {
        Calendar current = Calendar.getInstance();
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        return !current.before(selectedDate);
    }

    private void showPromptErrorMessage(String message) {
        prompt.setText(message);
        prompt.setVisibility(View.VISIBLE);
    }

    private void hidePromptErrorMessage() {
        prompt.setVisibility(View.GONE);
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public Calendar getDiagnosticDate() {
        return diagnosticDate;
    }
}
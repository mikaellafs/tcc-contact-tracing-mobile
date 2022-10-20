package pg.contact_tracing.utils.adapters;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import pg.contact_tracing.models.Report;

public class ReportAdapter {
    public static JSONObject toJSONObject(Report report) {
        JSONObject reportJSON = new JSONObject();
        
        try{
            reportJSON.put("userId", report.getUserId());
            reportJSON.put("dateStartSymptoms", report.getDateStartSymptoms().getTime());
            reportJSON.put("dateDiagnostic", report.getDateDiagnostic().getTime());
            reportJSON.put("dateReport", report.getDateReport().getTime());
        }catch (JSONException e) {
            Log.e("REPORT_ADAPTER","Failed to parse contact as json");
        }

        return reportJSON;
    }
}

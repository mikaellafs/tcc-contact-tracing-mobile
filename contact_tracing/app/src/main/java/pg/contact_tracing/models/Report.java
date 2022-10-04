package pg.contact_tracing.models;

import java.util.Date;

public class Report {
    private String userId;
    private Date dateStartSymptoms;
    private Date dateDiagnostic;
    private Date dateReport;

    public Report(String userId, Date dateStartSymptoms, Date dateDiagnostic, Date dateReport) {
        this.userId = userId;
        this.dateStartSymptoms = dateStartSymptoms;
        this.dateDiagnostic = dateDiagnostic;
        this.dateReport = dateReport;
    }

    public Date getDateStartSymptoms() {
        return dateStartSymptoms;
    }

    public Date getDateDiagnostic() {
        return dateDiagnostic;
    }

    public Date getDateReport() {
        return dateReport;
    }

    public String getUserId() {
        return userId;
    }
}

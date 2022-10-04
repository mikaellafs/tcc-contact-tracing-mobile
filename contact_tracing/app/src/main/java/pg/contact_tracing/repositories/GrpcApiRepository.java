package pg.contact_tracing.repositories;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.util.Date;

import pg.contact_tracing.models.ApiResult;
import pg.contact_tracing.models.ECSignature;
import pg.contact_tracing.models.Report;
import pg.contact_tracing.models.User;
import pg.contact_tracing.services.grpc.GrpcService;
import pg.contact_tracing.services.grpc.RegisterResult;
import pg.contact_tracing.services.grpc.ReportResult;

public class GrpcApiRepository {
    private static final String GRPC_API_REPOSITORY_LOG = "GRPC_API_REPOSITORY";
    GrpcService api;

    public GrpcApiRepository() {
        api = new GrpcService();
        api.createStubs();
    }

    public ApiResult registerUser(User user, ECSignature signature) {
        Log.i(GRPC_API_REPOSITORY_LOG, "Register user: " + user.toString() + "\nSignature: " + signature.toString());

        RegisterResult result = api.registerUser(
                user.getId(),
                user.getPublicKey(),
                user.getPassword(),
                ByteString.copyFrom(signature.getSignature())
        );

        Log.i(GRPC_API_REPOSITORY_LOG, "Register user api call result: "+ result.getStatus() + " - " + result.getMessage());
        return new ApiResult(result.getStatus(), result.getMessage(), result.getServerPk());
    }

    public ApiResult reportInfection(Report report, ECSignature signature) {
        Log.i(GRPC_API_REPOSITORY_LOG, "Report infection: startSymptoms at "
                + report.getDateStartSymptoms().toString()
                + " and diagnosed at " + report.getDateDiagnostic().toString());

        ReportResult result = api.reportInfection(
                report.getUserId(),
                report.getDateStartSymptoms().getTime(),
                report.getDateDiagnostic().getTime(),
                report.getDateDiagnostic().getTime(),
                ByteString.copyFrom(signature.getSignature()));

        Log.i(GRPC_API_REPOSITORY_LOG, "Report infection api call result: "+ result.getStatus() + " - " + result.getMessage());
        return new ApiResult(result.getStatus(), result.getMessage());
    }
}

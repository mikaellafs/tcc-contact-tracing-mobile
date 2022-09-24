package pg.contact_tracing.repositories;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.util.Date;

import pg.contact_tracing.models.ApiResult;
import pg.contact_tracing.models.ECSignature;
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

    public ApiResult registerUser(User user, ECSignature signature, String password) {
        Log.i(GRPC_API_REPOSITORY_LOG, "Register user: " + user.toString() + "\nSignature: " + signature.toString());

        RegisterResult result = api.registerUser(user.getId(),
                user.getPublicKey(),
                ByteString.copyFrom(signature.getSignature()),
                new String(signature.getMessage()),
                password);

        Log.i(GRPC_API_REPOSITORY_LOG, "Register user api call result: "+ result.getStatus() + " - " + result.getMessage());
        return new ApiResult(result.getStatus(), result.getMessage(), result.getServerPk());
    }

    public ApiResult reportInfection(User user, ECSignature signature, Date dateStartSymptoms, Date dateDiagnostic) {
        Log.i(GRPC_API_REPOSITORY_LOG, "Report infection: startSymptoms at " + dateStartSymptoms.toString() + " and diagnosed at " + dateDiagnostic.toString());
        ReportResult result = api.reportInfection(
                user.getId(),
                user.getPublicKey(),
                ByteString.copyFrom(signature.getSignature()),
                new String(signature.getMessage()),
                dateStartSymptoms.getTime(),
                dateDiagnostic.getTime(),
                new Date().getTime());

        Log.i(GRPC_API_REPOSITORY_LOG, "Report infection api call result: "+ result.getStatus() + " - " + result.getMessage());
        return new ApiResult(result.getStatus(), result.getMessage());
    }
}

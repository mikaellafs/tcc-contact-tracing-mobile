package pg.contact_tracing.services.grpc;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcService {
    private static final String GRPC_SERVICE_LOG = "GRPC_SERVICE";
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    ContactTracingGrpc.ContactTracingBlockingStub blockingStub;

    public void createStubs() {
        Log.i(GRPC_SERVICE_LOG, "Creating GRPC stubs");
        ManagedChannel mChannel = ManagedChannelBuilder.forAddress(HOST, PORT).useTransportSecurity().build();

        blockingStub = ContactTracingGrpc.newBlockingStub(mChannel);
    }

    public RegisterResult registerUser(String id, String publicKey, ByteString sig, String messageSigned, String password) {
        Log.i(GRPC_SERVICE_LOG, "Register user: " + id);
        User user = User.newBuilder()
                .setId(id)
                .setPublicKey(publicKey)
                .build();
        Signature signature = Signature.newBuilder()
                .setSignature(sig)
                .setMessage(messageSigned)
                .build();

        RegisterRequest request = RegisterRequest.newBuilder()
                .setUser(user)
                .setSignature(signature)
                .setPassword(password)
                .build();

        return blockingStub.register(request);
    }

    public ReportResult reportInfection(
            String id,
            String publicKey,
            ByteString sig,
            String messageSigned,
            long dateStartMillis,
            long dateDiagnosticMillis,
            long dateReportMillis
    ) {
        Log.i(GRPC_SERVICE_LOG, "Report infection: " + id);
        User user = User.newBuilder()
                .setId(id)
                .setPublicKey(publicKey)
                .build();
        Signature signature = Signature.newBuilder()
                .setSignature(sig)
                .setMessage(messageSigned)
                .build();

        Timestamp startSympt = Timestamp.newBuilder()
                .setSeconds(dateStartMillis / 1000)
                .setNanos((int) ((dateStartMillis % 1000) * 1000000))
                .build();

        Timestamp diagnosticDate = Timestamp.newBuilder()
                .setSeconds(dateDiagnosticMillis / 1000)
                .setNanos((int) ((dateDiagnosticMillis % 1000) * 1000000))
                .build();

        Timestamp reportDate = Timestamp.newBuilder()
                .setSeconds(dateReportMillis / 1000)
                .setNanos((int) ((dateReportMillis % 1000) * 1000000))
                .build();

        ReportRequest request = ReportRequest.newBuilder()
                .setUser(user)
                .setSignature(signature)
                .setDateStartSymptoms(startSympt)
                .setDateDiagnostic(diagnosticDate)
                .setDateReport(reportDate)
                .build();

        return blockingStub.reportInfection(request);
    }
}

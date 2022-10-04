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

    public RegisterResult registerUser(String id, String publicKey, String password, ByteString sig) {
        Log.i(GRPC_SERVICE_LOG, "Register user: " + id);

        Register register = Register.newBuilder()
                .setUserId(id)
                .setPk(publicKey)
                .setPassword(password)
                .build();

        RegisterRequest request = RegisterRequest.newBuilder()
                .setRegister(register)
                .setSignature(sig)
                .build();

        return blockingStub.register(request);
    }

    public ReportResult reportInfection(
            String id,
            long dateStartMillis,
            long dateDiagnosticMillis,
            long dateReportMillis,
            ByteString sig
    ) {
        Log.i(GRPC_SERVICE_LOG, "Report infection: " + id);

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

        Report report = Report.newBuilder()
                .setUserId(id)
                .setDateReport(reportDate)
                .setDateDiagnostic(diagnosticDate)
                .setDateStartSymptoms(startSympt)
                .build();

        ReportRequest request = ReportRequest.newBuilder()
                .setReport(report)
                .setSignature(sig)
                .build();

        return blockingStub.reportInfection(request);
    }
}

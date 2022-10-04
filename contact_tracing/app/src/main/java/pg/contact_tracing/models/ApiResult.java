package pg.contact_tracing.models;

public class ApiResult {
    private int code;
    private String message;
    private String serverPk;

    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResult(int code, String message, String serverPk) {
        this.code = code;
        this.message = message;
        this.serverPk = serverPk;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getServerPk() {
        return serverPk;
    }
}

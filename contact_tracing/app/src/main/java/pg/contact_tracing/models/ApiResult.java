package pg.contact_tracing.models;

public class ApiResult {
    private int code;
    private String message;

    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

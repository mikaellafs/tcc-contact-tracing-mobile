package pg.contact_tracing.models;

public class ApiResult {
    private int code;
    private String message;
    private String userId;

    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResult(int code, String message, String userId) {
        this.code = code;
        this.message = message;
        this.userId = userId;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getuserId() {
        return userId;
    }
}

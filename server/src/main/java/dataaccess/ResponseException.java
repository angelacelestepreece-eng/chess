package dataaccess;

public class ResponseException extends Exception {

    public enum Code {
        BadRequest,
        Unauthorized,
        Forbidden,
        NotFound,
        Conflict,
        ServerError
    }

    private final Code code;

    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}

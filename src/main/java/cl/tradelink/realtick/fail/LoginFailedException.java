package cl.tradelink.realtick.fail;

public class LoginFailedException extends Exception {
    public LoginFailedException() {
        super("Login failed");
    }

    public LoginFailedException(String message) {
        super(message);
    }

    public LoginFailedException(String message, Exception innerEx) {
        super(message, innerEx);
    }
}

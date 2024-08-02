package cl.tradelink.realtick.fail;

public class SessionNotFoundException extends Exception {
    public SessionNotFoundException() {
        super("Session not found.");
    }

    public SessionNotFoundException(String message) {
        super(message);
    }
}
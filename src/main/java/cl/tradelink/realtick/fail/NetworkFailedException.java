package cl.tradelink.realtick.fail;

public class NetworkFailedException extends Exception {
    public NetworkFailedException() {
        super("Network lost");
    }

    public NetworkFailedException(String message) {
        super(message);
    }

    public NetworkFailedException(String message, Exception innerEx) {
        super(message, innerEx);
    }
}

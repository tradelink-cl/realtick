package cl.tradelink.realtick.fail;

public class RequestFailedException extends Exception {
    public RequestFailedException() {
        super("Request failed");
    }

     public RequestFailedException(String message) {
        super(message);
    }
    public RequestFailedException(String message, Exception innerEx) {
        super(message, innerEx);
    }
}

package cl.tradelink.realtick.fail;

public class ServerNotAvailableException extends Exception{
    public ServerNotAvailableException() {
        super("Server not responding");
    }

    public ServerNotAvailableException(String message) {
        super(message);
    }

    public ServerNotAvailableException(String message, Exception innerEx) {
        super(message, innerEx);
    }
}

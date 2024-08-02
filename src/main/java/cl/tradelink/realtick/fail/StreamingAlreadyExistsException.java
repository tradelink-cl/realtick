package cl.tradelink.realtick.fail;

public class StreamingAlreadyExistsException extends Exception{
    public StreamingAlreadyExistsException() {
        super("Streaming already exists");
    }

     public StreamingAlreadyExistsException(String message) {
        super(message);
    }
    public StreamingAlreadyExistsException(String message, Exception innerEx) {
        super(message, innerEx);
    }
}

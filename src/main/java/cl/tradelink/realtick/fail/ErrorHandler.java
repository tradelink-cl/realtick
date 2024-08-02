package cl.tradelink.realtick.fail;

public interface ErrorHandler {
    void onError(Throwable error);
}
package exemplo;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.StreamObserver;
import realtick.grpc.*;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MarketDataClient {

    private String password = "test123";
    private String server = "EMSUATXAPI.taltrade.com";
    private String user = "USER13";
    private String domain = "XAPIDEMO";
    private String locale = "Inhouse Americas";
    private int port = Integer.parseInt("9000");

    private static final Logger logger = Logger.getLogger(MarketDataClient.class.getName());
    private final ManagedChannel channel;
    private final UtilityServicesGrpc.UtilityServicesBlockingStub blockingStub;
    private final MarketDataServiceGrpc.MarketDataServiceStub asyncStub;

    public MarketDataClient(String host, int port) throws SSLException {


        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1))
                .build();

        channel = NettyChannelBuilder.forAddress(server, port)
                .sslContext(sslContext)
                .build();

        blockingStub = UtilityServicesGrpc.newBlockingStub(channel);
        asyncStub = MarketDataServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void run() {
        try {

            ConnectRequest request = ConnectRequest.newBuilder()
                    .setUserName("your_username")
                    .setDomain("your_domain")
                    .setPassword("your_password")
                    .setLocale("your_locale")
                    .build();

            ConnectResponse response = blockingStub.connect(request);
            logger.info("Connect result: " + response.getResponse());

            if ("success".equals(response.getResponse())) {
                String userToken = response.getUserToken();

                Level1MarketDataRequest mdRequest = Level1MarketDataRequest.newBuilder()
                        .addSymbols("TSLA")
                        .setRequest(true)
                        .setAdvise(true)
                        .setUserToken(userToken)
                        .build();

                CountDownLatch finishLatch = new CountDownLatch(1);

                StreamObserver<Level1MarketDataResponse> responseObserver = new StreamObserver<Level1MarketDataResponse>() {
                    @Override
                    public void onNext(Level1MarketDataResponse response) {
                        logger.info("Received market data: " + response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (Status.fromThrowable(t).getCode() == Status.CANCELLED.getCode()) {
                            logger.info("Stream cancelled");
                        } else {
                            logger.log(Level.WARNING, "Encountered error in stream", t);
                        }
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Stream completed");
                        finishLatch.countDown();
                    }
                };

                asyncStub.subscribeLevel1Ticks(mdRequest, responseObserver);

                for (int i = 0; i < 5; i++) {
                    logger.info("Hello from main thread: " + i);
                    Thread.sleep(1000);
                }

                // Cancel the stream
                responseObserver.onError(Status.CANCELLED.withDescription("Client requested cancellation").asRuntimeException());

                // Await termination of the stream
                finishLatch.await();

                DisconnectRequest disconnectRequest = DisconnectRequest.newBuilder()
                        .setUserToken(userToken)
                        .build();

                DisconnectResponse disconnectResponse = blockingStub.disconnect(disconnectRequest);
                logger.info("Disconnect result: " + disconnectResponse.getServerResponse());
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "RPC failed", e);
        } finally {
            try {
                shutdown();
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Channel did not shutdown cleanly", e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MarketDataClient client = new MarketDataClient("localhost", 50051);
        client.run();
    }
}
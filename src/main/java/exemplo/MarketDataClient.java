package exemplo;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import realtick.grpc.MarketDataServiceGrpc;
import realtick.grpc.Utilities;
import realtick.grpc.UtilityServicesGrpc;

import javax.net.ssl.SSLException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class MarketDataClient {

    private String password = "test123";
    private String server = "EMSUATXAPI.taltrade.com";
    private String user = "USER13";
    private String domain = "XAPIDEMO";
    private String locale = "inhouse americas";
    private int port = Integer.parseInt("9000");

    private static final Logger logger = Logger.getLogger(MarketDataClient.class.getName());
    private ManagedChannel channel;
    private UtilityServicesGrpc.UtilityServicesBlockingStub blockingStub;
    private MarketDataServiceGrpc.MarketDataServiceStub asyncStub;

    public MarketDataClient(String host, int port) throws SSLException {

        InputStream certInputStream = getClass().getClassLoader().getResourceAsStream("roots.pem");
        if (certInputStream == null) {
            log.error("Failed to load roots.pem_ from resources");
            return;
        }



        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(certInputStream)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                .build();


        channel = NettyChannelBuilder.forAddress(server, port)
                .sslContext(sslContext)
                .build();



       // blockingStub = UtilityServicesGrpc.newBlockingStub(channel);
        //asyncStub = MarketDataServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void run() {
        try {

            Utilities.ConnectRequest connectRequest = Utilities.ConnectRequest.newBuilder()
                    .setUserName(user)
                    .setDomain(domain)
                    .setPassword(password)
                    .setLocale(locale)
                    .build();

            Utilities.ConnectResponse response = blockingStub.connect(connectRequest);
            logger.info("Connect result: " + response.getResponse());

            if ("success".equals(response.getResponse())) {
                String userToken = response.getUserToken();
                System.out.println();

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
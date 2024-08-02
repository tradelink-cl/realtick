package cl.tradelink.realtick;

import com.ezesoft.xapi.generated.Utilities;
import com.ezesoft.xapi.generated.UtilityServicesGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SrpConnectExample {

    private String password = "test123";
    private String server = "EMSUATXAPI.taltrade.com";
    private String user = "USER13";
    private String domain = "XAPIDEMO";
    private String locale = "inhouse americas";
    private int port = 9000;

    private ManagedChannel channel;
    private UtilityServicesGrpc.UtilityServicesBlockingStub blockingStub;

    public void run() {
        try {

            // Cargar el archivo PEM desde resources
            InputStream certInputStream = getClass().getClassLoader().getResourceAsStream("roots.pem");
            if (certInputStream == null) {
                log.error("Failed to load roots.pem_ from resources");
                return;
            }

            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(certInputStream)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.NPN_AND_ALPN,
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2))
                    .build();

            // Construcción del canal gRPC con SSL
            channel = NettyChannelBuilder.forAddress(server, port)
                    .sslContext(sslContext)
                    .build();

            blockingStub = UtilityServicesGrpc.newBlockingStub(channel);


            Utilities.ConnectRequest startLoginSrpRequest = Utilities.ConnectRequest.newBuilder()
                    .setUserName(user)
                    .setDomain(domain)
                    .setLocale(locale)
                    .setPassword(password)
                    .build();

            try {

                Utilities.ConnectResponse  connect= blockingStub.connect(startLoginSrpRequest);
                log.info("Start SRP result: " + connect.getResponse());


            } catch (StatusRuntimeException e) {
                log.error("RPC failed: {0}", e.getStatus());
            }


            startLoginSrpRequest = Utilities.ConnectRequest.newBuilder()
                    .setUserName(user)
                    .setDomain(domain)
                    .setLocale(locale)
                    .setPassword(password)
                    .build();

            try {
                Utilities.ConnectResponse srpStartResponse2 = blockingStub.connect(startLoginSrpRequest);
                log.info("Start SRP result: " + srpStartResponse2.getResponse());

            } catch (StatusRuntimeException e) {
                log.error("RPC failed: {}", e.getStatus());
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void disconnect() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws Exception {
        SrpConnectExample example = new SrpConnectExample();
        example.run();
        Thread.sleep(30000);  // Espera para mantener la conexión abierta por un tiempo
        example.disconnect();
    }
}

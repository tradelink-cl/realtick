package exemplo;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import realtick.grpc.*;


import java.util.concurrent.TimeUnit;

@Slf4j
public class SrpConnectExample {

    private String password = "test123";
    private String server = "EMSUATXAPI.taltrade.com";
    private String user = "USER13";
    private String domain = "XAPIDEMO";
    private String locale = "Inhouse Americas";
    private int port = 9000; // No es necesario convertir explícitamente una cadena a entero

    private ManagedChannel channel;
    private UtilityServicesGrpc.UtilityServicesBlockingStub blockingStub;

    public void run() {
        try {
            // Configuración del contexto SSL
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1))
                    .build();

            // Construcción del canal gRPC
            channel = NettyChannelBuilder.forAddress(server, port)
                    .sslContext(sslContext)
                    .build();

            // Crear un stub usando el canal
            blockingStub = UtilityServicesGrpc.newBlockingStub(channel);

            // Crear solicitud de conexión
            ConnectRequest connectRequest = ConnectRequest.newBuilder()
                    .setUserName(user)
                    .setDomain(domain)
                    .setPassword(password)
                    .setLocale(locale)
                    .build();

            try {
                // Enviar solicitud de conexión
                ConnectResponse connectResponse = blockingStub.connect(connectRequest);
                log.info("Connect result: " + connectResponse.getResponse());

                // Manejar la respuesta de conexión
                if ("success".equals(connectResponse.getResponse())) {
                    DisconnectRequest disconnectRequest = DisconnectRequest.newBuilder()
                            .setUserToken(connectResponse.getUserToken())
                            .build();

                    try {
                        // Enviar solicitud de desconexión
                        DisconnectResponse disconnectResponse = blockingStub.disconnect(disconnectRequest);
                        log.info("Disconnect result: " + disconnectResponse.getServerResponse());
                    } catch (StatusRuntimeException e) {
                        log.warn("RPC failed: {}", e.getStatus());
                    }
                } else {
                    log.error("Connection failed: {}", connectResponse.getResponse());
                    if ("UserNotPermissionedForApp".equals(connectResponse.getResponse())) {
                        log.error("User does not have permission for the application. Please check user permissions.");
                    } else {
                        log.error("Unknown error: {}", connectResponse.getResponse());
                    }
                }

            } catch (StatusRuntimeException e) {
                log.error("RPC failed: {} {}", e.getStatus(), e);
            }

        } catch (Exception e) {
            log.error("Error setting up SSL context or gRPC channel: ", e);
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

package exemplo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import io.grpc.netty.shaded.io.netty.handler.ssl.*;
import realtick.grpc.Utilities;
import realtick.grpc.UtilityServicesGrpc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class SrpConnectExample2 {

    private  ManagedChannel channel;
    private  UtilityServicesGrpc.UtilityServicesBlockingStub blockingStub;

    public SrpConnectExample2(String host, int port, String certFilePath) throws IOException {
        // Load the certificate

        InputStream certInputStream = getClass().getClassLoader().getResourceAsStream("roots.pem");
        if (certInputStream == null) {
            return;
        }


        // Create SSL Context
        SslContext sslContext = SslContextBuilder.forClient()
                .sslProvider(SslProvider.OPENSSL)
                .trustManager(certInputStream)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                .build();

        // Create the channel
        channel = NettyChannelBuilder.forAddress(host, port)
                .sslContext(sslContext)
                .build();

        blockingStub = UtilityServicesGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void connect(String user, String domain, String password, String locale) {
        Utilities.ConnectRequest request = Utilities.ConnectRequest.newBuilder()
                .setUserName(user)
                .setDomain(domain)
                .setPassword(password)
                .setLocale(locale)
                .build();
        try {
            Utilities.ConnectResponse response = blockingStub.connect(request);
            System.out.println("Connect result: " + response.getResponse());

            if ("success".equals(response.getResponse())) {
                Utilities.DisconnectRequest disconnectRequest = Utilities.DisconnectRequest.newBuilder()
                        .setUserToken(response.getUserToken())
                        .build();
                Utilities.DisconnectResponse disconnectResponse = blockingStub.disconnect(disconnectRequest);
                System.out.println("Disconnect result: " + disconnectResponse.getServerResponse());
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    public static void main(String[] args) throws Exception {



        SrpConnectExample2 client = new SrpConnectExample2("EMSUATXAPI.taltrade.com", 9000, "roots.pem");
        try {
            client.connect("USER13", "XAPIDEMO", "test123", "inhouse americas");
        } finally {
            client.shutdown();
        }
    }
}
package exemplo;

import cl.tradelink.realtick.UtilitiesRealtick;
import cl.tradelink.realtick.UtilityServicesGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SrpConnectExample {

    private String password = "Vector.2023";
    private String server = "AMERICASGW.taltrade.com";
    private String user = "FRICCI";
    private String domain = "VECTORCAP";
    private String locale = "inhouse americas";
    private int port = Integer.parseInt("9000");

    private ManagedChannel channel;
    private UtilityServicesGrpc.UtilityServicesBlockingStub utilStub;

    public void run() throws IOException {

        channel = NettyChannelBuilder.forAddress(server, port)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File(SrpConnectExample.class.getResource("/roots.pem").getFile())).build())
                .build();

        utilStub = UtilityServicesGrpc.newBlockingStub(channel);

        UtilitiesRealtick.StartLoginSrpRequest srpStartRequest = UtilitiesRealtick.StartLoginSrpRequest.newBuilder()
                .setUserName(user)
                .setDomain(domain)
                .build();

        UtilitiesRealtick.StartLoginSrpResponse srpStartResponse = null;

        try {

            srpStartResponse = utilStub.startLoginSrp(srpStartRequest);
            log.info("Start SRP result: " + srpStartResponse.getResponse());

        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {0}", e.getStatus());
            return;
        }

        if ("success".equals(srpStartResponse.getResponse())) {

            String identity = user + "@" + domain;
            String gHex = srpStartResponse.getSrpg();
            String nHex = srpStartResponse.getSrpN();


            /*
            srp.rfc5054_enable(true);
            User usr = new User(identity, password, srp.SHA256, srp.NG_CUSTOM, nHex, gHex);
            srp.rfc5054_enable(false);

            byte[] A = usr.startAuthentication();

            byte[] bytesB = new BigInteger(srpStartResponse.getSrpb()).toByteArray();
            byte[] bytesS = new BigInteger(srpStartResponse.getSrpSalt(), 16).toByteArray();
            byte[] M = usr.processChallenge(bytesS, bytesB);

            String strMc = new BigInteger(1, M).toString(16);
            String strEphA = new BigInteger(1, A).toString();
            String srpTransactId = srpStartResponse.getSrpTransactId();



            UtilitiesRealtick.CompleteLoginSrpRequest srpCompleteRequest = UtilitiesRealtick.CompleteLoginSrpRequest.newBuilder()
                    .setIdentity(identity)
                    .setSrpTransactId(srpTransactId)
                    .setStrEphA(strEphA)
                    .setStrMc(strMc)
                    .setUserName(user)
                    .setDomain(domain)
                    .setLocale(locale)
                    .build();

            UtilitiesRealtick.CompleteLoginSrpResponse connectResponse;
            try {
                connectResponse = utilStub.completeLoginSrp(srpCompleteRequest);
                log.info("Connect result: " + connectResponse.getResponse());
            } catch (StatusRuntimeException e) {
                log.warn("RPC failed: {0}", e.getStatus());
                return;
            }

            if ("success".equals(connectResponse.getResponse())) {
                UtilitiesRealtick.DisconnectRequest disconnectRequest = UtilitiesRealtick.DisconnectRequest.newBuilder()
                        .setUserToken(connectResponse.getUserToken())
                        .build();

                UtilitiesRealtick.DisconnectResponse disconnectResponse;
                try {
                    disconnectResponse = utilStub.disconnect(disconnectRequest);
                    log.info("Disconnect result: " + disconnectResponse.getServerResponse());
                } catch (StatusRuntimeException e) {
                    log.warn("RPC failed: {0}", e.getStatus());
                }
            }

             */
        }
    }

    public void disconnect() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SrpConnectExample example = new SrpConnectExample();
        example.run();
        Thread.sleep(30000);
        example.disconnect();
    }
}
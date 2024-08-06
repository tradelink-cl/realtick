package cl.tradelink.realtick;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import cl.tradelink.realtick.mkd.SubscribeLevel1Ticks;
import com.ezesoft.xapi.generated.MarketDataServiceGrpc;
import com.ezesoft.xapi.generated.UtilityServicesGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class MarketDataClient {

    private static final Logger logger = Logger.getLogger(MarketDataClient.class.getName());
    private ManagedChannel channel;
    private UtilityServicesGrpc.UtilityServicesBlockingStub blockingStub;
    private MarketDataServiceGrpc.MarketDataServiceStub asyncStub;



    public static void main(String[] args) {


        try {

            Properties properties;

            try (FileInputStream fis = new FileInputStream(args[0])) {
                properties = new Properties();
                properties.load(fis);

                EMSXAPILibrary.Create(properties.getProperty("config"));
                EMSXAPILibrary templib = EMSXAPILibrary.Get();

                templib.login();
                templib.startListeningHeartbeat(5);

                CompletableFuture<Void> subscribeLevel1TicksAsync = CompletableFuture.runAsync(() -> {
                    SubscribeLevel1Ticks subscribeLevel1TicksExample = new SubscribeLevel1Ticks();
                    subscribeLevel1TicksExample.run("SQM", templib);
                });


                EMSXAPILibrary.Create(properties.getProperty("config"));
                EMSXAPILibrary templib2 = EMSXAPILibrary.Get();

                templib2.login();

                templib2.startListeningHeartbeat(5);

                CompletableFuture<Void> subscribeLevel1TicksAsync2 = CompletableFuture.runAsync(() -> {
                    SubscribeLevel1Ticks subscribeLevel1TicksExample = new SubscribeLevel1Ticks();
                    subscribeLevel1TicksExample.run("BCH", templib2);
                });


                EMSXAPILibrary.Create(properties.getProperty("config"));
                EMSXAPILibrary templib3 = EMSXAPILibrary.Get();

                templib3.login();

                templib3.startListeningHeartbeat(5);

                CompletableFuture<Void> subscribeLevel1TicksAsync3 = CompletableFuture.runAsync(() -> {
                    SubscribeLevel1Ticks subscribeLevel1TicksExample = new SubscribeLevel1Ticks();
                    subscribeLevel1TicksExample.run("AAPL", templib3);
                });



            } catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }


            while (true){
                Thread.sleep(1000);
            }




            /*
            CompletableFuture<Void> subscribeOrdInfoAsync = CompletableFuture.runAsync(() -> {
                //ExampleSubscribeOrdInfo subscribeOrdInfoExample = new ExampleSubscribeOrdInfo();
                //subscribeOrdInfoExample.run();
            });

            //Submit 20 orders each after interval of 10 seconds
            for(int i=1; i<=20; i++)
            {

                //ExampleSubmitSingleOrder submitSingleOrdExample = new ExampleSubmitSingleOrder();
                //submitSingleOrdExample.run();
                //System.out.println("Submitted order");
                //hread.sleep(10*1000);


            }

            Thread.sleep(1 * 60 * 1000);

             */

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }


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


        /*
        channel = NettyChannelBuilder.forAddress(server, port)
                .sslContext(sslContext)
                .build();

         */



       // blockingStub = UtilityServicesGrpc.newBlockingStub(channel);
        //asyncStub = MarketDataServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void run() {
        try {

            /*
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

             */

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

}
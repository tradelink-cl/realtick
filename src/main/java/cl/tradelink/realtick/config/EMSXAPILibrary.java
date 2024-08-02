package cl.tradelink.realtick.config;

import cl.tradelink.realtick.fail.*;
import com.ezesoft.xapi.generated.MarketDataServiceGrpc;
import com.ezesoft.xapi.generated.SubmitOrderServiceGrpc;
import com.ezesoft.xapi.generated.Utilities.*;
import com.ezesoft.xapi.generated.Utilities.SubscribeHeartBeatResponse.HeartBeatStatus;
import com.ezesoft.xapi.generated.UtilityServicesGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class EMSXAPILibrary {

    private EMSXAPILibrary(String configFileName) throws Exception {
        this.config = new EMSXAPIConfig(configFileName);
        this.openChannel();
    }

    private static Object instanceLock = new Object();
    private static EMSXAPILibrary instance;

    public static void Create(String configFileName)
            throws Exception {
        if(configFileName == null || configFileName == "")
                configFileName = "config.cfg";
        synchronized (instanceLock) {
            instance = new EMSXAPILibrary(configFileName);
        }
    }

    public static EMSXAPILibrary Get(){
        return instance;
    }


    public ErrorHandler errorHandler;
    private ManagedChannel _channel;

    private UtilityServicesGrpc.UtilityServicesBlockingStub _utilitySvcStub;
    private MarketDataServiceGrpc.MarketDataServiceBlockingStub _mktDataSvcStub;
    private SubmitOrderServiceGrpc.SubmitOrderServiceBlockingStub _orderServiceStub;

    private EMSXAPIConfig config;
    private String userToken;
    private Thread heartBeatThread;   


    private boolean isLoggedIn;
    private boolean heartbeatExitSignal;

    private Object loggedInLock = new Object();

    public boolean getIsLoggedIn() {
        synchronized (this.loggedInLock) {
            return this.isLoggedIn;
        }
    }

    public void setIsLoggedIn(boolean value) {
        synchronized (this.loggedInLock) {
            this.isLoggedIn = value;
        }
    }

    public String getUserToken() {
        return this.userToken;
    }

    public void setUserToken(String val) {
        this.userToken = val;
    }

    public UtilityServicesGrpc.UtilityServicesBlockingStub getUtilityServiceStub() {
        return this._utilitySvcStub;
    }

    public MarketDataServiceGrpc.MarketDataServiceBlockingStub getMarketDataServiceStub() {
        return this._mktDataSvcStub;
    }

    public SubmitOrderServiceGrpc.SubmitOrderServiceBlockingStub getOrderServiceStub() {
        return this._orderServiceStub;
    }

    private ManagedChannel createChannel(String hostName, int port) {

        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(hostName, port);

        if (config.getKeepAliveTime() > 0) {
            builder.keepAliveTime(config.getKeepAliveTime(), TimeUnit.MILLISECONDS)
                    .keepAliveTimeout(config.getKeepAliveTimeout(), TimeUnit.MILLISECONDS)
                    .keepAliveWithoutCalls(true);
        }
        if (config.getMaxMessageSize() > 0) {
            builder.maxInboundMessageSize(config.getMaxMessageSize());
        }
        if (config.getIsSslEnabled()) {
            try {
                if (config.getCertFilePath() != null) {
                    File certFile = new File(config.getCertFilePath());
                    if (!certFile.exists()) {
                        throw new RuntimeException("Certificate file " + config.getCertFilePath() + " does not exist");
                    }
                    SslContext sslContext = GrpcSslContexts.forClient()
                            .trustManager(certFile)
                            .build();
                    builder.sslContext(sslContext);
                }
            } catch (SSLException e) {
                throw new RuntimeException("Couldn't set up SSL context", e);
            }
        } else {
            builder.usePlaintext();
        }
        return builder.build();
    }

    public ManagedChannel getChannel() throws NetworkFailedException, ServerNotAvailableException {
        if (_channel == null || _channel.isShutdown()) {
            this.openChannel();
        }

        return this._channel;
    }

    private void initStubs() {
        _utilitySvcStub = UtilityServicesGrpc.newBlockingStub(this._channel);
        _mktDataSvcStub = MarketDataServiceGrpc.newBlockingStub(this._channel);
        _orderServiceStub = SubmitOrderServiceGrpc.newBlockingStub(this._channel);
    }

    public void login() throws LoginFailedException {
        try {
            setIsLoggedIn(false);
            ConnectRequest connectRequest = ConnectRequest.newBuilder()
                    .setUserName(config.getUser())
                    .setDomain(config.getDomain())
                    .setPassword(config.getPassword())
                    .setLocale(config.getLocale())
                    .build();

            ConnectResponse connectResponse = this.getUtilityServiceStub().connect(connectRequest);

            this.userToken = connectResponse.getUserToken();
            this.setIsLoggedIn(true);
        } catch (Exception ex) {
            throw new LoginFailedException(ex.getMessage(), ex);
        }
    }

    public void logout() {
        try {
            DisconnectRequest disConnReq = DisconnectRequest.newBuilder()
                    .setUserToken(userToken)
                    .build();

            DisconnectResponse disconnectResponse = getUtilityServiceStub().disconnect(disConnReq);

            if (disconnectResponse.getServerResponse().equals("success")) {
                System.out.println("Logged out");
            } else {
                String errorMessage = disconnectResponse.getOptionalFieldsMap().get("ErrorMessage");
                System.out.println(errorMessage);
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    public void closeChannel() {

        if (this._channel != null && !this._channel.isShutdown())
            try {
                this._channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        this._channel = null;
    }

    public void openChannel() throws NetworkFailedException, ServerNotAvailableException {
        if(!this.canConnectToInternet()){
            throw new NetworkFailedException(
                "Network not available");
        }
        if (this.canConnectToServer()) {
            this.closeChannel();
            this._channel = createChannel(config.getServer(), config.getPort());
            this.initStubs();
            return;
        }
        throw new ServerNotAvailableException(
                "Server "+config.getServer()+" not responding, might be down.");
    }

    private void sleep(long delayMS) {
        try {
            Thread.sleep(delayMS);
        } catch (Exception e) {

        }
    }

    public void startListeningHeartbeat(int reqTimeout) throws Exception {
        if (!this.getIsLoggedIn()) {
            throw new Exception("User needs to login first");
        }

        suspendHeartbeatThread();

        heartbeatExitSignal = false;
        heartBeatThread = new Thread(() -> {
            try {
                heartbeatExitSignal = false;
                boolean refreshChannel = false;
                boolean refreshLogin = false;

                int retryCount = 0;

                while (this.getIsLoggedIn() && retryCount < config.getMaxRetryCount()) {
                    try {
                        if(heartbeatExitSignal)
                            break;

                        retryCount++;

                        if (refreshChannel) {
                            this.closeChannel();
                            this.openChannel();
                        }
                        if (refreshLogin) {
                            this.setIsLoggedIn(false);
                            this.login();
                        }
                        refreshChannel = false;
                        refreshLogin = false;

                        this.execSubscribeHeartBeat(reqTimeout);
                    } catch (RuntimeException runEx) {
                        // need to initialize the channel again but same token can be re-used
                        long delayMS = calculateDelayMillis(retryCount);
                        if (runEx instanceof RuntimeException)
                            writeToLog(
                                    "Runtime IO error: attempting again(" + retryCount + ") in " + delayMS + " ms...");
                        refreshChannel = true;
                        refreshLogin = false;
                        sleep(delayMS);

                    } catch (SessionNotFoundException ssnEx) {
                        // need to login again
                        refreshLogin = true;
                        refreshChannel = false;
                        long delayMS = calculateDelayMillis(retryCount);
                        writeToLog("Session not found: attempting login again(" + retryCount + ") in " + delayMS
                                + " ms...");
                        sleep(delayMS);

                    } catch (StreamingAlreadyExistsException strEx) {
                        // need to login again
                        refreshLogin = false;
                        refreshChannel = false;
                        long delayMS = calculateDelayMillis(retryCount);
                        writeToLog(strEx.getMessage());
                        sleep(delayMS);

                    } catch (ServerNotAvailableException srvrEx) {
                        // need to login again
                        refreshChannel = true;
                        refreshLogin = true;
                        long delayMS = calculateDelayMillis(retryCount);
                        writeToLog(srvrEx.getMessage());
                        sleep(delayMS);
                    }
                    catch (Exception ex) {
                        writeToLog(ex.toString());
                        break;
                    }
                }
            } catch (Exception ex) {
                if (this.errorHandler != null)
                    this.errorHandler.onError(ex);
            }
        });

        heartBeatThread.start();
    }

    private void writeToLog(String msg) {
        System.out.println(msg);
    }

    public void execSubscribeHeartBeat(int reqTimeout)
            throws SessionNotFoundException, RuntimeException, StreamingAlreadyExistsException {

        SubscribeHeartBeatRequest subscribeRequest = SubscribeHeartBeatRequest.newBuilder()
                .setUserToken(getUserToken())
                .setTimeoutInSeconds(reqTimeout)
                .build();

        Iterator<SubscribeHeartBeatResponse> hbResponseIt = this.getUtilityServiceStub().subscribeHeartBeat(subscribeRequest);

        while (hbResponseIt != null && hbResponseIt.hasNext()) {

            if(heartbeatExitSignal)
                break;

            SubscribeHeartBeatResponse response = hbResponseIt.next();
            HeartBeatStatus resStatus = response.getStatus();
            String serverMsg = response.getAcknowledgement().getServerResponse();

            // writeToLog("HeartBeat response status: " + resStatus.toString());
            writeToLog("[" + getCurrentTime() + "]" + " HeartBeat status: " + resStatus.toString() + " | " + serverMsg);

            if (resStatus == HeartBeatStatus.DEAD) {
                hbResponseIt = null;
                throw new SessionNotFoundException("Session not found for user token " + this.getUserToken());
            } else if (resStatus == HeartBeatStatus.UNKNOWN || resStatus == HeartBeatStatus.UNRECOGNIZED) {
                hbResponseIt = null;
                throw new RuntimeException("Status received as " + resStatus + "");
            } else if (serverMsg.equalsIgnoreCase("Error: Active streaming subscription already exists.")) {
                hbResponseIt = null;
                throw new StreamingAlreadyExistsException("Heartbeat subscription already exists");
            }
        }
    }

    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();

        // Define the desired date and time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the date and time using the formatter
        return now.format(formatter);
    }

    private long calculateDelayMillis(int retryCount) {
        return (long) Math.pow(2, retryCount) * config.getRetryDelayMS();
    }

    public void suspendHeartbeatThread() {
        if (heartBeatThread != null) {
            if (heartBeatThread.isAlive()){
                heartBeatThread.interrupt();
                heartbeatExitSignal = true;
            }

            while (heartBeatThread.isAlive()) {
                writeToLog("Waiting for heartbreat thread to stop");
                sleep(config.getRetryDelayMS());
            }
            heartBeatThread = null;
        }
    }

    private boolean canConnectToInternet() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return address.isReachable(3000); // Timeout of 5 seconds
        } catch (UnknownHostException e) {
            return false; // Unable to resolve host
        } catch (Exception e) {
            return false; // Other exceptions
        }
    }

    private boolean canConnectToServer() {
        Socket socket = new Socket();
        int timeoutMs = 3000; // Set timeout to 3 seconds

        try {
            InetSocketAddress address = new InetSocketAddress(config.getServer(), config.getPort());
            socket.connect(address, timeoutMs);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

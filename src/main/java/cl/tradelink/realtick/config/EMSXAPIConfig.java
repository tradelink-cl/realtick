package cl.tradelink.realtick.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EMSXAPIConfig {

   
    private int keepAliveTime;
    private int keepAliveTimeout;
    private String user;
    private String password;
    private String server;
    private String domain;
    private int port;
    private String locale;
    private String certFilePath;
    private Boolean ssl;
    private int maxRetryCount;
    private int retryDelayMS;

    public EMSXAPIConfig(String cfgFileName) {
        if (cfgFileName == null || cfgFileName.isEmpty()) {
            cfgFileName = "config.cfg";
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(cfgFileName)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read config items
        user = properties.getProperty("user");
        password = properties.getProperty("password");
        server = properties.getProperty("server");
        domain = properties.getProperty("domain");
        port = Integer.parseInt(properties.getProperty("port").trim());
        locale = properties.getProperty("locale");
        certFilePath = properties.getProperty("certFilePath");
        keepAliveTime = Integer.parseInt(properties.getProperty("keepAliveTime").trim());
        keepAliveTimeout = Integer.parseInt(properties.getProperty("keepAliveTimeout").trim());
        ssl = Boolean.parseBoolean(properties.getProperty("ssl"));
        maxRetryCount = Integer.parseInt(properties.getProperty("maxRetryCount").trim());
        retryDelayMS = Integer.parseInt(properties.getProperty("retryDelayMS").trim());
    }
 
    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getServer() {
        return server;
    }

    public String getDomain() {
        return domain;
    }

    public int getPort() {
        return port;
    }

    public String getLocale() {
        return locale;
    }

    public String getCertFilePath() {
        return certFilePath;
    }
    
    public int getMaxMessageSize(){
        return  1024 * 1024 * 1024;
    }

    public int getKeepAliveTimeout(){
        return keepAliveTimeout;
    }
    
    public int getKeepAliveTime(){
        return keepAliveTime;
    }

    public boolean getIsSslEnabled(){
        return ssl;
    }

    public int getMaxRetryCount(){
        return maxRetryCount;
    }

    public int getRetryDelayMS(){
        return retryDelayMS;
    }

}
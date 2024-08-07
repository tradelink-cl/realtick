package cl.tradelink.realtick.routing;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import cl.tradelink.realtick.mkd.SubscribeLevel1Ticks;
import com.ezesoft.xapi.generated.Order;
import com.ezesoft.xapi.generated.Order.SubscribeOrderInfoRequest;
import com.ezesoft.xapi.generated.Order.SubscribeOrderInfoResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SubscribeOrdInfo {

    /*
    public void run() {

        EMSXAPILibrary lib = null;
        try{
           // lib = EMSXAPILibrary.Get();

            SubscribeOrderInfoRequest req = SubscribeOrderInfoRequest.newBuilder()
                                            .setUserToken(lib.getUserToken())
                                            .setIncludeUserSubmitOrder(true)
                                            .setIncludeUserSubmitStagedOrder(true)
                                            .setIncludeUserSubmitCompoundOrder(true)
                                            .build();
           
            Iterator<SubscribeOrderInfoResponse> responseIt =  lib.getOrderServiceStub().subscribeOrderInfo(req);

            while(responseIt.hasNext()){
                SubscribeOrderInfoResponse data = responseIt.next();

                System.out.println("------------------------------");
                System.out.println(data.toString());
                System.out.println("------------------------------");
             }
        }
        catch(Exception ex){
            System.out.println("Error - "+ ex.getMessage());
        }
    }

     */


    public static void main(String[] args) {

        Properties properties;

        try (FileInputStream fis = new FileInputStream(args[0])) {

            properties = new Properties();
            properties.load(fis);

            EMSXAPILibrary.Create(properties.getProperty("config"));
            EMSXAPILibrary templib = EMSXAPILibrary.Get();
            templib.login();

            Order.SubscribeOrderInfoRequest req = Order.SubscribeOrderInfoRequest.newBuilder()
                    .setUserToken(templib.getUserToken())
                    .setIncludeExchangeTradeOrder(true)
                    .setIncludeUserSubmitChange(true)
                    .setIncludeUserSubmitCancel(true)
                    .setIncludeExchangeAcceptOrder(true)
                    .setIncludeUserSubmitOrder(true)
                    .setIncludeUserSubmitTradeReport(true)
                    .build();

            Iterator<Order.SubscribeOrderInfoResponse> responseIt =  templib.getOrderServiceStub().subscribeOrderInfo(req);

            while(responseIt.hasNext()){

                Order.SubscribeOrderInfoResponse data = responseIt.next();

                if(data.getCurrentStatus().equals("LIVE")){
                    System.out.println(data.getCurrentStatus());
                }

            }






            Order.SubmitTradeReportRequest reqq = Order.SubmitTradeReportRequest.newBuilder()
                    .setUserToken(templib.getUserToken())
                    .build();


            while (true){
                Thread.sleep(10);
            }


        } catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }



    }
}

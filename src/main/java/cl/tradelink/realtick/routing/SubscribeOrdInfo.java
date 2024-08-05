package cl.tradelink.realtick.routing;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import com.ezesoft.xapi.generated.Order.SubscribeOrderInfoRequest;
import com.ezesoft.xapi.generated.Order.SubscribeOrderInfoResponse;

import java.util.Iterator;

public class SubscribeOrdInfo {
    public void run() {

        EMSXAPILibrary lib = null;
        try{
            lib = EMSXAPILibrary.Get();

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
}

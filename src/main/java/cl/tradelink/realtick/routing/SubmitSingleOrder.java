package cl.tradelink.realtick.routing;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import com.ezesoft.xapi.generated.Order;

public class SubmitSingleOrder {
    public void run() {
        EMSXAPILibrary lib = null;
        try{
            //lib = EMSXAPILibrary.Get();

            Order.SubmitSingleOrderRequest req = Order.SubmitSingleOrderRequest.newBuilder()
                                            .setUserToken(lib.getUserToken())
                                            .setSymbol("SYMBOL")
                                            .setSide("BUY/SELL")
                                            .setQuantity(100)
                                            .setRoute("ROUTE")
                                            .setStaged(false)
                                            .setClaimRequire(false)
                                            .setAccount("BANK;BRANCH;CUSTOMER;DEPOSIT")
                                            .build();
            
            Order.SubmitSingleOrderResponse response =  lib.getOrderServiceStub().submitSingleOrder(req);

            System.out.println("------------------------------");
            System.out.println(response.toString());                
            System.out.println("------------------------------");
        }
        catch(Exception ex){
            System.out.println("Error - "+ ex.getMessage());
        }
    }
}

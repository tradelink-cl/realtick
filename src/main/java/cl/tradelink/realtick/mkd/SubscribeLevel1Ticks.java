package cl.tradelink.realtick.mkd;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import com.ezesoft.xapi.generated.Marketdata;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubscribeLevel1Ticks {

    public void run() {

        EMSXAPILibrary lib = null;
        try{

            lib = EMSXAPILibrary.Get();
            List<String> symbolsList = new ArrayList<>();
            //symbolsList.add("VOD.LSE`");
            //symbolsList.add("BARC.LSE`");

            //symbolsList.add("AAPL");



            Marketdata.Level1MarketDataRequest req = com.ezesoft.xapi.generated.Marketdata.Level1MarketDataRequest.newBuilder()
                                            .setUserToken(lib.getUserToken())
                                            .setAdvise(true)
                                            .setRequest(true)
                                            .addAllSymbols(symbolsList)
                                            .build();
           
            Iterator<com.ezesoft.xapi.generated.Marketdata.Level1MarketDataResponse> responseIt =  lib.getMarketDataServiceStub().subscribeLevel1Ticks(req);
            while(responseIt.hasNext()){
                com.ezesoft.xapi.generated.Marketdata.Level1MarketDataResponse data = responseIt.next();

                System.out.println("------------------------------");
                System.out.println(data.toString());                
                System.out.println("------------------------------");
             }

        } catch(Exception ex){
            System.out.println("Error - "+ ex.getMessage());
        }
    }
}

package cl.tradelink.realtick.mkd;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import com.ezesoft.xapi.generated.Marketdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubscribeLevel1Ticks {

    public void run(String symbol, EMSXAPILibrary lib) {

        try{

            Marketdata.Level1MarketDataRequest req = com.ezesoft.xapi.generated.Marketdata.Level1MarketDataRequest.newBuilder()
                                            .setUserToken(lib.getUserToken())
                                            .setAdvise(true)
                                            .setRequest(true)
                                            .addSymbols(symbol)
                                            .build();

            Iterator<com.ezesoft.xapi.generated.Marketdata.Level1MarketDataResponse> responseIt =  lib.getMarketDataServiceStub().subscribeLevel1Ticks(req);
            while(responseIt.hasNext()){

                com.ezesoft.xapi.generated.Marketdata.Level1MarketDataResponse data = responseIt.next();

                if(data.getBid().getDecimalValue() > 0){
                    System.out.println(data.getSymbolDesc() + " " + data.getBid().getDecimalValue());
                }
                if(data.getAsk().getDecimalValue() > 0){
                    System.out.println(data.getSymbolDesc() + " " + data.getAsk().getDecimalValue());
                }
             }

        } catch(Exception ex){
            System.out.println("Error - "+ ex.getMessage());
        }
    }
}

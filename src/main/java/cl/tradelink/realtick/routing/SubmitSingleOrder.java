package cl.tradelink.realtick.routing;

import cl.tradelink.realtick.config.EMSXAPILibrary;
import com.ezesoft.xapi.generated.Order;
import com.ezesoft.xapi.generated.Utilities;
import com.google.protobuf.DoubleValue;
import com.google.protocolbuff.g.IDGenerator;
import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.util.Properties;

@Slf4j
public class SubmitSingleOrder {

    public static void main(String[] args) {

        Properties properties;

        try (FileInputStream fis = new FileInputStream(args[0])) {

            properties = new Properties();
            properties.load(fis);

            EMSXAPILibrary.Create(properties.getProperty("config"));
            EMSXAPILibrary templib = EMSXAPILibrary.Get();
            templib.login();

            Order.SubmitSingleOrderRequest req = Order.SubmitSingleOrderRequest.newBuilder()
                    .setOrderTag(IDGenerator.getID())
                    .setUserToken(templib.getUserToken())
                    .setSymbol("AAPL")
                    .setSide("BUY")
                    .setTimeInForce(Utilities.ExpirationType.newBuilder().setExpiration(Utilities.ExpirationType.ExpirationTypes.DAY))
                    .setQuantity(1)
                    .setRoute("BULL-ARCAX")
                    .setPrice(DoubleValue.newBuilder().setValue(100).build())
                    .setPriceType(Utilities.PriceTypeEnum.newBuilder().setPriceTypeValue(1))
                    .setStaged(false)
                    .setClaimRequire(false)
                    .setAccount("BULL;00;BLTK;50D200452")
                    .build();


            /*
            Order.SubmitSingleOrderResponse response =  templib.getOrderServiceStub().submitSingleOrder(req);

            System.out.println("------------------------------");
            System.out.println(response.toString());
            System.out.println("---");


            String orderid = response.getOrderDetails().getOrderId();


            com.ezesoft.xapi.generated.Order.ChangeSingleOrderRequest responses = Order.ChangeSingleOrderRequest.newBuilder()
                    .setUserToken(templib.getUserToken())
                    .setPrice(DoubleValue.newBuilder().setValue(199).build())
                    .setQuantity(1)
                    .setOrderId(orderid).build();



            Order.ChangeSingleOrderResponse responseReplace =  templib.getOrderServiceStub().changeSingleOrder(responses);

            System.out.println("------------------------------");
            System.out.println(responseReplace.toString());
            System.out.println("---");




            com.ezesoft.xapi.generated.Order.CancelSingleOrderRequest cancel = Order.CancelSingleOrderRequest.newBuilder()
                    .setOrderId(orderid)
                    .setUserToken(templib.getUserToken()).build();


            Order.CancelSingleOrderResponse responseCancel =  templib.getOrderServiceStub().cancelSingleOrder(cancel);

            System.out.println("------------------------------");
            System.out.println(responseCancel.toString());
            System.out.println("------------------------------");




            while (true){
                Thread.sleep(100);
            }

             */


        } catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }



    }
}

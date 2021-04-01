import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DataProcessor implements Runnable {

  private Connection conn;
  private String QUEUE_NAME;
  private StoreInfo cache = StoreInfo.getInstance();

  public DataProcessor(Connection conn, String QUEUE_NAME) {
    this.conn = conn;
    this.QUEUE_NAME = QUEUE_NAME;
  }

  @Override
  public void run() {
    try {
      final Channel channel = conn.createChannel();
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        // process the fetched message
        PurchaseWrapper purchaseInfo = readRequestBody(message);
        //System.out.println(purchaseInfo);
        // TODO: save this into a DT (2d array? hashmap?) so that I can later process GET requests
        int storeID = purchaseInfo.getStoreID();
        List<PurchaseItems> items = purchaseInfo.getPurchaseItems();
        for (PurchaseItems i : items) {
          System.out.println("trying to write to cache");
          cache.putSalesInfo(storeID, i.getItemID(), i.getNumberOfItems());
          System.out.println("wrote to cache");
        }
        // TODO: basicReject too? idk -> yes in exception
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        cache.getTopFiveStores(101);
        cache.getTopTenItems(1);
      };

      channel.basicConsume(QUEUE_NAME, deliverCallback, consumerTag -> {});
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private PurchaseWrapper readRequestBody(String reqBody) throws IOException {
    String[] parsed = reqBody.split("#");
    // for (String s : parsed) { System.out.println(s); }
    reqBody = parsed[0];
    int storeID = Integer.valueOf(parsed[1]);
    int custID = Integer.valueOf(parsed[2]);
    String purchaseDate = parsed[3];
    try {
      Purchase purchase = new Gson().fromJson(reqBody, Purchase.class);
      LocalDate parsedDate = LocalDate.parse(purchaseDate, DateTimeFormatter.BASIC_ISO_DATE);
      return new PurchaseWrapper(storeID, custID, parsedDate, purchase.items);
    } catch(Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}

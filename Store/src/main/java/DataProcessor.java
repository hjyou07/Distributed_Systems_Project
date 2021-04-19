import Model.Purchase;
import Model.PurchaseItems;
import Model.PurchaseWrapper;
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
  private int[][] itemByStore;
  private final boolean DURABLE = false;

  public DataProcessor(Connection conn, String QUEUE_NAME, int[][] cache) {
    this.conn = conn;
    this.QUEUE_NAME = QUEUE_NAME;
    this.itemByStore = cache;
  }

  @Override
  public void run() {
    try {
      final Channel channel = conn.createChannel();
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        try {
          String message = new String(delivery.getBody(), "UTF-8");
          // process the fetched message
          PurchaseWrapper purchaseInfo = readRequestBody(message);
          int storeID = purchaseInfo.getStoreID();
          List<PurchaseItems> items = purchaseInfo.getPurchaseItems();
          for (PurchaseItems i : items) {
            //System.out.println(i.toString());
            int row = i.getItemID() - 1;
            int col = storeID - 1;
            synchronized (itemByStore[row]) {
              itemByStore[row][col] += i.getNumberOfItems();
            }
          }
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
          e.printStackTrace();
          channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
        }
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
      return new PurchaseWrapper(storeID, custID, parsedDate, purchase.getItems());
    } catch(Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void simplePutCheck() {
    int total = 0;
    for (int[] item : itemByStore) {
      for (int quantity : item) {
        total += quantity;
      }
    }
    System.out.println("total quant" + total);
  }
}

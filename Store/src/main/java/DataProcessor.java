import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataProcessor implements Runnable {

  private Connection conn;
  private String QUEUE_NAME = "dataProcessor";

  public DataProcessor(Connection conn, String QUEUE_NAME) {
    this.conn = conn;
    this.QUEUE_NAME = QUEUE_NAME;
  }

  @Override
  public void run() {
    try {
      final Channel channel = conn.createChannel();
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        // process the fetched message
        PurchaseWrapper purchaseInfo = readRequestBody(message);
        System.out.println(purchaseInfo);
        // TODO: save this into a DT (2d array? hashmap?) so that I can later process GET requests
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

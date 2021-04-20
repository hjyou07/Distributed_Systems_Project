import DAL.DynamoDao;
import DAL.PurchaseDao;
import Model.Purchase;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.sql.SQLException;

public class DBWriter implements Runnable {
  private Connection conn;
  private String QUEUE_NAME;
  private final boolean DURABLE = false;

  public DBWriter(Connection conn, String QUEUE_NAME) {
    this.conn = conn;
    this.QUEUE_NAME = QUEUE_NAME;
  }

  @Override
  public void run() {
    Channel channel = null;
   try {
     channel = conn.createChannel();
     channel.basicQos(1);

     ChannelConsumer consumer = new ChannelConsumer(channel);
     // I can pass in a DeliverCallback and it is preferred over Consumer for a lambda-oriented syntax, but I'm just exploring.
     // also, auto-ack is off with this overloaded signature. auto-cak is "fire and forget"
     channel.basicConsume(QUEUE_NAME, consumer);
   } catch (IOException e) {
     e.printStackTrace();
   }
  }

  public class ChannelConsumer extends DefaultConsumer {

    public ChannelConsumer(Channel channel) {
      super(channel);
    }

    /**
     * Called when a basic.deliver is received for this consumer. Equivalent to functional interface DeliverCallback's handle()
     * @param consumerTag the consumer tag associated with the consumer
     * @param envelope packaging data for the message
     * @param properties content header data for the message
     * @param body the message body (opaque, client specific bye array)
     * @throws IOException
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
        byte[] body) throws IOException {
      String message = new String(body, "UTF-8");
      // TODO 1: Lift this up to the Purchase microservice (separate project)
      // now try creating the purchase POJO object from the json string
      Purchase purchase = readRequestBody(message);
      // TODO 2: Send ack once write to the db succeeds.
      try {
        DynamoDao dao = new DynamoDao();
        dao.createPurchaseInDB(purchase);
        // b: false indicates to ack just the supplied delivery tag
        getChannel().basicAck(envelope.getDeliveryTag(), false);
      } catch (Exception e) {
        e.printStackTrace();
        // b: true indicates requeueing
        getChannel().basicReject(envelope.getDeliveryTag(), true);
      }
    }

    private Purchase readRequestBody(String reqBody) {
      String[] parsed = reqBody.split("#");
      // for (String s : parsed) { System.out.println(s); }
      String items = parsed[0];
      int storeID = Integer.valueOf(parsed[1]);
      int custID = Integer.valueOf(parsed[2]);
      String purchaseDate = parsed[3];
      return new Purchase(storeID, custID, purchaseDate, items);
    }
  }

}
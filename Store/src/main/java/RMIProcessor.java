import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.PriorityQueue;
import org.javatuples.Pair;

public class RMIProcessor implements Runnable {
  private static final int NUM_ITEMS = 100000;
  private static final int NUM_STORES = 512;
  private final boolean DURABLE = false;
  private final int PERSISTENT = 1;
  private Connection conn;

  private String QUEUE_NAME;
  private int[][] itemByStore;


  public RMIProcessor(Connection conn, String QUEUE_NAME, int[][] itemByStore) {
    this.conn = conn;
    this.QUEUE_NAME = QUEUE_NAME;
    this.itemByStore = itemByStore;
  }

  @Override
  public void run() {
    try {
      final Channel channel = conn.createChannel();
      channel.queueDeclare(QUEUE_NAME, DURABLE, false, false, null);
      channel.queuePurge(QUEUE_NAME);
      channel.basicQos(1);

      Object monitor = new Object();

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
            .Builder()
            .deliveryMode(PERSISTENT)
            .correlationId(delivery.getProperties().getCorrelationId())
            .build();

        String response = "";
        try {
          String message = new String(delivery.getBody(), "UTF-8");
          String[] urlParts = message.split("/");
          int ID = Integer.parseInt(urlParts[2]);

          String res = message.contains("store") ? getTopTenItems(ID) : getTopFiveStores(ID);
          response += res;

        } catch (Exception e) {
          System.out.println(" [.] " + e.toString());
          channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
        } finally {
          channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          // RabbitMq consumer worker thread notifies the RPC server owner thread
          synchronized (monitor) {
            monitor.notify();
          }
        }
      };

      channel.basicConsume(QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
      // Wait and be prepared to consume the message from RPC client.
      while (true) {
        synchronized (monitor) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private String getTopTenItems(int storeID) {
    final int heapSize =10;
    int col = storeID - 1;

    PriorityQueue<Pair<Integer,Integer>> topTen = new PriorityQueue<>(heapSize, new PairComparator());
    for (int i=0; i<NUM_ITEMS; i++) {
      int numItems = itemByStore[i][col];
      if (i < heapSize) topTen.add(new Pair<>(i+1, numItems));
      else {
        assert topTen.peek() != null;
        // value1 in a Pair<> is numberOfItems
        if (topTen.peek().getValue1() <= numItems) {
          topTen.poll();
          topTen.add(new Pair<>(i + 1, numItems));
        }
      }
    }
    return writeResponseBody(topTen,10,"itemID");
  }

  private String getTopFiveStores(int itemID) {
    final int heapSize = 5;
    int row = itemID - 1;

    PriorityQueue<Pair<Integer,Integer>> topFive = new PriorityQueue<>(heapSize, new PairComparator());
    for (int i=0; i<NUM_STORES; i++) {
      int numItems = itemByStore[row][i];
      if (i<heapSize) topFive.add(new Pair<>(i+1, numItems));
      else {
        assert topFive.peek() != null;
        if (topFive.peek().getValue1() <= numItems) {
          topFive.poll();
          topFive.add(new Pair<>(i + 1, numItems));
        }
      }
    }
    return writeResponseBody(topFive,5,"storeID");
  }

  private String writeResponseBody(PriorityQueue<Pair<Integer,Integer>> topResult, int heapSize, String key) {
    StringBuilder res = new StringBuilder();
    res.append("{\"stores\": [" + System.lineSeparator());
    for (int i=0; i<heapSize; i++) {
      Pair<Integer,Integer> p = topResult.poll();
      res.append(String.format("\t{\"%s\":%d, \"numberOfItems\":%d}", key, p.getValue0(), p.getValue1()));
      res.append("," + System.lineSeparator());
    }
    res.delete(res.lastIndexOf(","),res.lastIndexOf(System.lineSeparator()));
    res.append("]}");
    return res.toString();
  }

}

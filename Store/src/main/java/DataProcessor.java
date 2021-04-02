import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import org.javatuples.Pair;

public class DataProcessor implements Runnable {

  private static final int NUM_ITEMS = 100000;
  private static final int NUM_STORES = 512;
  private Connection conn;
  private String QUEUE_NAME;
  private int[][] itemByStore;

  public DataProcessor(Connection conn, String QUEUE_NAME, int[][] cache) {
    this.conn = conn;
    this.QUEUE_NAME = QUEUE_NAME;
    this.itemByStore = cache;
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
          int row = i.getItemID() -1; int col = storeID -1;
          synchronized (itemByStore[row]) {
            itemByStore[row][col] += i.getNumberOfItems();
          }
        }
        // TODO: basicReject too? idk -> yes in exception
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        getTopFiveStores(101);
        getTopTenItems(1);
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

  private void simplePutCheck() {
    int total = 0;
    for (int[] item : itemByStore) {
      for (int quantity : item) {
        total += quantity;
      }
    }
    System.out.println("total quant" + total);
  }

  public String getTopTenItems(int storeID) {
    final int heapSize =10;
    int col = storeID - 1;

    PriorityQueue<Pair<Integer,Integer>> topTen = new PriorityQueue<>(heapSize);
    for (int i=0; i<heapSize; i++) {
      topTen.add(new Pair<>(i+1, itemByStore[i][col]));
    }
    for (int i=heapSize; i<NUM_ITEMS; i++) {
      if (topTen.peek().getValue1() <= itemByStore[i][col]) {
        topTen.poll();
        topTen.add(new Pair<>(i+1, itemByStore[i][col]));
      }
    }
    System.out.println(itemByStore[111][col]);
    return writeResponseBody(topTen, "itemID");
  }

  public String getTopFiveStores(int itemID) {
    final int heapSize = 5;
    int row = itemID - 1;

    PriorityQueue<Pair<Integer,Integer>> topFive = new PriorityQueue<>(heapSize);
    for (int i=0; i<heapSize; i++) {
      topFive.add(new Pair<>(i+1, itemByStore[row][i]));
    }
    for (int i=heapSize; i<NUM_STORES; i++) {
      if (topFive.peek().getValue1() <= itemByStore[row][i]) {
        topFive.poll();
        topFive.add(new Pair<>(i+1, itemByStore[row][i]));
      }
    }
    return writeResponseBody(topFive, "storeID");
  }

  private String writeResponseBody(PriorityQueue<Pair<Integer,Integer>> topResult, String key) {
    StringBuilder res = new StringBuilder();
    List<Pair<Integer,Integer>> formattedArr = new ArrayList<>(topResult);
    formattedArr.sort(new PairComparator());
    res.append("{\"stores\": [" + System.lineSeparator());
    for (Pair<Integer,Integer> p : formattedArr) {
      res.append(String.format("\t{\"%s\":%d, \"numberOfItems\":%d}", key, p.getValue0(), p.getValue1()));
      res.append("," + System.lineSeparator());
    }
    res.delete(res.lastIndexOf(","),res.lastIndexOf(System.lineSeparator()));
    res.append("]}");
    System.out.println(res.toString());
    return res.toString();
  }

}

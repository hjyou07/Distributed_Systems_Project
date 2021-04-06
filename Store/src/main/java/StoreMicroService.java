import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreMicroService {
  private static final String RPC_QUEUE_NAME = "getRequestQueue";
  private static final String QUEUE_NAME = "dataProcessor";
  private static final String EXCHANGE_NAME = "micro";
  private static final String USERNAME = "RABBIT_USERNAME";
  private static final String PASSWORD = "RABBIT_PASSWORD";
  private static final String HOST = "RABBIT_HOST";
  private static final String FILE_PATH = "/home/ec2-user/store/config.properties";
  private static final boolean isLocal = false;
  private static final int NUM_ITEMS = 100000;
  private static final int NUM_STORES = 512;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    if (isLocal) { factory.setHost("localhost"); } else {
      try (InputStream input = new FileInputStream(FILE_PATH)) {
        Properties prop = new Properties();
        // load properties file
        prop.load(input);
        factory.setUsername(prop.getProperty(USERNAME));
        factory.setPassword(prop.getProperty(PASSWORD));
        factory.setHost(prop.getProperty(HOST));
      } catch (IOException e) {
        e.printStackTrace();
        throw e;
      }
    }
    ExecutorService dataProcessorPool = Executors.newFixedThreadPool(500); // TODO: How should I configure this?

    Connection conn = null;
    Channel channel = null;
    try {
      conn = factory.newConnection(dataProcessorPool);
      channel = conn.createChannel();
      // prep for Purchases processing, dummy channel for exchangeDeclare and queueBind
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
      int[][] itemByStore = new int[NUM_ITEMS][NUM_STORES];
      // run threads for Purchase processing
      for (int i=0; i < 500; i++) {
        dataProcessorPool.execute(new DataProcessor(conn, QUEUE_NAME, itemByStore));
      }
      // run a thread RMI GET requests - my RMIProcessor is single-threaded
      new Thread(new RMIProcessor(conn, RPC_QUEUE_NAME, itemByStore)).start();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}

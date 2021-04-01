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
  private static final String FILE_PATH = "/Users/heej/Desktop/Spring2021/BSDS/Project/Purchases/src/main/resources/config.properties";
  private static final boolean isLocal = true;

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
    ExecutorService dataProcessorPool = Executors.newFixedThreadPool(10); // TODO: How should I configure this?


    Connection conn = null;
    Channel channel = null;
    try {
      conn = factory.newConnection(dataProcessorPool);
      channel = conn.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
      for (int i=0; i < 10; i++) {
        dataProcessorPool.execute(new DataProcessor(conn, QUEUE_NAME));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}

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

public class PurchasesMicroService {
  private static final String QUEUE_NAME = "dbWriter";
  private static final String EXCHANGE_NAME = "micro";
  private static final String USERNAME = "RABBIT_USERNAME";
  private static final String PASSWORD = "RABBIT_PASSWORD";
  private static final String HOST = "RABBIT_HOST";
  private static final String FILE_PATH = "/home/ec2-user/purchase/config.properties";
  private static final boolean isLocal = false;
  private static final boolean DURABLE = true;

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
    ExecutorService dbWriterPool = Executors.newFixedThreadPool(500);


    Connection conn = null;
    Channel channel = null;
    try {
      conn = factory.newConnection(dbWriterPool);
      channel = conn.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, DURABLE);
      channel.queueDeclare(QUEUE_NAME, DURABLE, false, false, null);
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
      for (int i=0; i < 500; i++) {
        dbWriterPool.execute(new DBWriter(conn, QUEUE_NAME));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}

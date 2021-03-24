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
  private static final String QUEUE_NAME = "dbwriter";
  private static final String EXCHANGE_NAME = "micro";
  private static final String USERNAME = "RABBIT_USERNAME";
  private static final String PASSWORD = "RABBIT_PASSWORD";
  private static final String HOST = "RABBIT_HOST";
  private static final String FILE_PATH = "/Users/heej/Desktop/Spring2021/BSDS/Project/Purchases/src/main/resources/config.properties";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    try (InputStream input = new FileInputStream(FILE_PATH)) {
      Properties prop = new Properties();
      // load a properties file
      prop.load(input);
      factory.setUsername(prop.getProperty(USERNAME));
      factory.setPassword(prop.getProperty(PASSWORD));
      factory.setHost(prop.getProperty(HOST));
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
    // factory.setHost("localhost"); // TODO: use properties file
    ExecutorService dbWriterPool = Executors.newFixedThreadPool(60); // match the dbcp poolSize


    Connection conn = null;
    try {
      conn = factory.newConnection(dbWriterPool);
      long Start = System.currentTimeMillis();
      for (int i=0; i < 65; i++) {
        dbWriterPool.execute(new DBWriter(conn, QUEUE_NAME, EXCHANGE_NAME));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    // wrap up, tells the ExecutorService not to accept any more tasks to run
    // dbWriterPool.shutdown();
    // blocks the calling thread until all threads are idle and no more work is waiting in the queue
    // dbWriterPool.awaitTermination(10, TimeUnit.SECONDS);
  }
}

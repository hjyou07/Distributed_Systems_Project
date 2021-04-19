import DAL.PurchaseDao;
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
import java.util.concurrent.TimeoutException;

public class PurchasesMicroService {
  private static final String QUEUE_NAME = "purchaseX";
  private static final String USERNAME = "RABBIT_USERNAME";
  private static final String PASSWORD = "RABBIT_PASSWORD";
  private static final String HOST = "RABBIT_HOST";
  private static final String HOST2 = "RABBIT_HOST2";
  private static final String HOST3 = "RABBIT_HOST3";
  private static final String FILE_PATH = "/home/ec2-user/purchase/config.properties";
  private static final boolean isLocal = false;
  private static final boolean DURABLE = false;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    ConnectionFactory factory2 = new ConnectionFactory();
    ConnectionFactory factory3 = new ConnectionFactory();
    if (isLocal) { factory.setHost("localhost"); } else {
      try (InputStream input = new FileInputStream(FILE_PATH)) {
        Properties prop = new Properties();
        // load properties file
        prop.load(input);
        factory.setUsername(prop.getProperty(USERNAME));
        factory.setPassword(prop.getProperty(PASSWORD));
        factory.setHost(prop.getProperty(HOST));
        factory2.setUsername(prop.getProperty(USERNAME));
        factory2.setPassword(prop.getProperty(PASSWORD));
        factory2.setHost(prop.getProperty(HOST2));
        factory3.setUsername(prop.getProperty(USERNAME));
        factory3.setPassword(prop.getProperty(PASSWORD));
        factory3.setHost(prop.getProperty(HOST3));
      } catch (IOException e) {
        e.printStackTrace();
        throw e;
      }
    }
    ExecutorService dbWriterPool = Executors.newFixedThreadPool(500);


    Connection conn=null, conn2=null, conn3=null;
    Channel channel=null, channel2=null, channel3=null;
    try {
      conn = factory.newConnection(dbWriterPool);
      conn2 = factory2.newConnection(dbWriterPool);
      conn3 = factory3.newConnection(dbWriterPool);
//      PurchaseDao dao = new PurchaseDao();
//      dao.createTable();

      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
      for (int i=0; i < 500; i++) {
        dbWriterPool.execute(new DBWriter(conn, QUEUE_NAME));
        dbWriterPool.execute(new DBWriter(conn2, QUEUE_NAME));
        dbWriterPool.execute(new DBWriter(conn3, QUEUE_NAME));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public static Connection connectToAllShards(Connection conn)
      throws IOException, TimeoutException {
    Channel dummyChannel = conn.createChannel();
    return conn;
  }

}

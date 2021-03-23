import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PurchasesMicroService {
  private static final String QUEUE_NAME = "dbwriter";
  private static final String EXCHANGE_NAME = "micro";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost"); // TODO: use properties file
    ExecutorService dbWriterPool = Executors.newFixedThreadPool(60); // match the dbcp poolSize

    try (Connection conn = factory.newConnection(dbWriterPool);) {
      for (int i=0; i < 65; i++) {
        dbWriterPool.execute(new DBWriter());
      }
    }
    // wrap up, tells the ExecutorService not to accept any more tasks to run
    dbWriterPool.shutdown();
    // blocks the calling thread until all threads are idle and no more work is waiting in the queue
    dbWriterPool.awaitTermination(10, TimeUnit.SECONDS);
  }
}

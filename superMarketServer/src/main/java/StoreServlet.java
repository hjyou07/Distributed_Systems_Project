import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "Servlet")
public class StoreServlet extends HttpServlet {

  private ConnectionFactory factory;
  private Connection conn;
  private ChannelFactory channelFactory = new ChannelFactory();
  private Channel dummychannel;
  private ObjectPool<Channel> channelPool;
  private static final String RPC_QUEUE_NAME = "getRequestQueue";
  private static final String REPLY_QUEUE = "getResponseQueue";
  private final String USERNAME = System.getProperty("RABBIT_USERNAME");
  private final String PASSWORD = System.getProperty("RABBIT_PASSWORD");
  private final String HOST = System.getProperty("RABBIT_HOST");
  private final boolean isLocal = false;
  private final boolean DURABLE = false;
  private final int PERSISTENT = 1;

  public class ChannelFactory extends BasePooledObjectFactory<Channel> {

    /**
     * Creates an object instance, to be wrapped in a PooledObject.
     *
     * @return Channel, for each rabbitmq connection
     * @throws Exception
     */
    @Override
    public Channel create() throws Exception {
      return conn.createChannel();
    }

    /**
     * Wrap the provided instance with a default implementation of PooledObject.
     *
     * @param channel
     * @return
     */
    @Override
    public PooledObject<Channel> wrap(Channel channel) {
      return new DefaultPooledObject<Channel>(channel);
    }
  }

  public void init() throws ServletException {
    factory = new ConnectionFactory();
    if (isLocal) {
      factory.setHost("localhost");
    } else {
      factory.setUsername(USERNAME);
      factory.setPassword(PASSWORD);
      factory.setHost(HOST);
    }

    dummychannel = null;
    try {
      conn = factory.newConnection();
      channelPool = new GenericObjectPool<>(channelFactory);
      dummychannel = channelPool.borrowObject();
      dummychannel.queueDeclare(RPC_QUEUE_NAME, DURABLE, false, false, null);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void destroy() {
    if (conn != null) {
      try {
        conn.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (channelPool != null) {
      channelPool.close();
    }
    if (dummychannel != null) {
      try {
        dummychannel.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    isTotalURLValid(urlPath, res, HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");

    final String corrId = UUID.randomUUID().toString();

    Channel channel = null;
    try {
      channel = channelPool.borrowObject();
      channel.queueDeclare(REPLY_QUEUE, DURABLE, false, false, null);
      AMQP.BasicProperties props = new AMQP.BasicProperties
          .Builder()
          .deliveryMode(PERSISTENT)
          .correlationId(corrId)
          .replyTo(REPLY_QUEUE)
          .build();
      channel.basicPublish("", RPC_QUEUE_NAME, props, urlPath.getBytes("UTF-8"));

      final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

      String ctag = channel.basicConsume(REPLY_QUEUE, true, (consumerTag, delivery) -> {
        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
          response.offer(new String(delivery.getBody(), "UTF-8"));
        }
      }, consumerTag -> {
      });

      String result = response.take();
      channel.basicCancel(ctag);

      if (result != null && result.length() != 0) {
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(result);
      } else {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("requested data not found");
      }
    } catch (Exception e) {
      e.printStackTrace();
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res.getWriter().write("unable to process query");
    } finally {
      if (channel != null) {
        try {
          channelPool.returnObject(channel);
        } catch (Exception e) {
          e.printStackTrace();
          // TODO: How should I handle this? -> swallow
        }
      }
    }
  }

  private String[] isTotalURLValid(String url, HttpServletResponse res, int resCode, String nullMessage) throws IOException {
    if (checkNull(url, res, resCode, nullMessage)) { return new String[]{}; }
    String[] parts = url.split("/");
    if (!isUrlValid(parts)) {
      res.setStatus(resCode);
      res.getWriter().write("Bad parameters in the url");
      return new String[]{};
    }
    return parts;
  }

  private boolean checkNull(String content, HttpServletResponse res, int resCode, String message) throws IOException {
    if ((content == null || content.isEmpty())) {
      res.setStatus(resCode);
      res.getWriter().write(message);
      return true;
    }
    return false;
  }

  private boolean isUrlValid(String[] urlParts) {
    // urlPath = "/items/store/{storeID}" or "/items/top5/{itemID}"
    // check for the [, store, 001] or [, top10, 101]
    // below checks for the ~/items/
    if (urlParts.length == 0 || urlParts.length != 3) return false;
    for (int i =0; i < urlParts.length; i++) {
      switch (i) {
        // actually, I don't really need to check for 0 because servlet mapping specifies /purchase/*
        case 1:
          if (!(isStringValid(urlParts[i], "store") || isStringValid(urlParts[i], "top5"))) { return false; }
          break;
        case 2:
          if (!isNumberValid(urlParts[i])) { return false; }
          break;
      }
    }
    return true;
  }

  private boolean isNumberValid(String string) {
    try {
      Integer.parseInt(string);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private boolean isStringValid(String string1, String string2) {
    return string1.equals(string2);
  }

  private boolean isDateValid(String dateInString) {
    try {
      LocalDate.parse(dateInString, DateTimeFormatter.BASIC_ISO_DATE);
      return true;
    } catch (DateTimeParseException e){
      return false;
    }
  }

}

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
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
public class PurchaseServlet extends HttpServlet {

  private ConnectionFactory factory;
  private Connection conn;
  private ChannelFactory channelFactory = new ChannelFactory();
  private Channel dummychannel;
  private ObjectPool<Channel> channelPool;
  private final String EXCHANGE_NAME = "micro";
  private final String USERNAME = System.getProperty("RABBIT_USERNAME");
  private final String PASSWORD = System.getProperty("RABBIT_PASSWORD");
  private final String HOST = System.getProperty("RABBIT_HOST");

    public class ChannelFactory extends BasePooledObjectFactory<Channel> {
      /**
       * Creates an object instance, to be wrapped in a PooledObject.
       * @return Channel, for each rabbitmq connection
       * @throws Exception
       */
      @Override
      public Channel create() throws Exception {
        return conn.createChannel();
      }

      /**
       * Wrap the provided instance with a default implementation of PooledObject.
       * @param channel
       * @return
       */
      @Override
      public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
      }
    }

  public void init() throws ServletException {
    // TODO 1: In the init() method, initialize the connection (this is the socket, so is slow)
    factory = new ConnectionFactory();
    boolean isLocal = true;
    if (isLocal) { factory.setHost("localhost"); } else {
      factory.setUsername(USERNAME);
      factory.setPassword(PASSWORD);
      factory.setHost(HOST);
    }

    dummychannel = null;
    try {
      conn = factory.newConnection();
      // TODO 2: create a channel pool that shares a bunch of pre-created channels
      channelPool = new GenericObjectPool<>(channelFactory);
      // TODO 4.1: create a dummy channel
      dummychannel = channelPool.borrowObject();
      // TODO 4.2: declare exchange: fanout
      dummychannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
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
    if (channelPool != null) { channelPool.close(); }
    // TODO 5: close dummy channel
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

    String[] urlParts = isTotalURLValid(urlPath, res, HttpServletResponse.SC_NOT_FOUND, "Missing parameters");
    
    if (urlParts.length != 0) {
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write("It works!");
      // do any sophisticated processing with urlParts which contains all the url params
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("plain/text");
    String urlPath = req.getPathInfo();
    // check the incoming url
    String[] urlParts = isTotalURLValid(urlPath, res, HttpServletResponse.SC_NOT_FOUND,
        "Missing parameters");
    if (urlParts.length == 0)
      return;

    String storeID = urlParts[1];
    String custID = urlParts[3];
    String purchaseDate = urlParts[5];

    // check request body, .lines() returns Stream<String>, .collect() returns String
    BufferedReader reqBodyBuffer = req.getReader();
    String reqBody = reqBodyBuffer.lines().collect(Collectors.joining());
    if (checkNull(reqBody, res, HttpServletResponse.SC_BAD_REQUEST, "Missing requestBody")) {
      return;
    }
    // find some char I can use as a delimiter that won't be in JSON string
    reqBody = reqBody.concat("#").concat(storeID).concat("#").concat(custID).concat("#").concat(purchaseDate);

    // TODO 3: In the dopost(), create a channel and use that to publish to RabbitMQ. Close it at end of the request
    Channel channel = null;
    try {
      channel = channelPool.borrowObject();
      // TODO 3.2: publish to the exchange
      channel.basicPublish(EXCHANGE_NAME, "", null, reqBody.getBytes("UTF-8"));
      // System.out.println(reqBody);
      // System.out.println("publish to exchange successful");
      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getWriter().write("Record passed to the database");
    } catch (Exception e) {
      e.printStackTrace();
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res.getWriter().write("Unable to publish to rabbitmq exchange");
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
    // TODO 4: Return appropriate response back - asynchronous?
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
    // urlPath = "/purchase/{storeID}/customer/{custID}/date/yyyymmdd"
    // check for the [, 001, customer, 001, date, 20210101]
    // below checks for the ~/purchase/
    if (urlParts.length == 0 || urlParts.length != 6) return false;
    for (int i =0; i < urlParts.length; i++) {
      switch (i) {
        // actually, I don't really need to check for 0 because servlet mapping specifies /purchase/*
        case 1: case 3:
          //System.out.println(urlParts[i]);
          if (!isNumberValid(urlParts[i])) { return false; }
          break;
        case 2:
          if (!isStringValid(urlParts[i], "customer")) { return false; }
          break;
        case 4:
          if (!isStringValid(urlParts[i], "date")) { return false; }
          break;
        case 5:
          if (!isDateValid(urlParts[i])) { return false; }
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

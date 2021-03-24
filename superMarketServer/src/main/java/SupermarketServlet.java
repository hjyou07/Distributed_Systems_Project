import com.google.gson.*;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.io.BufferedReader;
import java.sql.SQLException;
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
import obsolete.ProtoPurchase;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "Servlet")
public class SupermarketServlet extends HttpServlet {

  private ConnectionFactory factory;
  private Connection conn;
  private ChannelFactory channelFactory = new ChannelFactory();
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
    // factory.setHost("localhost"); // TODO: Consider using System.Property from catalina.properties
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    factory.setHost(HOST);

    try {
      conn = factory.newConnection();
      // TODO 4.1: create a dummy channel
    } catch (IOException e) {
      e.printStackTrace();
      // exit(1)
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
    // TODO 4.2: declare exchange
    // TODO 2: create a channel pool that shares a bunch of pre-created channels
    channelPool = new GenericObjectPool<>(channelFactory);
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
    // TODO 5: close dummy channel
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    String[] urlParts = isTotalURLValid(urlPath, res, HttpServletResponse.SC_NOT_FOUND, "Missing parameters");
    
    if (urlParts.length != 0) {
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write("It works!");
      // do any sophisticated processing with urlParts which contains all the url params TODO: process url params in 'urlParts'
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
    // I'm using pool instead. Simply call borrowObject to obtain the channel, and then call returnObject when we're done with it.
    // Do I need to define any behavior upon return?
    Channel channel = null;
    try {
      channel = channelPool.borrowObject();
      // TODO 3.1: declare exchange: fanout
      // TODO: 4: I can lift this off! Create a dummy channel in init just to declare the exchange
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
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

  private boolean isRequestValid(Purchase purchase) {
    // TODO: implement a validation for the ProtoPurchase POJO created by request body
    return true;
  }

  private Purchase readRequestBody(String reqBody, int storeID, int custID, String purchaseDate) {
    return new Purchase(storeID, custID, purchaseDate, reqBody);
  }

  private ProtoPurchase readRequestBody(String reqBody, HttpServletResponse res) throws IOException {
    try {
      ProtoPurchase purchase = new Gson().fromJson(reqBody, ProtoPurchase.class);
      return purchase;
    } catch(Exception e) {
      System.err.println(e.getMessage());
      res.getWriter().write("Bad requestBody, can't create a ProtoPurchase");
    }
    return null;
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

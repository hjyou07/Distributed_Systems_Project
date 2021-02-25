import com.google.gson.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Servlet")
public class SupermarketServlet extends HttpServlet {

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

    int storeID;
    int custID;
    String purchaseDate;

    String[] urlParts = isTotalURLValid(urlPath, res, HttpServletResponse.SC_NOT_FOUND,
        "Missing parameters");
    if (urlParts.length == 0)
      return;

    storeID = Integer.valueOf(urlParts[1]);
    custID = Integer.valueOf(urlParts[3]);
    purchaseDate = urlParts[5];

    // check request body, .lines() returns Stream<String>, .collect() returns String
    BufferedReader reqBodyBuffer = req.getReader();
    String reqBody = reqBodyBuffer.lines().collect(Collectors.joining());
    if (checkNull(reqBody, res, HttpServletResponse.SC_BAD_REQUEST, "Missing requestBody")) {
      return;
    }

    // now try creating the purchase POJO object from the json string
    Purchase purchase = readRequestBody(reqBody, storeID, custID, purchaseDate);

    if (!isRequestValid(purchase)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Bad requestBody");
      return;
    }

    try {
      PurchaseDao dao = new PurchaseDao();
      dao.createPurchaseInDB(purchase);
      res.setStatus(
          HttpServletResponse.SC_CREATED); // if url valid, status code: 201 Write Successful
      res.getWriter().write(String.format("echoing the request body: %s", purchase.items));
    } catch (SQLException e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("There was a problem writing to the DB");
    } catch (Exception e) {
      System.err.println("PurchaseDao can't be created");
      e.printStackTrace();
    }
  }

  private String[] isTotalURLValid(String url, HttpServletResponse res, int resCode, String nullMessage) throws IOException {
    if (checkNull(url, res, resCode, nullMessage)) { return new String[]{}; }
    String[] parts = url.split("/");
    if (!isUrlValid(parts)) {
      res.setStatus(resCode);
      res.getWriter().write("Bad parameters");
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
        case 1: case 3: case 5:
          //System.out.println(urlParts[i]);
          if (!isNumberValid(urlParts[i])) {
            return false;
          }
          break;
        case 2:
          if (!isStringValid(urlParts[i], "customer")) {
            return false;
          }
          break;
        case 4:
          if (!isStringValid(urlParts[i], "date")) {
            return false;
          }
        // TODO: implement a better validation method for date
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

}

import com.google.gson.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Servlet")
public class SupermarketServlet extends HttpServlet {

  protected void doPost(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("plain/text");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      System.out.println("Missing parameters");
      res.setStatus(HttpServletResponse.SC_NOT_FOUND); // status code: 404 Not Found
      res.getWriter().write("Missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    if (!isUrlValid(urlParts)) {
      System.out.println("Bad parameters");
      res.setStatus(HttpServletResponse.SC_NOT_FOUND); // if url not valid, status code: 404
      res.getWriter().write("Bad parameters");
      return;
    }

    // check request body
    BufferedReader reqBodyBuffer = req.getReader();
    // .lines() returns Stream<String>, .collect() returns String,
    // you can pass in an optional param in Collectors.joining() for a separator
    String reqBody = reqBodyBuffer.lines().collect(Collectors.joining());

    if (reqBody == null || reqBody.isEmpty()) {
      System.out.println("Missing requestBody");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST); // status code: 400, Invalid Inputs
      res.getWriter().write("Missing requestBody");
      return;
    }

    // now try creating the purchase POJO object from the json string
    Purchase purchase = readRequestBody(reqBody,res);

    if (!isRequestValid(purchase)) {
      System.out.println("Bad requestBody");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Bad requestBody");
    } else {
      res.setStatus(HttpServletResponse.SC_CREATED); // if url valid, status code: 201 Write Successful
      res.getWriter().write(String.format("echoing the request body: %s", purchase.itemList.toString()));
    }
  }

  private boolean isUrlValid(String[] urlParts) {
    // urlPath = "/purchase/{storeID}/customer/{custID}/date/yyyymmdd"
    // check for the [, 001, customer, 001, date, 20210101]
    // Stream.of(urlParts).forEach(System.out::println);
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
    // TODO: implement a validation for the Purchase POJO created by request body
    for (PurchaseItem item : purchase.itemList) {
      if (item.numberOfItems == 0) {
        return false;
      }
    }
    return true;
  }

  private Purchase readRequestBody(String reqBody, HttpServletResponse res) throws IOException {
    try {
      Purchase purchase = new Gson().fromJson(reqBody, Purchase.class);
      return purchase;
    } catch(Exception e) {
      System.err.println(e.getMessage());
      res.getWriter().write("Bad requestBody, can't create a Purchase");
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

  protected void doGet(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND); // status code: 404 Not Found
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND); // if url not valid, status code: 404
    } else {
      res.setStatus(HttpServletResponse.SC_OK); // if url valid, status code: 200 Ok
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in 'urlParts'
      res.getWriter().write("It works!");
    }
  }

}

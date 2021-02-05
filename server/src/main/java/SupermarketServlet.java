import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonParser;

@WebServlet(name = "Servlet")
public class SupermarketServlet extends HttpServlet {

  protected void doPost(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    // TODO: implement doPost method using similar URL validation and the API spec
    // the POST method to the same URL accepts a JSON request body. We can parse that information
    // with req.getReader() method which returns a BufferedReader object.
    // requestBody is a purchase = an array of items, where each item is something like:
    // {"itemID": "someString", "numberOfItems": 1}
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
    String reqBody = reqBodyBuffer.lines().collect(Collectors.joining(System.lineSeparator()));

    if (reqBody == null || reqBody.isEmpty()) {
      System.out.println("Missing requestBody");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST); // status code: 400, Invalid Inputs
      res.getWriter().write("Missing requestBody");
      return;
    }

    if (!isRequestValid(reqBody)) {
      System.out.println("Bad requestBody");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Bad requestBody");
    } else {
      res.setStatus(HttpServletResponse.SC_CREATED); // if url valid, status code: 201 Write Successful
      res.getWriter().write(String.format("echoing the request body: %s", reqBody));
    }
  }

  private boolean isUrlValid(String[] urlParts) {
    // TODO: validate the request url path according to the API specification from Assignment 1
    // urlPath = "/purchase/{storeID}/customer/{custID}/date/yyyymmdd"
    // check for the [, 001, customer, 001, date, 20210101]
    // Stream.of(urlParts).forEach(System.out::println);
    for (int i =0; i < urlParts.length; i++) {
      //System.out.println(i);
      switch (i) {
        // actually, I don't really need to check for 0 because servlet mapping specifies /purchase/*
        case 0:
          //System.out.println(!urlParts[i].isEmpty());
          if (!urlParts[i].isEmpty()) {
            return false;
          }
          break;
        case 1: case 3: case 5:
          //System.out.println(urlParts[i]);
          if (!isNumberValid(urlParts[i])) {
            return false;
          }
          break;
        case 2:
          //System.out.println(urlParts[i].equals("customer"));
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

  private boolean isRequestValid(String reqBody) {
    // TODO: implement a validation for request body
    // can I use gson?
    //JsonElement parseTree = JsonParser.parseString(reqBody);
    //System.out.println(parseTree.toString());
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

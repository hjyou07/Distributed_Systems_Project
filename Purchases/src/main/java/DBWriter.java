import java.sql.SQLException;

public class DBWriter implements Runnable {

  @Override
  public void run() {
    // TODO 4: Lift this up to the Purchase microservice (separate project)
    // now try creating the purchase POJO object from the json string
    Purchase purchase = readRequestBody(reqBody);

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

  private boolean isRequestValid(Purchase purchase) {
    // TODO: implement a validation for the ProtoPurchase POJO created by request body
    return true;
  }

  private Purchase readRequestBody(String reqBody) {
    String[] parsed = reqBody.split("#");
    for (String s : parsed) { System.out.println(s); }
    int storeID = Integer.valueOf(parsed[1]);
    int custID = Integer.valueOf(parsed[2]);
    String purchaseDate = parsed[3];
    return new Purchase(storeID, custID, purchaseDate, reqBody);
  }
}
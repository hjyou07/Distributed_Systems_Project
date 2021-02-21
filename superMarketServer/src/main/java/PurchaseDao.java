import java.sql.*;
import org.apache.commons.dbcp2.*;

/**
 * This Data Access Object class provides an abstract interface to 
 * some type of persistence mechanism that allows bridging the application server to the database
 */
public class PurchaseDao {
  private static BasicDataSource dataSource;

  public PurchaseDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  /**
   * Takes in a Purchase POJO, then insert it into MySQL database
   */
  public void createPurchaseInDB(Purchase purchase) {
    Connection conn = null;
    PreparedStatement insertStatement = null;
    String insertQuery = "INSERT INTO Purchase (storeID, custID, purchaseDate, items) " +
        "VALUES (?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      insertStatement = conn.prepareStatement(insertQuery);
      insertStatement.setInt(1, purchase.getStoreID());
      insertStatement.setInt(2, purchase.getCustID());
      insertStatement.setString(3, purchase.getPurchaseDate());
      insertStatement.setString(4, purchase.getItems());

      // execute insert SQL statement
      insertStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (insertStatement != null) {
          insertStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }

  /**
   * This main method tests the connection and insert query to local mysql database
   * @param args None
   */
  public static void main(String[] args) {
    PurchaseDao purchaseDao = new PurchaseDao();
    String items = "{\"items\":[{\"itemID\":\"101\",\"numItems\":\"1\"},{\"itemID\":\"102\",\"numItems\":\"1\"},{\"itemID\":\"103\",\"numItems\":\"1\"},{\"itemID\":\"104\",\"numItems\":\"1\"},{\"itemID\":\"105\",\"numItems\":\"1\"}]}";
    purchaseDao.createPurchaseInDB(new Purchase(1,1001,"20210101", items));
  }
}

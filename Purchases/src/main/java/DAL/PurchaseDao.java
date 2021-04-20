package DAL;

import Model.Purchase;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;

/**
 * This Data Access Object class provides an abstract interface to 
 * some type of persistence mechanism that allows bridging the application server to the database
 */
public class PurchaseDao {
  private static HikariDataSource dataSource;

  public PurchaseDao() {
    dataSource = DataSource.getDataSource();
  }

  public void createTable() throws SQLException {
    String createQuery = "create table Purchase ("
        + "storeID int not null,"
        + "custID int not null,"
        + "purchaseDate varchar(255) not null,"
        + "items JSON)";
    try (Connection conn = dataSource.getConnection();
    PreparedStatement createStatement = conn.prepareStatement(createQuery)) {
      createStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
  }
  /**
   * Takes in a Purchase POJO, then insert it into MySQL database
   */
  public void createPurchaseInDB(Purchase purchase) throws SQLException {
    String insertQuery = "INSERT INTO Purchase (storeID, custID, purchaseDate, items) " +
        "VALUES (?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement insertStatement = conn.prepareStatement(insertQuery);) {
      insertStatement.setInt(1, purchase.getStoreID());
      insertStatement.setInt(2, purchase.getCustID());
      insertStatement.setString(3, purchase.getPurchaseDate());
      insertStatement.setString(4, purchase.getItems());

      // execute insert SQL statement
      insertStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * This main method tests the connection and insert query to local mysql database
   * @param args None
   */
  public static void main(String[] args) throws SQLException {
    PurchaseDao purchaseDao = new PurchaseDao();
    String items = "{\"items\":[{\"itemID\":\"101\",\"numItems\":\"1\"},{\"itemID\":\"102\",\"numItems\":\"1\"},{\"itemID\":\"103\",\"numItems\":\"1\"},{\"itemID\":\"104\",\"numItems\":\"1\"},{\"itemID\":\"105\",\"numItems\":\"1\"}]}";
    purchaseDao.createPurchaseInDB(new Purchase(1,1001,"20210101", items));
  }
}

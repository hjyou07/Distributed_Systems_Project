package DAL;

import Model.Purchase;
import com.amazonaws.regions.Regions;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DynamoDao {

  private static DynamoDB dynamo;

  public DynamoDao() {
    this.dynamo = DynamoDataSource.getDynamoDB();
  }

  public void createPurchaseInDB(Purchase purchase) {
    Table table = dynamo.getTable("Purchase");

    int storeID = purchase.getStoreID();
    int custID = purchase.getCustID();
    String purchaseDate = purchase.getPurchaseDate();
    String items = purchase.getItems();

    try {
      table.putItem(
              new Item().withPrimaryKey("storeID", storeID, "custID", custID)
                  .withString("purchaseDate", purchaseDate).withString("items", items));
    } catch (Exception e) {
      System.err.println("Unable to add item: " + storeID + " " + custID + items);
      System.err.println(e.getMessage());
    }
  }

  /**
   * This main method tests the connection and insert query to aws dynamoDB instance
   * @param args None
   */
  public static void main(String[] args) {
    DynamoDao dynamoDao = new DynamoDao();
    String items = "{\"items\":[{\"ItemID\":\"101\",\"numberOfItems\":\"1\"}]}";
    dynamoDao.createPurchaseInDB(new Purchase(1,1001,"20210101", items));
  }

}

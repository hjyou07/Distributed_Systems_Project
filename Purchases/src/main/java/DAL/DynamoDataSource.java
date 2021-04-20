package DAL;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import java.util.HashMap;
import java.util.Map;

public class DynamoDataSource {
  private static DynamoDB dynamoDB;

  static {
    try {
      AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_EAST_1)
          .build();

      dynamoDB = new DynamoDB(client);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

  }

  public static DynamoDB getDynamoDB() { return dynamoDB; }
}

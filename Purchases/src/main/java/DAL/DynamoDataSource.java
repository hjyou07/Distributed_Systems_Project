package DAL;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class DynamoDataSource {
  private static DynamoDB dynamoDB;

  static {
    try {
      AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_EAST_1).withCredentials(new InstanceProfileCredentialsProvider(false))
          .build();

      dynamoDB = new DynamoDB(client);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

  }

  public static DynamoDB getDynamoDB() { return dynamoDB; }
}

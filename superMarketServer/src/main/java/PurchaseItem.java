import com.google.gson.*;
import com.google.gson.annotations.*;

public class PurchaseItem {
  @SerializedName("ItemID")
  protected String itemID;
  @SerializedName("numberOfItems:")
  protected int numberOfItems;

  public PurchaseItem(String itemID, int numberOfItems) {
    this.itemID = itemID;
    this.numberOfItems = numberOfItems;
  }

  @Override
  public String toString() {
    return "PurchaseItem{" +
        "itemID='" + itemID + '\'' +
        ", numberOfItems=" + numberOfItems +
        '}';
  }
}

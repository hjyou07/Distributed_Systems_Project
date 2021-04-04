import com.google.gson.annotations.*;
import java.util.Objects;

public class PurchaseItems {
  @SerializedName("ItemID")
  protected Integer itemID;
  @SerializedName("numberOfItems:")
  protected Integer numberOfItems;

  public PurchaseItems(int itemID, int numberOfItems) {
    this.itemID = itemID;
    this.numberOfItems = numberOfItems;
  }

  public Integer getItemID() {
    return itemID;
  }

  public void setItemID(int itemID) {
    this.itemID = itemID;
  }

  public Integer getNumberOfItems() {
    return numberOfItems;
  }

  public void setNumberOfItems(int numberOfItems) {
    this.numberOfItems = numberOfItems;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PurchaseItems purchaseItems = (PurchaseItems) o;
    return Objects.equals(this.itemID, purchaseItems.itemID) &&
        Objects.equals(this.numberOfItems, purchaseItems.numberOfItems);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemID, numberOfItems);
  }

  @Override
  public String toString() {
    return "PurchaseItems{" +
        "itemID='" + itemID + '\'' +
        ", numberOfItems=" + numberOfItems +
        '}';
  }
}

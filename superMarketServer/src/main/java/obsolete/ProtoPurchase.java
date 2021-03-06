package obsolete;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 This pojo class represents a Purchase model that is compatible with Swagger API
 */
public class ProtoPurchase {
  @SerializedName("items")
  protected List<PurchaseItems> items = null;

  public ProtoPurchase items(List<PurchaseItems> items) {
    this.items = items;
    return this;
  }

  public ProtoPurchase addItemsItem(PurchaseItems itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<PurchaseItems>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
   **/
  public List<PurchaseItems> getItems() {
    return items;
  }

  /**
   * Set Items given a List of PurchaseItems
   * @param items
   */
  public void setItems(List<PurchaseItems> items) {
    this.items = items;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProtoPurchase purchase = (ProtoPurchase) o;
    return Objects.equals(this.items, purchase.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProtoPurchase {\n");

    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

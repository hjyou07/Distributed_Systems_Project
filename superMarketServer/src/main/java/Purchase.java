import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Purchase {
  @SerializedName("items")
  protected List<PurchaseItem> itemList;

  public Purchase(List<PurchaseItem> itemList) {
    this.itemList = itemList;
  }
}

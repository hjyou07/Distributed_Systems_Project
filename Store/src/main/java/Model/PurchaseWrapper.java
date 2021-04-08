package Model;

import Model.PurchaseItems;
import java.time.LocalDate;
import java.util.List;

public class PurchaseWrapper {
  private int storeID;
  private int custID;
  private LocalDate purchaseDate;
  private List<PurchaseItems> purchaseItems;

  public PurchaseWrapper(int storeID, int custID, LocalDate purchaseDate,
      List<PurchaseItems> purchaseItems) {
    this.storeID = storeID;
    this.custID = custID;
    this.purchaseDate = purchaseDate;
    this.purchaseItems = purchaseItems;
  }

  public int getStoreID() {
    return storeID;
  }

  public int getCustID() {
    return custID;
  }

  public LocalDate getPurchaseDate() {
    return purchaseDate;
  }

  public List<PurchaseItems> getPurchaseItems() {
    return purchaseItems;
  }

  @Override
  public String toString() {
    return "PurchaseWrapper{" +
        "storeID=" + storeID +
        ", custID=" + custID +
        ", purchaseDate=" + purchaseDate +
        ", purchaseItems=" + purchaseItems +
        '}';
  }
}

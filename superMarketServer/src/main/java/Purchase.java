/**
  This pojo class represents a Purchase model that is compatible with my current DB schema
 */
public class Purchase {
  private int storeID;
  private int custID;
  private String purchaseDate;
  String items;

  public Purchase(int storeID, int custID, String purchaseDate, String items) {
    this.storeID = storeID;
    this.custID = custID;
    this.purchaseDate = purchaseDate;
    this.items = items;
  }

  public int getStoreID() {
    return storeID;
  }

  public int getCustID() {
    return custID;
  }

  public String getPurchaseDate() {
    return purchaseDate;
  }

  public String getItems() {
    return items;
  }
}
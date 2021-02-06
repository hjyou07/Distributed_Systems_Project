public class PurchaseCounter {
  private int count = 0;

  synchronized public void inc() {
    count++;
  }

  public int getCount() {
    return this.count;
  }
}

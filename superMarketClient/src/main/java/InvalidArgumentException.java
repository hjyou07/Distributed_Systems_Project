public class InvalidArgumentException extends Exception {

  public InvalidArgumentException() {
    super();
  }

  protected String getExceptionMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append("Please provide all parameters; or only maxStores, date, and IP address, "
        + "in the correct format. The latter option will fill in the rest with default values."
        + System.lineSeparator());
    builder.append("    Input arguments in the following order: " + System.lineSeparator());
    builder.append("    maxStores(maximum number of stores to simulate)" + System.lineSeparator());
    builder.append("    numCustomer(default=1000; number of customers per store)" + System.lineSeparator());
    builder.append("    maxItemID(default=100000; numeric maximum itemID allowed)" + System.lineSeparator());
    builder.append("    numPurchases(default=60; number of purchase per hour)" + System.lineSeparator());
    builder.append("    numPurchaseItems(default=5, range=1~20; number of items for each purchase)" + System.lineSeparator());
    builder.append("    date(provide in YYYYMMDD format)" + System.lineSeparator());
    builder.append("    serverAddress(provide in [serverIP]:[portnumber] format)" + System.lineSeparator());
    return builder.toString();
  }
}

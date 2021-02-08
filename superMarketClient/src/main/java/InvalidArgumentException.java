public class InvalidArgumentException extends Exception {

  public InvalidArgumentException() {
    super("Please provide all parameters, or only maxStores, date, and IP address, in the correct format");
  }
}

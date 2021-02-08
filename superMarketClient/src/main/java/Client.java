import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
  private final static int PHASE_BARRIER = 1;
  private static int maxStores;  // Run with 32, 64, 128, 256 threads,
  private static String date;
  private static String serverAddress;
  private static int numCust;
  private static int maxItemID;
  private static int numPurchases;
  private static int numPurchaseItems;

  private static PurchaseCounter purchaseCounter;
  private static PurchaseCounter badPurchaseCounter;
  private static long threadStartTime;
  private static long threadEndTime;

  public static void main(String[] args) throws InterruptedException, InvalidArgumentException {
    // TODO: Read in the parameters
    parseArgs(args);
    // create the global counter
    // TODO: Avoid doing this in part 2
    purchaseCounter = new PurchaseCounter();
    badPurchaseCounter = new PurchaseCounter();
    // create latch to pass it into every store thread
    CountDownLatch centralPhaseSignal = new CountDownLatch(PHASE_BARRIER);
    CountDownLatch westPhaseSignal = new CountDownLatch(PHASE_BARRIER);
    CountDownLatch closeSignal = new CountDownLatch(maxStores);

    int eastStores = maxStores/4;
    int centralStores = maxStores/4;
    int westStores = maxStores/2;

    // create runnables and assign storeID
    Store[] storeThreads = new Store[maxStores];
    for (int i=0; i < maxStores; i++) {
      storeThreads[i] = new Store(i,purchaseCounter, badPurchaseCounter, centralPhaseSignal,westPhaseSignal,closeSignal);
      if (date != null && serverAddress != null) {
        storeThreads[i].setServerAddress(serverAddress);
        storeThreads[i].setDate(date);
      }
      if (numCust != 0 && maxItemID != 0 && numPurchases != 0 && numPurchaseItems != 0) {
        storeThreads[i].setNumCust(numCust);
        storeThreads[i].setMaxItemID(maxItemID);
        storeThreads[i].setNumPurchases(numPurchases);
        storeThreads[i].setNumPurchaseItems(numPurchaseItems);
      }
    }

    // note the indices of the starting store of each phase
    int firstCentralStore = eastStores;
    int firstWestStore = eastStores + centralStores;

    // take timestamp
    threadStartTime = System.currentTimeMillis();
    // create and start the threads
    // east phase launch
    for (int i=0; i < eastStores; i++) {
      new Thread(storeThreads[i]).start();
    }
    centralPhaseSignal.await();
    // central phase launch when central latch goes down
    for (int i = firstCentralStore; i < firstWestStore; i++) {
      new Thread(storeThreads[i]).start();
    }
    westPhaseSignal.await();
    // west phase launch when west latch goes down
    for (int i = firstWestStore; i < maxStores; i++) {
      new Thread(storeThreads[i]).start();
    }
    // wait for ALL the stores to close with the closeSignal latch
    closeSignal.await();
    // take timestamp
    threadEndTime = System.currentTimeMillis();
    System.out.println(report());
  }

  private static void parseArgs(String[] args) throws InvalidArgumentException {
    // force the user to either input all parameters, or provide only maxStores, date, and IP address
    // but for dev purposes, I'm allowing no arguments to run it on localhost with default config
    if (args.length != 3 && args.length != 7 && args.length != 0) {
      throwCustomException();
    } else if (args.length == 3) {
      parseThreeArgs(args);
    } else {
      parseAllArgs(args);
    }
  }

  private static void parseThreeArgs(String[] args) throws InvalidArgumentException {
    try {
      for (int i = 0; i < 3; i++) {
        switch (i) {
          case 0:
            maxStores = Integer.parseInt(args[i]);
            break;
          case 1:
            date = args[i];
            break;
          case 2:
            serverAddress = "http://".concat(args[i]).concat("/superMarketServer_war");
            validateServerAddr(serverAddress);
            break;
        }
      }
    } catch(Exception e) {
      throwCustomException();
    }
  }

  private static void parseAllArgs(String[] args) throws InvalidArgumentException {
    try {
      for (int i = 0; i < 7; i++) {
        switch (i) {
          case 0:
            maxStores = Integer.parseInt(args[i]);
            break;
          case 1:
            numCust = Integer.parseInt(args[i]);
            break;
          case 2:
            maxItemID = Integer.parseInt(args[i]);
            break;
          case 3:
            numPurchases = Integer.parseInt(args[i]);
            break;
          case 4:
            limitInputRange(1, 20, args[i]);
            numPurchaseItems = Integer.parseInt(args[i]);
            break;
          case 5:
            date = args[i];
            break;
          case 6:
            serverAddress = "http://".concat(args[i]).concat("/superMarketServer_war");
            validateServerAddr(serverAddress);
            break;
        }
      }
    } catch(Exception e) {
      throwCustomException();
    }
  }

  private static void throwCustomException() throws InvalidArgumentException {
    InvalidArgumentException e = new InvalidArgumentException();
    System.err.println(e.getExceptionMessage());
    throw e;
  }

  // test 21, and some string that will cause parseInt to fail
  private static void limitInputRange(int low, int high, String arg)
      throws InvalidArgumentException {
     int input = Integer.parseInt(arg);
     if (input < low || input > high) {
       throw new InvalidArgumentException();
     }
  }

  private static void validateServerAddr(String address) throws InvalidArgumentException {
    String pattern = "(^http://)(\\d+\\.+\\d+\\.+\\d+\\.+\\d+)(:\\d+)(/superMarketServer_war$)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(address);
//    System.out.println(r.toString());
//    System.out.println(m.matches());
    if (!(m.matches() || address.contains("localhost"))) {
      throw new InvalidArgumentException();
    }
  }

  // TODO: check date format

  private static String report() {
    StringBuilder reportBuilder = new StringBuilder();
    reportBuilder.append("number of stores: " + maxStores);
    reportBuilder.append("\ntotal number of successful requests: " + purchaseCounter.getCount());
    // TODO: Figure out a way to keep track of number of requests
    reportBuilder.append("\ntotal number of unsuccessful requests: " + (badPurchaseCounter.getCount()));
    reportBuilder.append("\ntotal wall time (sec): " + calculateWallTime());
    reportBuilder.append("\nthroughput (requests/sec): " + calculateThroughput(purchaseCounter.getCount(), calculateWallTime()));
    return reportBuilder.toString();
  }

  private static long calculateWallTime() {
    // timestamps are in milliseconds, convert to seconds
    return ((threadEndTime - threadStartTime) / 1000);
  }

  private static double calculateThroughput(int numRequest, long wallTime) {
    return numRequest/wallTime;
  }

}

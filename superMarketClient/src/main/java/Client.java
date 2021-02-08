import java.util.concurrent.CountDownLatch;

public class Client {
  private final static int PHASE_BARRIER = 1;
  private static int maxStores = 32;  // Run with 32, 64, 128, 256 threads
  private static String date;
  private static String serverAddress;

  private static PurchaseCounter purchaseCounter;
  private static PurchaseCounter badPurchaseCounter;
  private static long threadStartTime;
  private static long threadEndTime;

  public static void main(String[] args) throws InterruptedException, InvalidArgumentException {
    // TODO: Read in the parameters -> how? just parse thru args? For now, I'm just manually gonna set it
    parseArgs(args);
    // create the global counter
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
      // TODO: all the setters should call in values from the command line
      // the correcnt serverAddress is: http://localhost:8080/superMarketServer_war_exploded
      if (date != null && serverAddress != null) {
        storeThreads[i].setServerAddress(serverAddress);
        storeThreads[i].setDate(date);
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
    if (args.length != 3 && args.length != 7 && args.length != 0) {
      InvalidArgumentException e = new InvalidArgumentException();
      System.err.println(e.getMessage());
      throw e;
    } else if (args.length == 3) {
      for (int i=0; i < 3; i++) {
        switch (i) {
          case 0:
            maxStores = Integer.parseInt(args[i]);
            break;
          case 1:
            date = args[i];
            break;
          case 2:
            serverAddress = args[i].concat("/superMarketServer_war_exploded");
            break;
        }
      }
    } //TODO: handle the case where the user inputs all the params
  }

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

import Model.LatencyBucket;

public class DataProcessor {
  /* 5. DataProcessor calculates the following:
  - mean response time for POSTs (millisecs)
  - median response time for POSTs (millisecs)
  - the total wall time
  - throughput = requests per second = total number of requests/wall time
  - p99 (99th percentile) response time for POSTs [Here’s a nice article](https://www.elastic.co/blog/averages-can-dangerous-use-percentile) about why percentiles are important and why calculating them is not always easy.
  - max response time for POSTs
   */
  // needs to access Client's threadStartTime and threadEndTime
  private int median;
  private int p99;
  private int max;
  private int wallTime;
  private int throughput;
  private LatencyBucket latencyBucket;
  private int[] cumulBucket;

  public DataProcessor(LatencyBucket latencyBucket) {
    this.latencyBucket = latencyBucket;
    //cumulBucket = latencyBucket.makeCumulativeBucket();
  }

  public double calculateMean() {
    int[] counterBucket = latencyBucket.getCounterBucket();
    int total = 0;
    for (int i=0; i < counterBucket.length; i++) {
      total += i * counterBucket[i];
    }
    int numRequest = getSuccess() + getFailure();
    // cumulBucket holds all the number of requests at the end of the array
    // assert(cumulBucket[cumulBucket.length-1] == (getSuccess()+getFailure()));
    return total/numRequest;
  }

  public int calculateMedian() {
    int[] counterBucket = latencyBucket.getCounterBucket();
    int median = (getSuccess() + getFailure()) / 2;
    System.out.println(median);
    int nthRequest = 0;
    for (int i=0; i < counterBucket.length; i++) {
      nthRequest += counterBucket[i];
      if (nthRequest >= median) {
        return i;
      }
    }
    System.out.println("nth request is at: " + nthRequest);
    return -1;
  }

  public long calculateWallTime() {
    // timestamps are in milliseconds, convert to seconds
    return ((Client.threadEndTime - Client.threadStartTime) / 1000);
  }

  public double calculateThroughput(long wallTime) {
    int numRequest = getSuccess() + getFailure();
    return numRequest/wallTime;
  }

  public int getSuccess() {
    return latencyBucket.getSuccessCount();
  }

  public int getFailure() {
    return latencyBucket.getFailureCount();
  }

}

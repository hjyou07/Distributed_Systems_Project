import Model.LatencyBucket;

public class DataProcessor {
  // 5. DataProcessor calculates the stat
  // needs to access Client's threadStartTime and threadEndTime
  private int p99;
  private int max;
  private int wallTime;
  private int throughput;
  private LatencyBucket latencyBucket;
  private int[] counterBucket;

  public DataProcessor(LatencyBucket latencyBucket) {
    this.latencyBucket = latencyBucket;
    this.counterBucket = latencyBucket.getCounterBucket();
  }

  public double calculateMean() {
    int total = 0;
    for (int i=0; i < counterBucket.length; i++) {
      total += i * counterBucket[i];
    }
    int numRequest = getSuccess() + getFailure();
    System.out.println(total);
    System.out.println(numRequest);
    return total/numRequest;
  }

  public int calculatePercentile(double percentile) {
    double p = percentile * (getSuccess() + getFailure());
    int nthRequest = 0;
    int i = 0;
    while (nthRequest < p) {
      nthRequest += counterBucket[i++];
    }
    return i-1;
  }

  public int calculateMedian() {
    return calculatePercentile(0.5);
  }

  public int calculateP99() {
    return calculatePercentile(0.99);
  }

  public int getMaximum() {
    return calculatePercentile(1);
  }

  public int getMinimum() {
    int nthRequest = 0;
    int i = 0;
    while (nthRequest < 1) {
      nthRequest += counterBucket[i++];
    }
    return i-1;
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

  public void printCounterBucket() {
    for (int i=0; i<300; i++) {
       System.out.println(counterBucket[i]);
    }
  }

}
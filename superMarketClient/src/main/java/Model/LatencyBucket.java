package Model;

public class LatencyBucket {
  // counterBucket stores the number of requests with i(ms) latency
  // where i is the index of the bucket (1 ~ 10000).
  // e.g. bucket[10] = 5 means there's 5 requests with latency of 10ms
  // bucket size is 10000 representing the timeout for a http request.
  private final static int TIME_OUT = 100000;
  private int[] counterBucket = new int[TIME_OUT];
  private int successCount;
  private int failureCount;

  public void putInBucket(Response pojoResponse) {
    // I assume that latency can't be bigger than 10000 anyways, so casting into int
    int i = (int) pojoResponse.getLatency();
    (counterBucket[i])++;
    countSuccess(pojoResponse);
  }

  private void countSuccess(Response pojoResponse) {
    if (pojoResponse.getResponseCode() == 201) {
      successCount++;
    } else {
      failureCount++;
    }
  }

  public int[] getCounterBucket() {
    return counterBucket;
  }

  public int getSuccessCount() {
    return successCount;
  }

  public int getFailureCount() {
    return failureCount;
  }

}

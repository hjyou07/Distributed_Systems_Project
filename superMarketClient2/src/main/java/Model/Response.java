package Model;

public class Response {
  // 1. Create a Response pojo class to capture the below information
  private long start;
  private long end;
  private String requestType;
  private int responseCode;

  public Response(long start, long end, String requestType, int responseCode) {
    this.start = start;
    this.end = end;
    this.requestType = requestType;
    this.responseCode = responseCode;
  }

  public long getStart() {
    return start;
  }

  public long getLatency() {
    return end - start; // note this is in milliseconds
  }

  public String getRequestType() {
    return requestType;
  }

  public int getResponseCode() {
    return responseCode;
  }
}

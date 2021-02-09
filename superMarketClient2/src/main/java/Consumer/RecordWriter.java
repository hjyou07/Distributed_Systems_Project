package Consumer;

import Model.Response;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.BlockingQueue;

public class RecordWriter implements Runnable {
  // 3. RecordWriter takes in the Response object from the queue (consumer)
  // and start writing to a csv file
  private final BlockingQueue<Response> csvBuffer;
  private static final String CSV_FILE_PATH = "./requestResult.csv";
  private BufferedWriter writer;

  public RecordWriter(BlockingQueue csvBuffer) {
    this.csvBuffer = csvBuffer;
  }

  @Override
  public void run() {
    try {
      initializeCSV();
      Response pojoResponse;
      while (!((pojoResponse = csvBuffer.take()).getRequestType().equals("EXIT"))) {
        String line = formatToCSV(pojoResponse);
        writeToCSV(line);
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  private LocalDateTime convertEpochToDate(long epochSecond) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochSecond), ZoneId.systemDefault());
  }

  private String convertEpochToTimestamp(long epochMilli) {
    return (new Timestamp(epochMilli)).toString();
  }

  public String formatToCSV(Response pojoResponse) {
    String startTime = convertEpochToTimestamp(pojoResponse.getStart());
    // uncomment below if you want the date format of epoch milliseconds
    // String startTime = String.valueOf(convertEpochToDate(pojoResponse.getStart()));
    String requestType = pojoResponse.getRequestType();
    String latency = String.valueOf(pojoResponse.getLatency());
    String responseCode = String.valueOf(pojoResponse.getResponseCode());
    String[] data = new String[]{startTime, requestType, latency, responseCode};
    return String.join(",", data);
  }

  public void initializeCSV() throws IOException {
    File file = new File(CSV_FILE_PATH);
    if (!file.exists()) file.createNewFile();
    writer = new BufferedWriter(new FileWriter(file));
    String header = "start_time, request_type, latency(ms), response_code";
    writer.write(header);
    writer.newLine();
  }

  public void writeToCSV(String line) throws IOException {
    writer.write(line);
    writer.newLine();
  }
}

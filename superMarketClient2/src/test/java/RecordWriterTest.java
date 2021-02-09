
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Before;
import org.junit.Test;


 /* To test this class, edit the access modifiers to the methods
public class RecordWriterTest {
  private Response pojoResponse;
  private BlockingQueue<Response> csvBuffer;
  private RecordWriter csvWriter;

  @Before
  public void setUp() throws Exception {
    pojoResponse = new Response(0,5,"POST", 201);
    csvBuffer = new LinkedBlockingQueue();
    csvWriter = new RecordWriter(csvBuffer);
  }

  @Test
  public void initializeCSV() throws IOException {
    csvWriter.initializeCSV();
  }

  @Test
  public void formatToCSV() {
    String line = csvWriter.formatToCSV(pojoResponse);
    System.out.println(line);
  }

  @Test
  public void writeToCSV() throws IOException {
    csvWriter.initializeCSV();
    String line = csvWriter.formatToCSV(pojoResponse);
    csvWriter.writeToCSV(line);
    // for this method test to actually write to a file, i need to call writer.close() within the method
  }
}
 */
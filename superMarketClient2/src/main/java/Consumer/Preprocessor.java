package Consumer;

import Model.*;
import java.util.concurrent.BlockingQueue;

public class Preprocessor implements Runnable {
  // 4. Preprocessor takes the Response object from the queue(another queue, another consumer)
  // and start putting in to an array of buckets -> like counting sort
  // array of 1000 slots, each index representing an increment of millisecond, and I will count the
  // number of latencies that falls into each bucket
  private final BlockingQueue<Response> preprocessBuffer;

  public Preprocessor(BlockingQueue preprocessBuffer) {
    this.preprocessBuffer = preprocessBuffer;
  }

  @Override
  public void run() {

  }
}

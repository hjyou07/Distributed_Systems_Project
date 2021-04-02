//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.PriorityQueue;
//import org.javatuples.Pair;
//
///**
// * Wrapper class for the internal data structure to save the store information
// * Uses singleton pattern to have one global copy for all access
// * Each row is synchronized for multithreaded write operations
// */
//public class StoreInfo {
//
//  private StoreInfo() {}
//
//  /**
//   * return top 10 items sold at a particular store in json format of
//   * {"stores": [{"itemID":0, "numberOfItems": 0},{},...{}]}
//   * @param storeID storeID of interest
//   * @return String top 10 items listed in json format
//   */
//  public String getTopTenItems(int storeID) {
//    StringBuilder res = new StringBuilder();
//    res.append("{\"stores\": [" + System.lineSeparator());
//    Integer[] storeCol = Arrays.stream(itemByStore).map(itemByStore -> itemByStore[storeID - 1]).toArray(Integer[]::new);
//    PriorityQueue<Pair<Integer,Integer>> topTen = fetchTopElements(10, storeCol);
//    for (Pair<Integer,Integer> p : topTen) {
//      // append the {"itemID":0,"numberOfItems":0}
//      res.append(String.format("{\"itemID\":%d, \"numberOfItems\":%d}", p.getValue0(), p.getValue1()));
//      res.append("," + System.lineSeparator());
//    }
//    res.delete(res.lastIndexOf(","),res.lastIndexOf(System.lineSeparator()));
//    res.append("]}");
//    System.out.println(res.toString());
//    return res.toString();
//  }
//
//  /**
//   * return top 5 stores for sales of a particular item in json format of
//   * {"stores": [{"storeID":0, "numberOfItems": 0},{},...{}]}
//   * @param itemID itemID of interest
//   * @return String top 5 stores listed in json format
//   */
//  public String getTopFiveStores(int itemID, Integer[][] itemByStore) {
//    int row = itemID - 1;
//    StringBuilder res = new StringBuilder();
//    res.append("{\"stores\": [" + System.lineSeparator());
//    PriorityQueue<Pair<Integer,Integer>> topFive = fetchTopElements(5, itemByStore[row]);
//    for (Pair<Integer,Integer> p : topFive) {
//      // append the {"storeID":0,"numberOfItems":0}
//      res.append(String.format("{\"storeID\":%d, \"numberOfItems\":%d}", p.getValue0(), p.getValue1()));
//      res.append("," + System.lineSeparator());
//    }
//    res.delete(res.lastIndexOf(","),res.lastIndexOf(System.lineSeparator()));
//    res.append("]}");
//    System.out.println(res.toString());
//    return res.toString();
//  }
//
//  public void putSalesInfo(Integer[][] itemByStore, int storeID, int itemID, int quantity) {
//    int row = itemID - 1;
//    int col = storeID - 1;
//    System.out.println("inside putSalesInfo()");
//    synchronized (itemByStore[row]) {
//      System.out.println("inside sync block");
//      itemByStore[row][col] += quantity;
//      System.out.println("finished updating");
//    }
//  }
//
//  /**
//   * Helper method to fetch the top 10 items or top 5 stores that utilizes a min heap of limited size
//   * @param heapSize size of the heap, either 10 or 5 depending on where it is called
//   * @param rowOrCol integer array of either itemIDs or storeIDs
//   * @return PriorityQueue of a pair of integers where the first integer is either itemID or storeID,
//   * and the second integer is the number of items for that itemID at a given store or storeID for a given itemID
//   */
//  private PriorityQueue<Pair<Integer,Integer>> fetchTopElements(int heapSize, Integer[] rowOrCol) {
//    PriorityQueue<Pair<Integer,Integer>> topElemHeap = new PriorityQueue<>(heapSize, new PairComparator());
//    for (int i=0; i<heapSize; i++) { topElemHeap.add(new Pair<>(i+1, rowOrCol[i])); }
//    for (int i=heapSize; i<rowOrCol.length; i++) {
//      Pair<Integer,Integer> idAndNumItems = new Pair<>(i+1, rowOrCol[i]);
//      // if the current pair > heap minimum, kick out the head and add current pair to the heap
//      if (topElemHeap.peek().compareTo(idAndNumItems) < 1) {
//        topElemHeap.poll();
//        topElemHeap.add(idAndNumItems);
//      }
//    }
//    return topElemHeap;
//  }
//
//  /**
//   * Custom comparator class to compare (ID, numberOfItems) pair
//   * prioritizes smaller number of items.
//   * ID can either be itemID or storeID, and pair p1 > p2 if p1.numberOfItems > p1.numberOfItems
//   */
//  private class PairComparator implements Comparator<Pair<Integer,Integer>> {
//    @Override
//    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
//      if (o1.getValue1() < o2.getValue1()) return -1;
//      else if (o1.getValue1() > o2.getValue1()) return 1;
//      return 0;
//    }
//  }
//
//}

import java.util.Comparator;
import org.javatuples.Pair;

/**
 * Custom comparator class to compare (ID, numberOfItems) pair prioritizes smaller number of
 * items. ID can either be itemID or storeID, and pair p1 > p2 if p1.numberOfItems >
 * p1.numberOfItems
 */
public class PairComparator implements Comparator<Pair<Integer, Integer>> {

  @Override
  public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
    if (o1.getValue1() < o2.getValue1())
      return -1;
    else if (o1.getValue1() > o2.getValue1())
      return 1;
    return 0;
  }
}
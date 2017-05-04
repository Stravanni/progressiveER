///**
// * @author giovanni
// * <p>
// * Build the heap (MinMaxPriorityWueue of Guava) that allows to iterate throuhg
// * the pairs: (a) simple psn is derived from traditional psn (b) weighted psn
// * exploits exploit the weights computed on the blocking graph as surrogate of
// * similarity
// */
//package BlockBuilding.Progressive;
//
//import BlockBuilding.Progressive.DataStructures.PIWeightingScheme;
//import BlockBuilding.Progressive.DataStructures.PositionIndex.PositionIndex;
//import Comparators.ComparisonWeightComparator;
//import Comparators.ComparisonWindowComparator;
//import DataStructures.Comparison;
//import com.google.common.collect.MinMaxPriorityQueue;
//
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Iterator;
//
//public abstract class AbstractProgressiveSortedNeighbor_heap_backup implements Iterator {
//
//    protected boolean cleanCleanER;
//    protected boolean simpleSN;
//
//    protected int datasetLimit;
//    protected int window_cursor;
//    protected int max_window;
//
//    protected AbstractSortedNeighborhoodBlocking_builder snb;
//    protected Comparator comparator;
//    protected Integer[] sortedEntities;
//    protected MinMaxPriorityQueue<Comparison> comparison_heap;
//    protected final PIWeightingScheme weightingScheme;
//    protected PositionIndex pIndex;
//    protected HashSet<Comparison> comparisons;
//
//    public AbstractProgressiveSortedNeighbor_heap_backup(PIWeightingScheme wScheme) {
//        weightingScheme = wScheme;
//    }
//
//    public void buildEntityList(int max_w) {
//        snb.buildBlocks(); // does not actually returns the blocks as in sorted neighbor, it only compute the ordered list of entities
//        datasetLimit = snb.getDatasetLimit();
//        sortedEntities = snb.getSortedEntities();
//        cleanCleanER = snb.isClean();
//        simpleSN = true;
//        window_cursor = 0;
//        comparator = simpleSN ? new ComparisonWindowComparator() : new ComparisonWeightComparator();
//        max_window = max_w;
//        pIndex = new PositionIndex(snb.getSortedEntities().length, sortedEntities, snb.getProfileList(), weightingScheme);
//        comparisons = new HashSet<>();
//        buildFirst();
//    }
//
//    protected void buildFirst() {
//        comparison_heap = MinMaxPriorityQueue.orderedBy(comparator)
//                .maximumSize(sortedEntities.length - 1)
//                .create();
//        System.out.println("n: " + sortedEntities.length);
//        for (window_cursor = 0; window_cursor < sortedEntities.length - 1; window_cursor++) {
//            if (!cleanCleanER) {
//                int id1 = sortedEntities[window_cursor];
//                int id2 = sortedEntities[window_cursor + 1];
//
//                if (id1 == id2) {
//                    continue;
//                }
//
//                //Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2, 1) : new Comparison(cleanCleanER, id2, id1, 1);
//                //c.setUtilityMeasure(1);
//                Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2, 1) : new Comparison(cleanCleanER, id2, id1, 1);
//                //Comparison cc = (id1 > id2) ? new Comparison(cleanCleanER, id1, id2, 1) : new Comparison(cleanCleanER, id2, id1, 1);
//
//                if (!comparisons.contains(c) && pIndex.isRepeatedComparison(id1, id2, window_cursor, window_cursor + 1)) {
//                    //System.out.println("greater than min window but not in comparisons");
//                    System.out.println("in pindex, but not in comparisons");
//                }
//
//                if (comparisons.contains(c) && !pIndex.isRepeatedComparison(id1, id2, window_cursor, window_cursor + 1)) {
//                    System.out.println("in comparisons, but not in pindex");
//                }
//
//                if (comparisons.contains(c)) {
//                    continue;
//                }
//
//                comparisons.add(c);
//                //comparisons.add(cc);
//
//                double weight = simpleSN ? 1 : pIndex.getWeight(id1, id2, window_cursor, window_cursor + 1);
//                if (weight <= 0) {
//                    continue;
//                }
//                c.setUtilityMeasure(weight);
//                c.set_sn_positions(window_cursor, window_cursor + 1);
//
//                if (!comparison_heap.offer(c)) {
//                    System.out.println("comparison not added to the heap");
//                }
//            } else {
//                boolean found = false;
//                int w = 0; // the size of the window
//                int window_size = 0;
//                while (!found && ((window_cursor + (++w)) < sortedEntities.length) && window_size < max_window) {
//                    // todo check the increasing of w, since it grows faster than the real windows (use 2 different counters_neighbor_cooccurrence)
//                    int id1 = sortedEntities[window_cursor];
//                    int id2 = sortedEntities[window_cursor + w];
//                    if (id1 < datasetLimit ^ id2 < datasetLimit) {
//                        window_size++;
//                        //System.out.println("ok");
//                        Comparison c = new Comparison(cleanCleanER, id1, id2 - datasetLimit, window_size);
//                        double weight = simpleSN ? window_size : pIndex.getWeight(id1, id2, window_cursor, window_cursor + w);
//                        //double weight = i;
//                        if (!(weight > 0)) {
//                            //w++;
//                            continue;
//                        }
//                        //double weight = i;
//                        c.setUtilityMeasure(weight);
//                        c.set_sn_positions(window_cursor, window_cursor + window_size);
//                        found = true;
//                        if (!comparison_heap.offer(c)) {
//                            System.out.println("comparison not added to the heap");
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public MinMaxPriorityQueue get_heap() {
//        return comparison_heap;
//    }
//
//    @Override
//    public boolean hasNext() {
//        if (comparison_heap.isEmpty()) {
//            System.out.println("no more comparisons");
//        }
//        return !comparison_heap.isEmpty();
//    }
//
//    @Override
//    public Object next() {
//        //System.out.println("before: " + comparison_heap.size());
//        Comparison current = comparison_heap.poll();
//        //System.out.println("after: " + comparison_heap.size());
//        int[] positions = current.get_sn_positions();
//
//        boolean found = false;
//
//        int w = positions[1] - positions[0];
//        int window_size = current.getWindow();
//        //System.out.println("p + w: " + positions[0] + " + " + w + " < " + sortedEntities.length);
//
//        if (window_size > max_window) {
//            return current;
//        } else {
//            if (!cleanCleanER) {
//                while (!found && (positions[0] + (++w) < sortedEntities.length)) {
//                    int id1 = sortedEntities[positions[0]];
//                    int id2 = sortedEntities[positions[0] + w];
//
//                    if (id1 == id2) {
//                        continue;
//                    }
//
//                    //Comparison c = new Comparison(cleanCleanER, id1, id2, w);
//                    Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2, w) : new Comparison(cleanCleanER, id2, id1, w);
//                    //Comparison cc = (id1 > id2) ? new Comparison(cleanCleanER, id1, id2, w) : new Comparison(cleanCleanER, id2, id1, w);
//                    //c.setUtilityMeasure(w);
//
////                    if (comparisons.contains(c) && !pIndex.isRepeatedComparison(id1, id2, positions[0], positions[0] + w)) {
////                        //System.out.println("in pindex, but not in comparisons");
////                    }
////
////                    if (!comparisons.contains(c) && pIndex.isRepeatedComparison(id1, id2, positions[0], positions[0] + w)) {
//////                        int position2 = (id1 < id2) ? positions[0] + w : positions[0];
//////                        int position1 = (id1 > id2) ? positions[0] + w : positions[0];
//////                        System.out.println("c2: " + c.getEntityId2() + " - " + position2 + " c1: " + c.getEntityId1() + " - " + position1);
////                        System.out.println("in comparisons, but not in pindex\n");
////                    }
//                    //pIndex.isRepeatedComparison(id1, id2, positions[0], positions[0] + w, comparisons.contains(c), c, positions[0], positions[0] + w);
//
////                    if (!comparisons.contains(c) && pIndex.greaterThanMinWindow(id1, id2, positions[0], positions[0] + w)) {
////                        System.out.println("greater than min window but not in comparisons");
////                    }
//
//                    if (comparisons.contains(c)) {
//                        continue;
//                    }
//                    comparisons.add(c);
//                    //comparisons.add(cc);
//
////                    if (pIndex.isRepeatedComparison(id1, id2, positions[0], positions[0] + w)) {
////                        //System.out.println("rep");
////                        continue;
////                    }
//
//                    double weight = simpleSN ? w : pIndex.getWeight(id1, id2, positions[0], positions[0] + w);
//                    if (weight <= 0) {
//                        System.out.println("weight lower than zero");
//                        //break;
//                        continue;
//                    }
//                    found = true;
//                    //System.out.println("weight: " + weight);
//                    //System.out.println("window: " + w);
//                    c.setUtilityMeasure(weight);
//                    c.set_sn_positions(positions[0], positions[0] + w);
//                    if (!comparison_heap.offer(c)) {
//                        System.out.println("comparison not added to the heap");
//                    }
//                }
//                //System.out.println("s: " + comparison_heap.size());
//            } else {
//                while (!found && ((positions[0] + (++w)) < sortedEntities.length)) {
//                    if (window_size > max_window) {
//                        return current;
//                    }
//                    int id1 = sortedEntities[positions[0]];
//                    int id2 = sortedEntities[positions[0] + w];
//
//                    if (pIndex.isRepeatedComparison(id1, id2, positions[0], positions[0] + w)) {
//                        continue;
//                    }
//
//                    if (id1 < datasetLimit ^ id2 < datasetLimit) {
//                        window_size++;
//                        //System.out.println("ok");
//                        Comparison c = new Comparison(cleanCleanER, id1, id2 - datasetLimit, window_size);// todo controllare che ci sia da sottrarre datasetLimit
//
//                        //double weight = simpleSN ? ++window_cursor : getWeight(c) / w;
//                        ++window_cursor;
//                        double weight = simpleSN ? window_size : pIndex.getWeight(id1, id2, positions[0], positions[0] + w);
//                        //double weight = i;
//                        if (weight <= 0) {
//                            //w++;
//                            continue;
//                        }
//
//                        c.setUtilityMeasure(weight);
//                        c.set_sn_positions(positions[0], positions[0] + w);
//                        found = true;
//                        if (!comparison_heap.offer(c)) {
//                            System.out.println("comparison not added to the heap");
//                        }
//                    }
//                }
//            }
//            return current; // considera anche l'ultimo inserito
//        }
//    }
//}

/**
 * @author giovanni
 * <p>
 * Build the heap (MinMaxPriorityWueue of Guava) that allows to iterate throuhg
 * the pairs: (a) simple psn is derived from traditional psn (b) weighted psn
 * exploits exploit the weights computed on the blocking graph as surrogate of
 * similarity
 */
package BlockBuilding.Progressive;

import BlockBuilding.Progressive.DataStructures.PIWeightingScheme;
import BlockBuilding.Progressive.DataStructures.PositionIndex_;
import Comparators.ComparisonWeightComparator;
import Comparators.ComparisonWindowComparator;
import DataStructures.*;
import com.google.common.collect.MinMaxPriorityQueue;

import java.io.PrintWriter;
import java.util.*;

public abstract class AbstractProgressiveSortedNeighbor_heap implements Iterator {

    protected boolean cleanCleanER;
    protected boolean simpleSN;

    protected int datasetLimit;
    protected int window_cursor;
    protected int max_window;

    public AbstractSortedNeighborhoodBlocking_builder snb;
    protected Integer[] sortedEntities;

    protected Comparator comparator;
    protected MinMaxPriorityQueue<Comparison> comparison_heap;
    //protected PositionIndexExtended pIndex;
    protected PositionIndex_ pIndex;

    protected Set<Comparison> comparisons;

    protected PIWeightingScheme weightingScheme;
    protected boolean usePI;

    protected PrintWriter writer;

    protected String name;

    public AbstractProgressiveSortedNeighbor_heap(boolean removeRepetedComparisons) {
        simpleSN = true;
        usePI = removeRepetedComparisons;
    }

    public AbstractProgressiveSortedNeighbor_heap(PIWeightingScheme wScheme, boolean removeRepetedComparisons) {
        weightingScheme = wScheme;
        simpleSN = false;
        usePI = removeRepetedComparisons;
    }

    public void buildEntityList(int max_w) {
        snb.buildBlocks(); // does not actually returns the blocks as in sorted neighbor, it only compute the ordered list of entities
        datasetLimit = snb.getDatasetLimit();
        sortedEntities = snb.getSortedEntities();
        cleanCleanER = snb.isClean();

        window_cursor = 0;
        comparator = (simpleSN) ? new ComparisonWindowComparator() : new ComparisonWeightComparator();
        max_window = max_w;
        //pIndex = new PositionIndex(snb.getSortedEntities().length, sortedEntities, snb.getProfileList(), weightingScheme);

        //pIndex = new PositionIndexExtended(snb.getSortedEntities().length, snb.getProfileList(), weightingScheme);
        //pIndex = new PositionIndexExtended(snb.getSortedEntities().length, snb, weightingScheme);
        pIndex = new PositionIndex_(snb, weightingScheme);

        comparisons = new HashSet<>(50 * sortedEntities.length);


        buildFirst();
    }

    protected void buildFirst() {
        comparison_heap = MinMaxPriorityQueue.orderedBy(comparator)
                .maximumSize(sortedEntities.length - 1)
                .create();
        System.out.println("n: " + sortedEntities.length);


        for (window_cursor = 0; window_cursor < sortedEntities.length - 1; window_cursor++) {
            if (!cleanCleanER) {
                int id1 = sortedEntities[window_cursor];
                int id2 = sortedEntities[window_cursor + 1];

                if (id1 == id2) {
                    continue;
                }

                if (usePI && pIndex.isRepeatedComparison(id1, id2)) {
//                    if (id1 < id2) {
//                        System.out.println("rep " + id1 + " - " + id2);
//                    } else {
//                        System.out.println("rep " + id2 + " - " + id1);
//                    }
                    continue;
                }
//                else {
//                    if (id1 < id2) {
//                        System.out.println(id1 + " - " + id2 + " #");
//                    } else {
//                        System.out.println(id2 + " - " + id1 + " #");
//                    }
//                }

                Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2, 1) : new Comparison(cleanCleanER, id2, id1, 1);

                double weight = simpleSN ? window_cursor : pIndex.getWeight(id1, id2, window_cursor, window_cursor + 1);

                if (weight <= 0) {
                    continue;
                }

                c.setUtilityMeasure(weight);
                c.set_sn_positions(window_cursor, window_cursor + 1);

                if (!comparison_heap.offer(c)) {
                    System.out.println("comparison not added to the heap");
                } else {
                    pIndex.addPosition(id1, window_cursor);
                    pIndex.addPosition(id2, window_cursor + 1);
                    pIndex.setWindowSizeMap(window_cursor, 1);
                }
            } else {

                boolean found = false;
                int win_local = 0; // the size of the window
                int window_size = 0;
                while (!found && ((window_cursor + (++win_local)) < sortedEntities.length) && window_size < max_window) {
                    int id1 = sortedEntities[window_cursor];
                    int id2 = sortedEntities[window_cursor + win_local];

                    // XOR
                    if (id1 < datasetLimit ^ id2 < datasetLimit) {
                        window_size++;

                        //Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2 - datasetLimit, window_size) : new Comparison(cleanCleanER, id1 - datasetLimit, id2, window_size);
                        Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2 - datasetLimit, window_size) : new Comparison(cleanCleanER, id2, id1 - datasetLimit, window_size);

                        if (usePI && pIndex.isRepeatedComparison(id1, id2)) {
                            continue;
                        }

                        double weight = 0;

                        //if (snb.getIndexKeyInitial(window_cursor) == snb.getIndexKeyInitial(window_cursor + win_local)) {
                        if (id1 < datasetLimit) {
                            weight = simpleSN ? window_cursor : pIndex.getWeight(id1, id2, window_cursor, window_cursor + win_local);
                        } else {
                            weight = simpleSN ? window_cursor : pIndex.getWeight(id2, id1, window_cursor, window_cursor + win_local);
                        }
                        //}

                        if (weight <= 0) {
                            continue;
                        }


                        c.setUtilityMeasure(weight);
                        //c.set_sn_positions(window_cursor, window_cursor + window_size);
                        c.set_sn_positions(window_cursor, window_cursor + win_local);
                        found = true;


                        if (!comparison_heap.offer(c)) {
                            System.out.println("comparison not added to the heap");
                        } else {
                            pIndex.addPosition(id1, window_cursor);
                            pIndex.addPosition(id2, window_cursor + win_local);
                            pIndex.setWindowSizeMap(window_cursor, win_local);
                        }
                    }
//                    pIndex.addPosition(id1, window_cursor);
//                    pIndex.addPosition(id2, window_cursor + win_local);
//                    pIndex.setWindowSizeMap(window_cursor, win_local);
                }
            }
        }
        //writer.close();
    }

    public MinMaxPriorityQueue get_heap() {
        return comparison_heap;
    }

    @Override
    public boolean hasNext() {
        if (comparison_heap.isEmpty()) {
            System.out.println("no more comparisons");
        }
        return !comparison_heap.isEmpty();
    }

    @Override
    public Object next() {
        Comparison current = comparison_heap.poll();
        int[] positions = current.get_sn_positions();

        boolean found = false;

        int win_local = positions[1] - positions[0];
        int window_size = current.getWindow();

        if (window_size > max_window) {
            return current;
        } else {
            while (!found && (positions[0] + (++win_local) < sortedEntities.length) && window_size < max_window) {
                if (!cleanCleanER) {
                    if (win_local > max_window) {
                        return current;
                    }
                    int id1 = sortedEntities[positions[0]];
                    int id2 = sortedEntities[positions[0] + win_local];

                    if (id1 == id2) {
                        continue;
                    }

                    if (usePI && pIndex.isRepeatedComparison(id1, id2)) {
                        continue;
                    }

                    Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2, win_local) : new Comparison(cleanCleanER, id2, id1, win_local);


                    double weight = simpleSN ? ++window_cursor : pIndex.getWeight(id1, id2, positions[0], positions[0] + win_local);
                    if (weight <= 0) {
                        continue;
                    }
                    found = true;

                    c.setUtilityMeasure(weight);
                    c.set_sn_positions(positions[0], positions[0] + win_local);

                    if (!comparison_heap.offer(c)) {
                        System.out.println("comparison not added to the heap");
                    } else {
                        pIndex.addPosition(id2, positions[0] + win_local);
                        pIndex.setWindowSizeMap(positions[0], win_local);
                    }
                } else {

                    if (win_local > max_window) {
                        return current;
                    }

                    int id1 = sortedEntities[positions[0]];
                    int id2 = sortedEntities[positions[0] + win_local];

                    if (id1 < datasetLimit ^ id2 < datasetLimit) {
                        window_size++;

                        if (usePI && pIndex.isRepeatedComparison(id1, id2)) {
                            continue;
                        }

                        //Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2 - datasetLimit, window_size) : new Comparison(cleanCleanER, id1 - datasetLimit, id2, window_size);
                        Comparison c = (id1 < id2) ? new Comparison(cleanCleanER, id1, id2 - datasetLimit, window_size) : new Comparison(cleanCleanER, id2, id1 - datasetLimit, window_size);

                        double weight = 0;

                        //if (snb.getIndexKeyInitial(positions[0]) == snb.getIndexKeyInitial(positions[0] + win_local)) {
                        if (id1 < datasetLimit) {
                            weight = simpleSN ? ++window_cursor : pIndex.getWeight(id1, (id2), positions[0], positions[0] + win_local);
                        } else {
                            weight = simpleSN ? ++window_cursor : pIndex.getWeight(id2, (id1), positions[0], positions[0] + win_local);
                        }
                        //}

                        //double weight = i
                        if (weight <= 0) {
                            //w++;
                            continue;
                        }

                        c.setUtilityMeasure(weight);
                        c.set_sn_positions(positions[0], positions[0] + win_local);
                        found = true;
                        if (!comparison_heap.offer(c)) {
                            System.out.println("comparison not added to the heap");
                        } else {
                            //pIndex.addPosition(id1, window_cursor);
                            pIndex.addPosition(id2, positions[0] + win_local);
                            pIndex.setWindowSizeMap(positions[0], win_local);
                        }
                    }
//                    pIndex.addPosition(id2, positions[0] + win_local);
//                    pIndex.setWindowSizeMap(positions[0], win_local);
                }
            }
            return current; // considera anche l'ultimo inserito
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
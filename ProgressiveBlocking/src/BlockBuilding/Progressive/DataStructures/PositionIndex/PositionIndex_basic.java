package BlockBuilding.Progressive.DataStructures.PositionIndex;

import BlockBuilding.Progressive.DataStructures.PairsInfo;
import BlockBuilding.Progressive.DataStructures.WeightingSchemeSn;
import BlockBuilding.Progressive.SortedNeighborhood.ProgressiveSnBuilder;
import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;
import Utilities.ProfileComparison;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Ordering;

import java.io.Serializable;
import java.util.*;

/**
 * @author giovanni
 * @author gap2
 */
public class PositionIndex_basic implements Serializable {

    private static final long serialVersionUID = 13483254243447435L;

    private final int noOfEntities, datasetLimits;
    private final int[] windowIndex, profileCount;
    private final int[][] entityPositions;

    public int c;

    private final Queue<PairsInfo> pairsQueue;
    private final WeightingSchemeSn weightingScheme;
    private final List<EntityProfile>[] profiles;
    private MinHashIndex mhi;

    public PositionIndex_basic(ProgressiveSnBuilder snb, WeightingSchemeSn wScheme) {
        this(snb.getNumEntities(), snb.getSortedEntities(), snb.getProfileList(), wScheme);
    }

    public PositionIndex_basic(int no_of_entities, int[] sortedEntities, List<EntityProfile>[] profiles, WeightingSchemeSn wScheme) {
        noOfEntities = no_of_entities;

        weightingScheme = wScheme;

        profileCount = new int[noOfEntities];

        //pairsQueue = new PriorityQueue<>();

        //Comparator<PairsInfo> compMinMax = Ordering.natural().reverse();
        Comparator<PairsInfo> compMinMax = Ordering.natural();
        int sizePairsQueue = noOfEntities;
        pairsQueue = MinMaxPriorityQueue.orderedBy(compMinMax)
                .maximumSize(sizePairsQueue)
                .create();

        this.profiles = profiles;
        datasetLimits = profiles[0].size();

        int[] counters = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            counters[i] = 0;
        }
        for (int entityId : sortedEntities) {
            counters[entityId]++;
        }

        //initialize inverted index
        entityPositions = new int[noOfEntities][];
        windowIndex = new int[sortedEntities.length];

        for (int i = 0; i < noOfEntities; i++) {
            entityPositions[i] = new int[counters[i]];
            counters[i] = 0;
        }

        //build inverted index
        for (int position = 0; position < sortedEntities.length; position++) {
            int entityId = sortedEntities[position];
            entityPositions[entityId][counters[entityId]++] = position;
            windowIndex[position] = 0;
        }

        if (weightingScheme == WeightingSchemeSn.MINHASH) {
            mhi = new MinHashIndex(profiles, 60);
            mhi.buildIndex();
        } else {
            mhi = null;
        }
    }

    public void setWindowSizeMap(int position, int w) {
        windowIndex[position] = w;
    }

    public double getWeight(int entity1, int entity2, int position1, int position2) {
        switch (weightingScheme) {
//            case AGGREGATE_INVERSE_DISTANCE:
//                return getAggregateInverseDinstance(entity1, entity2, position1, position2);
//            case EXHAUSTIVE_AGGREGATE_INVERSE_DISTANCE:
//                return getExhaustiveAggregateInverseDistance(entity1, entity2, position1, position2);
//            case EXHAUSTIVE_NORMALIZED_WINDOW_OVERLAP:
//                return getExhaustiveNormalizedWindowOverlap(entity1, entity2, position1, position2);
//            case EXHAUSTIVE_WINDOW_OVERLAP:
//                return getExhaustiveWindowOverlap(entity1, entity2, position1, position2);
//            case NORMALIZED_WINDOW_OVERLAP:
//                return getNormalizedWindowOverlap(entity1, entity2, position1, position2);
//            case WINDOW_OVERLAP:
//                return getWindowOverlap(entity1, entity2, position1, position2);
            /*case JS:
                return ProfileComparison.getJaccardSimilarity(profiles[(entity1 < datasetLimits) ? 0 : 1].get(entity1).getAttributes(), profiles[(entity2 < datasetLimits) ? 0 : 1].get(entity2).getAttributes());*/
            case MINHASH:
                return mhi.getApproximateSimilarity(entity1, entity2);
            /*case TEST:
                double w = 1. / (++profileCount[entity1] + ++profileCount[entity2]);
                //double w = 1./Math.max(++profileCount[entity1], ++profileCount[entity2]);
                //System.out.println(profileCount[entity1] + profileCount[entity1] + " - " + 1. / (profileCount[entity1] + profileCount[entity1]) + " -  " + w);
                return w;*/
        }
        return -1;
    }

    /**
     * Checks if the comparison is repeated or not
     *
     * @param entity1
     * @param entity2
     * @return
     */
    public boolean isRepeatedComparison(int entity1, int entity2) {
        for (int p1 : entityPositions[entity1]) {
            for (int p2 : entityPositions[entity2]) {
                c++;
                // p1 is the lower
                if (p1 < p2) {
                    int current_window_size = p2 - p1;
                    int min_window_btw_profiles = windowIndex[p1];
                    if (current_window_size <= min_window_btw_profiles) {
                        return true;
                    }
                }

                // p2 is the lower
                if (p2 < p1) {
                    int current_window_size = p1 - p2;
                    int min_window_btw_profiles = windowIndex[p2];
                    if (current_window_size <= min_window_btw_profiles) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double getAggregateInverseDinstance(int entity1, int entity2, int position1, int position2) {
        pairsQueue.clear();
        for (int pi1 : entityPositions[entity1]) {
            for (int pi2 : entityPositions[entity2]) {
                pairsQueue.add(new PairsInfo(pi1, pi2));
            }
        }

        PairsInfo head = pairsQueue.poll();
        double inputWindow = Math.abs(position1 - position2);
        double minWindow = Math.abs(head.getPosition1() - head.getPosition2());
        if (minWindow < inputWindow) {
            return -1;
        }

        double totalWeight = 1.0 / minWindow;
        Set<Integer> excludedPositions = new HashSet<Integer>();
        excludedPositions.add(head.getPosition1());
        excludedPositions.add(head.getPosition2());

        while (!pairsQueue.isEmpty()) {
            PairsInfo currentPair = pairsQueue.poll();
            if (excludedPositions.contains(currentPair.getPosition1())
                    || excludedPositions.contains(currentPair.getPosition2())) {
                continue;
            }

            double currentWindow = Math.abs(currentPair.getPosition1() - currentPair.getPosition2());
            totalWeight += 1.0 / currentWindow;
            excludedPositions.add(currentPair.getPosition1());
            excludedPositions.add(currentPair.getPosition2());
        }
        return totalWeight;
    }

    private double getExhaustiveAggregateInverseDistance(int entity1, int entity2, int position1, int position2) {
        pairsQueue.clear();
        for (int pi1 : entityPositions[entity1]) {
            for (int pi2 : entityPositions[entity2]) {
                pairsQueue.add(new PairsInfo(pi1, pi2));
            }
        }

        PairsInfo head = pairsQueue.poll();
        double inputWindow = Math.abs(position1 - position2);
        //double minWindow = Math.abs(head.getPosition1().getPositionId() - head.getPosition2().getPositionId());
        double minWindow = Math.abs(head.getPosition1() - head.getPosition2());
        if (minWindow < inputWindow) {
            return -1;
        }

        double totalWeight = 1.0 / minWindow;
        while (!pairsQueue.isEmpty()) {
            PairsInfo currentPair = pairsQueue.poll();
            double currentWindow = Math.abs(currentPair.getPosition1() - currentPair.getPosition2());
            totalWeight += 1.0 / currentWindow;
        }
        return totalWeight;
    }

    private double getExhaustiveNormalizedWindowOverlap(int entity1, int entity2, int position1, int position2) {
        double overlapWeight = getExhaustiveWindowOverlap(entity1, entity2, position1, position2);
        return overlapWeight / (entityPositions[entity1].length + entityPositions[entity2].length);
    }

    private double getExhaustiveWindowOverlap(int entity1, int entity2, int position1, int position2) {
        pairsQueue.clear();
        for (int pi1 : entityPositions[entity1]) {
            for (int pi2 : entityPositions[entity2]) {
                pairsQueue.add(new PairsInfo(pi1, pi2));
            }
        }

        PairsInfo head = pairsQueue.poll();
        double inputWindow = Math.abs(position1 - position2);
        double minWindow = Math.abs(head.getPosition1() - head.getPosition2());
        if (minWindow < inputWindow) {
            return -1;
        }

        double totalWeight = 1;
        while (!pairsQueue.isEmpty()) {
            PairsInfo currentPair = pairsQueue.poll();
            double currentWindow = Math.abs(currentPair.getPosition1() - currentPair.getPosition2());
            if (inputWindow < currentWindow) {
                return totalWeight;
            }
            totalWeight++;
        }
        return totalWeight;
    }

    private double getNormalizedWindowOverlap(int entity1, int entity2, int position1, int position2) {
        double overlapWeight = getWindowOverlap(entity1, entity2, position1, position2);
        return overlapWeight / (entityPositions[entity1].length + entityPositions[entity2].length);
    }

    private double getWindowOverlap(int entity1, int entity2, int position1, int position2) {
        pairsQueue.clear();
        for (int pi1 : entityPositions[entity1]) {
            for (int pi2 : entityPositions[entity2]) {
                pairsQueue.add(new PairsInfo(pi1, pi2));
            }
        }

        PairsInfo head = pairsQueue.poll();
        double inputWindow = Math.abs(position1 - position2);
        double minWindow = Math.abs(head.getPosition1() - head.getPosition2());
        if (minWindow < inputWindow) {
            return -1;
        }

        double totalWeight = 1;
        Set<Integer> excludedPositions = new HashSet<Integer>();
        excludedPositions.add(head.getPosition1());
        excludedPositions.add(head.getPosition2());

        while (!pairsQueue.isEmpty()) {
            PairsInfo currentPair = pairsQueue.poll();
            if (excludedPositions.contains(currentPair.getPosition1())
                    || excludedPositions.contains(currentPair.getPosition2())) {
                continue;
            }

            double currentWindow = Math.abs(currentPair.getPosition1() - currentPair.getPosition2());
            if (inputWindow < currentWindow) {
                return totalWeight;
            }
            totalWeight++;
            excludedPositions.add(currentPair.getPosition1());
            excludedPositions.add(currentPair.getPosition2());
        }
        return totalWeight;
    }

    /**
     * An empty method. Just to be compatible with PositionIndexExtended.
     *
     * @param a
     * @param b
     */
    public void addPosition(int a, int b) {
        //
    }
}
package BlockBuilding.Progressive.SortedNeighborhood.Local;

import BlockBuilding.Progressive.DataStructures.WeightingSchemeSnLocal;
import Comparators.ComparisonUtilityComparator;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;

import java.util.*;

/**
 * @author gap2
 * @author giovanni
 */

public class LocalWeightedProgressiveSn extends NaiveProgressiveSn implements Iterator<Comparison> {

    protected final List<Comparison> windowComparisons;
    protected final WeightingSchemeSnLocal weightingSchema;

    protected int[] counters_neighbor_cooccurrence; // for each entity counts the co-occurrence of a neighbor
    protected int[] flags_neighbor; // avoid to initialize all the counters for each entity

    protected Set<Integer> distinctNeighbors;

    protected MinHashIndex mhi; // only if ws = MINHASH

    protected boolean naiveSn; // w/o weighting function

    /**
     * LocalCpProgressiveSn
     *
     * @param profiles
     */
    public LocalWeightedProgressiveSn(List<EntityProfile>[] profiles) {
        this(profiles, null, false);
    }

    public LocalWeightedProgressiveSn(List<EntityProfile>[] profiles, WeightingSchemeSnLocal ws, boolean removeRep) {
        this(profiles, ws, removeRep, 100);
    }

    /**
     * LocalWeightedProgressiveSn
     *
     * @param profiles
     * @param ws
     * @param removeRep
     */
    public LocalWeightedProgressiveSn(List<EntityProfile>[] profiles, WeightingSchemeSnLocal ws, boolean removeRep, int max_win) {
        super(profiles, removeRep, true, max_win);

        naiveSn = true;
        weightingSchema = ws;
        distinctNeighbors = new HashSet<>();

        if (weightingSchema != null) {
            System.out.println("weighting schema sn: " + weightingSchema);
            counters_neighbor_cooccurrence = new int[noOfEntities];
            flags_neighbor = new int[noOfEntities];
            for (int i = 0; i < noOfEntities; i++) {
                flags_neighbor[i] = -1;
            }
            if (weightingSchema == WeightingSchemeSnLocal.MINHASH) {
                mhi = new MinHashIndex(profiles, 60);
                mhi.buildIndex();
            }
            naiveSn = false;
        }
        currentWindow = 0;
        windowComparisons = new ArrayList<>();
    }

    protected void getWindowComparisons() {
        for (int entityId = 0; entityId < datasetLimit; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            int neighborPosition;
            int neighborId;

            for (int position : entityPositions) {

                // next neighbor at distance "currentWindow"
                neighborPosition = position + currentWindow;
                if (neighborPosition < sortedEntities.length &&
                        (cleanCleanER ?
                                datasetLimit <= sortedEntities[neighborPosition] : // clan-clean
                                sortedEntities[neighborPosition] < entityId)       // dirty
                        ) {
                    neighborId = sortedEntities[neighborPosition];
                    distinctNeighbors.add(neighborId);

                    if (!naiveSn)
                        updateCountersNeighbor(neighborId, entityId);
                }

                // previous neighbor at distance "currentWindow"
                neighborPosition = position - currentWindow;
                if (0 <= neighborPosition &&
                        (cleanCleanER ?
                                datasetLimit <= sortedEntities[neighborPosition] :
                                sortedEntities[neighborPosition] < entityId)
                        ) {
                    neighborId = sortedEntities[neighborPosition];
                    distinctNeighbors.add(neighborId);

                    if (!naiveSn)
                        updateCountersNeighbor(neighborId, entityId);
                }
            }

            for (Integer neighbor_id : distinctNeighbors) {
                Comparison c = new Comparison(cleanCleanER, entityId, (cleanCleanER ? (neighbor_id - datasetLimit) : neighbor_id));
                if (removeRepeatedComparisons) {
                    /*
                    * These positions just are not actually used:
                    * the comparison within a window-frame are already removed
                    * -> in "isRedundant()" method, this position is meaningless (differently from the naive solution)
                    * */
                    c.set_sn_positions(sPositionIndex.getEntityPositions(entityId)[0], sPositionIndex.getEntityPositions(neighbor_id)[0]);
                }
                if (!naiveSn) {
                    c.setUtilityMeasure(getWeight(entityId, neighbor_id, counters_neighbor_cooccurrence[neighbor_id]));
                }
                windowComparisons.add(c);
            }
        }
        if (!naiveSn) {
            Collections.sort(windowComparisons, new ComparisonUtilityComparator());
        }
    }

    protected double getWeight(int entityId1, int entityId2, int coOccurrenceFreq) {
        switch (weightingSchema) {
            case ACF:
                return coOccurrenceFreq;
            case NCF:
                double denominator = sPositionIndex.getEntityPositions(entityId1).length + sPositionIndex.getEntityPositions(entityId2).length - coOccurrenceFreq;
                return coOccurrenceFreq / denominator;
            case MINHASH:
                return mhi.getApproximateSimilarity(entityId1, entityId2);
        }
        return -1;
    }

    protected void updateCountersNeighbor(int neighborId, int entityId) {
        if (flags_neighbor[neighborId] != entityId) {
            counters_neighbor_cooccurrence[neighborId] = 0;
            flags_neighbor[neighborId] = entityId;
        }
        counters_neighbor_cooccurrence[neighborId]++;
    }

    @Override
    public Comparison nextComparison() {
        if (windowComparisons.isEmpty()) {
            currentWindow++;
            if (hasNext()) {
                getWindowComparisons();
            } else {
                return null;
            }
        }

        Comparison next_comparison = windowComparisons.remove(0);
        // Used in isRedundant()
        repId1 = next_comparison.get_sn_positions()[0];
        repId2 = next_comparison.get_sn_positions()[1];
        currentPosition = Math.min(repId1, repId2);

        return next_comparison;
    }
}
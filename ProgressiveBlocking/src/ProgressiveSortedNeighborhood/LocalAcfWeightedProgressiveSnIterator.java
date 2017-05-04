package ProgressiveSortedNeighborhood;

import BlockBuilding.Progressive.DataStructures.WeightingSchemeSnLocal;
import Comparators.ComparisonUtilityComparator;
import DataStructures.Comparison;
import DataStructures.EntityProfile;

import java.util.*;

/**
 *
 * @author gap2
 */

public class LocalAcfWeightedProgressiveSnIterator extends LocalCpProgressiveSnIterator implements Iterator<Comparison> {

    /* Acf stands for Absolute Co-occurrence Frequency */
    
    protected final int[] counters;
    protected final int[] flags;

    protected final Set<Integer> distinctNeighbors;

    public LocalAcfWeightedProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);

        distinctNeighbors = new HashSet<>();
        counters = new int[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }
    }

    public LocalAcfWeightedProgressiveSnIterator(List<EntityProfile>[] profiles, WeightingSchemeSnLocal ws, boolean removeRep, int max_win) {
        super(profiles);

        distinctNeighbors = new HashSet<>();
        counters = new int[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }
    }

    @Override
    protected void getCleanCleanWindowComparisons() {
        for (int entityId = 0; entityId < datasetLimit; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (int position : entityPositions) {
                if (position + currentWindow < sortedEntities.length
                        && datasetLimit <= sortedEntities[position + currentWindow]) {
                    int neighborId = sortedEntities[position + currentWindow];
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    distinctNeighbors.add(neighborId);
                }

                if (0 <= position - currentWindow
                        && datasetLimit <= sortedEntities[position - currentWindow]) {
                    int neighborId = sortedEntities[position - currentWindow];
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    distinctNeighbors.add(neighborId);
                }
            }

            for (Integer neighborId : distinctNeighbors) {
                Comparison c = new Comparison(cleanCleanER, entityId, neighborId - datasetLimit);
                c.setUtilityMeasure(getWeight(entityId, neighborId, counters[neighborId]));
                windowComparisons.add(c);
            }
        }

        Collections.sort(windowComparisons, new ComparisonUtilityComparator());
    }

    @Override
    protected void getDirtyWindowComparisons() {
        for (int entityId = 0; entityId < noOfEntities; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (int position : entityPositions) {
                if (position + currentWindow < sortedEntities.length
                        && sortedEntities[position + currentWindow] < entityId) {
                    int neighborId = sortedEntities[position + currentWindow];
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    distinctNeighbors.add(neighborId);
                }

                if (0 <= position - currentWindow
                        && sortedEntities[position - currentWindow] < entityId) {
                    int neighborId = sortedEntities[position - currentWindow];
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    distinctNeighbors.add(neighborId);
                }
            }

            for (Integer neighborId : distinctNeighbors) {
                Comparison c = new Comparison(cleanCleanER, neighborId, entityId);
                c.setUtilityMeasure(getWeight(entityId, neighborId, counters[neighborId]));
                windowComparisons.add(c);
            }
        }

        Collections.sort(windowComparisons, new ComparisonUtilityComparator());
    }

    protected double getWeight(int entityId1, int entityId2, int coOccurrenceFreq) {
        return coOccurrenceFreq;
    }
}

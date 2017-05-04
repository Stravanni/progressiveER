package ProgressiveSortedNeighborhood;

import BlockBuilding.Progressive.ProgressiveMetaBlocking.MetablockingComparator;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import ProgressiveSortedNeighborhood.DataStructures.SimplePositionIndex;

import java.util.*;

/**
 * @author gap2
 */
public class GlobalAcfWeightedProgressiveSnIterator extends NaiveProgressiveSnIterator implements Iterator<Comparison> {

    /* Acf stands for Absolute Co-occurrence Frequency */
    protected double minimumWeight;

    protected int counter;
    protected final int maxCPE; // max comparisons per entity

    protected final int[] counters;
    protected final int[] flags;

    protected Comparison[] sortedTopComparisons;
    protected final Queue<Comparison> topKEdges;
    protected final Set<Integer> distinctNeighbors;
    protected final SimplePositionIndex sPositionIndex;

    public GlobalAcfWeightedProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);

        counter = 0;
        maxCPE = 20;

        counters = new int[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        System.out.println("Window\t:\t" + maxWindow);

        distinctNeighbors = new HashSet<>();
        topKEdges = new PriorityQueue<>(2 * maxCPE, new MetablockingComparator());
        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);

        if (cleanCleanER) {
            getCleanCleanComparisons();
        } else {
            getDirtyComparisons();
        }
    }

    private void getCleanCleanComparisons() {
        final Set<Comparison> topComparisons = new HashSet<>();

        for (int entityId = 0; entityId < datasetLimit; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (currentWindow = 1; currentWindow < maxWindow; currentWindow++) {
                for (int position : entityPositions) {
                    if (position + currentWindow < sortedEntities.length
                            && datasetLimit <= sortedEntities[position + currentWindow]) {
                        int neighborId = sortedEntities[position + currentWindow];
                        if (flags[neighborId] != entityId) {
                            counters[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId, currentWindow);
                    }

                    if (0 <= position - currentWindow
                            && datasetLimit <= sortedEntities[position - currentWindow]) {
                        int neighborId = sortedEntities[position - currentWindow];
                        if (flags[neighborId] != entityId) {
                            counters[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId, currentWindow);
                    }

                }
            }

            topKEdges.clear();
            minimumWeight = -1;
            for (Integer neighborId : distinctNeighbors) {
                double weight = getWeight(entityId, neighborId);
                if (weight < minimumWeight) {
                    continue;
                }

                Comparison c = new Comparison(cleanCleanER, entityId, neighborId - datasetLimit);
                c.setUtilityMeasure(weight);
                topKEdges.add(c);
                if (maxCPE < topKEdges.size()) {
                    Comparison lastComparison = topKEdges.poll();
                    minimumWeight = lastComparison.getUtilityMeasure();
                }
            }
            topComparisons.addAll(topKEdges);
        }
        sortedTopComparisons = topComparisons.toArray(new Comparison[topComparisons.size()]);
        System.out.println("Total comparisons\t:\t" + sortedTopComparisons.length);
    }

    private void getDirtyComparisons() {
        final Set<Comparison> topComparisons = new HashSet<>();

        for (int entityId = 0; entityId < noOfEntities; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (currentWindow = 1; currentWindow < maxWindow; currentWindow++) {
                for (int position : entityPositions) {
                    if (position + currentWindow < sortedEntities.length
                            && sortedEntities[position + currentWindow] < entityId) {
                        int neighborId = sortedEntities[position + currentWindow];
                        if (flags[neighborId] != entityId) {
                            counters[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId, currentWindow);
                    }

                    if (0 <= position - currentWindow
                            && sortedEntities[position - currentWindow] < entityId) {
                        int neighborId = sortedEntities[position - currentWindow];
                        if (flags[neighborId] != entityId) {
                            counters[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId, currentWindow);
                    }
                }
            }

            topKEdges.clear();
            minimumWeight = -1;
            for (Integer neighborId : distinctNeighbors) {
                double weight = getWeight(entityId, neighborId);
                if (weight < minimumWeight) {
                    continue;
                }

                Comparison c = new Comparison(cleanCleanER, neighborId, entityId);
                c.setUtilityMeasure(weight);
                topKEdges.add(c);
                if (maxCPE < topKEdges.size()) {
                    Comparison lastComparison = topKEdges.poll();
                    minimumWeight = lastComparison.getUtilityMeasure();
                }
            }
            topComparisons.addAll(topKEdges);
        }
        sortedTopComparisons = topComparisons.toArray(new Comparison[topComparisons.size()]);
        System.out.println("Total comparisons\t:\t" + sortedTopComparisons.length);
    }

    protected double getWeight(int entityId, int neighborId) {
        return counters[neighborId];
    }

    @Override
    public boolean hasNext() {
        return counter < sortedTopComparisons.length;
    }

    @Override
    public Comparison next() {
        if (hasNext()) {
            return sortedTopComparisons[counter++];
        }
        return null;
    }

    protected void updateLocalWeight(int neighborId, int window) {
        counters[neighborId]++;
        distinctNeighbors.add(neighborId);
    }
}

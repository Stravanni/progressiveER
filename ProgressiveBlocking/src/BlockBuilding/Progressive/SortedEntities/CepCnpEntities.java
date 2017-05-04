package BlockBuilding.Progressive.SortedEntities;

import BlockBuilding.Progressive.DataStructures.EntityComparable;
import BlockBuilding.Progressive.DataStructures.InverseMetablockingComparator;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.AbstractProgressiveMetaBlocking;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepCnp;
import BlockBuilding.Progressive.DataStructures.MetablockingComparator;
import DataStructures.Comparison;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.*;

/**
 * @author giovanni
 *         The method orders the entities in sortedEntityHeap,
 *         meanwhile the entityComparison collects the top-1 comparison for each entity;
 */
public class CepCnpEntities extends CepCnp implements Iterator<Comparison>, AbstractProgressiveMetaBlocking {

    protected PriorityQueue<EntityComparable> sortedEntityHeap;
    protected PriorityQueue<Comparison> entityComparison;
    protected MinMaxPriorityQueue<Comparison> topKperEntity;
    protected boolean[] entityCompared;
    /*private double[] entityThreshold;*/
    /*private double wThreshold;*/
    protected double entityWeights;

    public CepCnpEntities(WeightingScheme scheme, int num_profiles) {
        super(scheme, 1);
        if (fixedThreshold != 1) {
            System.out.println("error in fixing the threshold");
        }
        sortedEntityHeap = new PriorityQueue<>(num_profiles, Comparator.reverseOrder());
        entityComparison = new PriorityQueue<>(num_profiles, new MetablockingComparator());
        entityCompared = new boolean[num_profiles];
    }

    @Override
    public boolean hasNext() {
        boolean top_comparisons = counter < sortedTopComparisons.length;
        boolean comparison_not_empty = !entityComparison.isEmpty();

        if (top_comparisons || comparison_not_empty) {
            return true;
        }

        int current_id;
        while (!sortedEntityHeap.isEmpty()) {
            current_id = sortedEntityHeap.poll().getId();
            entityCompared[current_id] = true;
            getComparisonEntity(current_id);
            if (!entityComparison.isEmpty()) {
                /*if (counter == sortedTopComparisons.length) {
                    entityComparison.poll(); // the first has already been compared
                }*/
                return true;
            }
        }

        return !entityComparison.isEmpty();
    }

    @Override
    public Comparison next() {
        if (counter < sortedTopComparisons.length) {
            return sortedTopComparisons[counter++];
        } else {
            return entityComparison.poll();
        }
    }


    /*This processEntity avoid to check entityCompared[neighborId] == true*/
    @Override
    protected void processEntity(int entityId) {
        int kk = (int) (10 * Math.max(1, blockAssingments / noOfEntities));
        /*kk = noOfEntities;*/
        topKperEntity = MinMaxPriorityQueue.orderedBy(new InverseMetablockingComparator()).maximumSize(kk).create();

        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (!entityCompared[neighborId]) {
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    validEntities.add(neighborId);
                }
            }
        }
    }

    /*This processArcsEntity avoid to check entityCompared[neighbor] == true*/
    @Override
    protected void processArcsEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            double blockComparisons = cleanCleanER ? bBlocks[blockIndex].getNoOfComparisons() : uBlocks[blockIndex].getNoOfComparisons();
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (!entityCompared[neighborId]) {
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId] += 1 / blockComparisons;
                    validEntities.add(neighborId);
                }
            }
        }
    }

    protected void getComparisonEntity(int entityId) {
        entityComparison.clear();
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            processArcsEntity(entityId);
        } else {
            processEntity(entityId);
        }

        for (int neighborId : validEntities) {
            double weight = getWeight(entityId, neighborId);
            if (weight < 0) {
                continue;
            }

            Comparison comparison = getComparison(entityId, neighborId);
            comparison.setUtilityMeasure(weight);

            topKperEntity.offer(comparison);
            /*entityComparison.add(comparison);*/
        }
        /*System.out.println("topKperEntity: " + topKperEntity.size() + " :: " + validEntities.size());*/
        entityComparison.addAll(topKperEntity);
    }

    protected void setLimits() {
        firstId = 0;
        lastId = noOfEntities;
    }

    @Override
    protected void setThreshold() {
        threshold = this.fixedThreshold;
        System.out.println("Threshold: " + threshold);
    }

    @Override
    protected void verifyValidEntities(int entityId) {
        List<Double> neighbor_weights = new LinkedList<>();
        double max_neighbor = Integer.MIN_VALUE;
        Comparison max_comparison = null;
        for (int neighborId : validEntities) {
            double weight = getWeight(entityId, neighborId);
            if (weight < 0) {
                continue;
            }

            neighbor_weights.add(weight);

            Comparison comparison = getComparison(entityId, neighborId);
            comparison.setUtilityMeasure(weight);

            if (weight > max_neighbor) {
                max_neighbor = weight;
                max_comparison = comparison;
            }
        }
        double max = 0;
        if (neighbor_weights.size() > 1) {
            /*Collections.sort(neighbor_weights);*/
            /*entityThreshold[entityId] = 0;*/
            for (double w : neighbor_weights) {
                entityWeights += w;
                max = Math.max(max, w);
                /*entityThreshold[entityId] = Math.max(entityThreshold[entityId], w);*/
            }
            /*entityWeights /= (neighbor_weights.size());*/
            entityWeights = max;
            if (neighbor_weights.size() == 1) {
                System.out.println("sinle neighbor");
            }
            /*entityWeights /= (neighbor_weights.size() * neighbor_weights.size());*/
            entityWeights /= (neighbor_weights.size());
            /*entityWeights = neighbor_weights.get(neighbor_weights.size() - 1);*/
            /*entityThreshold[entityId] /= 10;*/
            sortedEntityHeap.add(new EntityComparable(entityId, entityWeights));
        }
        /*wThreshold = Math.min(wThreshold, max);*/
        if (max_comparison != null) {
            topComparisons.add(max_comparison);
        }
    }

    @Override
    public String getName() {
        return "CepEntity";
    }

    public void setThreshold(int t) {
        this.fixedThreshold = t;
    }
}
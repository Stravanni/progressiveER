package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import Comparators.ComparisonWeightComparatorMax;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import BlockBuilding.Progressive.DataStructures.MetablockingComparator;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * @author gap2
 */
public class RedundancyCardinalityNodePruning extends MetaBlocking.FastImplementations.CardinalityNodePruning {

    protected Set<Comparison>[] nearestEntities;

    public RedundancyCardinalityNodePruning(WeightingScheme scheme) {
        this("Fast Redundancy Cardinality Node Pruning (" + scheme + ")", scheme);
    }

    protected RedundancyCardinalityNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    protected boolean isValidComparison(int entityId, Comparison comparison) {
        int neighborId = comparison.getEntityId1() == entityId ? comparison.getEntityId2() : comparison.getEntityId1();
        if (cleanCleanER && entityId < datasetLimit) {
            neighborId += datasetLimit;
        }

        if (nearestEntities[neighborId] == null) {
            return true;
        }

        if (nearestEntities[neighborId].contains(comparison)) {
            return entityId < neighborId;
        }

        return true;
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        nearestEntities = new Set[noOfEntities];
        //topKEdges = new PriorityQueue<Comparison>((int) (2 * threshold), new MetablockingComparator());
        topKEdges = MinMaxPriorityQueue.orderedBy(new MetablockingComparator()).create();
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < noOfEntities; i++) {
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                processEntity(i);
                verifyValidEntities(i);
            }
        }
        retainValidComparisons(newBlocks);
    }

    protected void retainValidComparisons(List<AbstractBlock> newBlocks) {
        final List<Comparison> retainedComparisons = new ArrayList<>();
        for (int i = 0; i < noOfEntities; i++) {
            if (nearestEntities[i] != null) {
                retainedComparisons.clear();
                for (Comparison comparison : nearestEntities[i]) {
                    if (isValidComparison(i, comparison)) {
                        retainedComparisons.add(comparison);
                    }
                }
                addDecomposedBlock(retainedComparisons, newBlocks);
            }
        }
    }

    @Override
    protected void verifyValidEntities(int entityId) {
        if (validEntities.isEmpty()) {
            return;
        }

        topKEdges.clear();
        minimumWeight = Double.MIN_VALUE;
        for (int neighborId : validEntities) {
            double weight = getWeight(entityId, neighborId);
            if (weight < minimumWeight) {
                continue;
            }

            Comparison comparison = getComparison(entityId, neighborId);
            comparison.setUtilityMeasure(weight);

            topKEdges.add(comparison);
            if (threshold < topKEdges.size()) {
                Comparison lastComparison = topKEdges.poll();
                minimumWeight = lastComparison.getUtilityMeasure();
            }
        }
        nearestEntities[entityId] = new HashSet<Comparison>(topKEdges);
    }
}

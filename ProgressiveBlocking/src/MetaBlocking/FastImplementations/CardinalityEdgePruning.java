/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */
package MetaBlocking.FastImplementations;

import Comparators.ComparisonWeightComparator;
import Comparators.ComparisonWeightComparatorMax;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.DecomposedBlock;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author gap2
 */
public class CardinalityEdgePruning extends WeightedEdgePruning {

    protected double minimumWeight;
    protected MinMaxPriorityQueue<Comparison> topKEdges;

    public CardinalityEdgePruning(WeightingScheme scheme) {
        this("Fast Cardinality Edge Pruning (" + scheme + ")", scheme);
    }

    protected CardinalityEdgePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
        nodeCentric = false;
    }

    protected void addDecomposedBlock(Collection<Comparison> comparisons, List<AbstractBlock> newBlocks) {
        if (comparisons.isEmpty()) {
            return;
        }

        int[] entityIds1 = new int[comparisons.size()];
        int[] entityIds2 = new int[comparisons.size()];

        int index = 0;
        Iterator<Comparison> iterator = comparisons.iterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            entityIds1[index] = comparison.getEntityId1();
            entityIds2[index] = comparison.getEntityId2();
            index++;
        }

        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        minimumWeight = Double.MIN_VALUE;
        //topKEdges = new PriorityQueue<Comparison>((int) (2 * threshold), new ComparisonWeightComparator());
        topKEdges = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparatorMax()).create();

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                verifyValidEntities(i);
            }
        }

        addDecomposedBlock(topKEdges, newBlocks);
    }

    @Override
    protected void setThreshold() {
        threshold = blockAssingments / 2;
        System.out.println("set threshold to: " + threshold);
    }

    protected void verifyValidEntities(int entityId) {
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
    }
}
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

package MetaBlocking.EnhancedMetaBlocking;

import Comparators.ComparisonWeightComparator;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import MetaBlocking.CardinalityNodePruning;
import MetaBlocking.WeightingScheme;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author gap2
 */
public class ReciprocalCardinalityNodePruning extends CardinalityNodePruning {

    protected PriorityQueue[] nearestEntities;

    public ReciprocalCardinalityNodePruning(WeightingScheme scheme) {
        super("Reciprocal Cardinality Node Pruning", scheme);
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);

        kThreshold = (int) Math.max(1, blockAssingments / entityIndex.getNoOfEntities());
        System.out.println("K-threshold\t:\t" + kThreshold);
        
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);
        blocks.clear();

        nearestEntities = new PriorityQueue[entityIndex.getNoOfEntities()];
        processPartition(0, entityIndex.getDatasetLimit(), blocks);
        processPartition(entityIndex.getDatasetLimit(), entityIndex.getNoOfEntities(), blocks);
        gatherComparisons(blocks);
    }

    private void gatherBilateralComparisons(List<Comparison> retainedComparisons) {
        for (int i = 0; i < entityIndex.getDatasetLimit(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }

            final Iterator<Comparison> iterator = nearestEntities[i].iterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                int entity2 = comparison.getEntityId2();
                if (nearestEntities[entity2 + entityIndex.getDatasetLimit()].contains(comparison)) {
                    retainedComparisons.add(comparison);
                }
            }
        }
    }

    protected void gatherComparisons(List<AbstractBlock> blocks) {
        final List<Comparison> retainedComparisons = new ArrayList<Comparison>();
        if (cleanCleanER) {
            gatherBilateralComparisons(retainedComparisons);
        } else {
            gatherUnilateralComparisons(retainedComparisons);
        }
        blocks.add(getDecomposedBlock(cleanCleanER, retainedComparisons));
    }

    private void gatherUnilateralComparisons(List<Comparison> retainedComparisons) {
        for (int i = 0; i < entityIndex.getNoOfEntities(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }

            final Iterator<Comparison> iterator = nearestEntities[i].iterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                int otherEntity = comparison.getEntityId1() == i ? comparison.getEntityId2() : comparison.getEntityId1();
                if (otherEntity < i) {
                    continue;
                }

                if (nearestEntities[otherEntity].contains(comparison)) {
                    retainedComparisons.add(comparison);
                }
            }
        }
    }

    @Override
    protected void processPartition(int firstId, int lastId, List<AbstractBlock> blocks) {
        for (int i = firstId; i < lastId; i++) {
            final Integer[] neighbors = getAdjacentEntities(i);
            if (neighbors == null) {
                continue;
            }

            nearestEntities[i] = new PriorityQueue<Comparison>((int) (2 * kThreshold), new ComparisonWeightComparator());
            for (int neighborId : neighbors) {
                Comparison comparison = getComparison(i, neighborId);
                comparison.setUtilityMeasure(getWeight(comparison));

                    nearestEntities[i].add(comparison);
                    if (kThreshold < nearestEntities[i].size()) {
                        nearestEntities[i].poll();
                    }
            }
        }
    }
}

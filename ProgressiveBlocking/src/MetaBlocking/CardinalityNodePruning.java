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

package MetaBlocking;

import Comparators.ComparisonWeightComparator;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author gap2
 */
public class CardinalityNodePruning extends AbstractNodePruning {

    protected int kThreshold;

    public CardinalityNodePruning(WeightingScheme scheme) {
        this("Cardinality Node Pruning", scheme);
    }
    
    public CardinalityNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
        kThreshold = -1;
    }

    @Override
    protected void processPartition(int firstId, int lastId, List<AbstractBlock> blocks) {
        kThreshold = (int) Math.max(1, blockAssingments / entityIndex.getNoOfEntities());
        for (int i = firstId; i < lastId; i++) {
            final Integer[] neighbors = getAdjacentEntities(i);
            if (neighbors == null) {
                continue;
            }

            Queue<Comparison> nearestEntities = new PriorityQueue<Comparison>((int) (2 * kThreshold), new ComparisonWeightComparator());
            for (int neighborId : neighbors) {
                Comparison comparison = getComparison(i, neighborId);
                comparison.setUtilityMeasure(getWeight(comparison));

                nearestEntities.add(comparison);
                if (kThreshold < nearestEntities.size()) {
                    nearestEntities.poll();
                }
            }

            blocks.add(getDecomposedBlock(cleanCleanER, nearestEntities));
        }
    }
}

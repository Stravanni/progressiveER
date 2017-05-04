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
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class RedundancyCardinalityNodePruning extends CardinalityNodePruning {
    
    protected Set<Comparison> distinctComparisons;
    
    public RedundancyCardinalityNodePruning(WeightingScheme scheme) {
        super("Redundancy Cardinality Node Pruning", scheme);
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        
        kThreshold = (int) Math.max(1, blockAssingments / entityIndex.getNoOfEntities());
        
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);
        blocks.clear();
        
        distinctComparisons = new HashSet<Comparison>();
        processPartition(0, entityIndex.getDatasetLimit(), blocks);
        processPartition(entityIndex.getDatasetLimit(), entityIndex.getNoOfEntities(), blocks);
        blocks.add(getDecomposedBlock(cleanCleanER, distinctComparisons));
    }
    
    @Override
    protected void processPartition(int firstId, int lastId, List<AbstractBlock> blocks) {
        for (int i = firstId; i < lastId; i++) {
            final Integer[] neighbors = getAdjacentEntities(i);
            if (neighbors == null) {
                continue;
            }
            
            Queue<Comparison> nearestEntities = new PriorityQueue<Comparison>(2 * kThreshold, new ComparisonWeightComparator());
            for (int neighborId : neighbors) {
                Comparison comparison = getComparison(i, neighborId);
                comparison.setUtilityMeasure(getWeight(comparison));

                nearestEntities.add(comparison);
                if (kThreshold < nearestEntities.size()) {
                    nearestEntities.poll();
                }
            }
            distinctComparisons.addAll(nearestEntities);
        }
    }   
}

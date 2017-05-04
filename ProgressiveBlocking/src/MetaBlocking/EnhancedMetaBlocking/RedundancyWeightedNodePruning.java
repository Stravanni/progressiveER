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

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import Utilities.ComparisonIterator;
import MetaBlocking.AbstractNodePruning;
import MetaBlocking.WeightingScheme;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */

public class RedundancyWeightedNodePruning extends AbstractNodePruning {

    protected double[] averageWeight;
    
    public RedundancyWeightedNodePruning(WeightingScheme scheme) {
        this("Redundancy Weighted Node Pruning", scheme);
    }
    
    public RedundancyWeightedNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        
        averageWeight = new double[entityIndex.getNoOfEntities()];
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);
        blocks.clear();
        
        processPartition(0, entityIndex.getDatasetLimit(), blocks);
        processPartition(entityIndex.getDatasetLimit(), entityIndex.getNoOfEntities(), blocks);
        filterComparisons(blocks);
    }
    
    protected void filterComparisons(List<AbstractBlock> blocks) {
        for (AbstractBlock block : blocksArray) {
            final List<Integer> entities1 = new ArrayList<Integer>();
            final List<Integer> entities2 = new ArrayList<Integer>();
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (isValidComparison(block.getBlockIndex(), comparison)) {
                    entities1.add(comparison.getEntityId1());
                    entities2.add(comparison.getEntityId2());
                }
            } 
            blocks.add(getDecomposedBlock(cleanCleanER, entities1, entities2));
        }
    }
    
    protected boolean isValidComparison(int blockIndex, Comparison comparison) {
        double weight = getWeight(blockIndex, comparison);
        if (weight < 0) {
            return false;
        }

        int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();
        return averageWeight[comparison.getEntityId1()] <= weight
               || averageWeight[entityId2] <= weight;
    }
    
    @Override
    protected void processPartition(int firstId, int lastId, List<AbstractBlock> blocks) {
        for (int i = firstId; i < lastId; i++) {
            final Integer[] neighbors = getAdjacentEntities(i);
            if (neighbors == null) {
                continue;
            }
            
            averageWeight[i] = 0;
            for (Integer neighborId : neighbors) {
                Comparison comparison = getComparison(i, neighborId);
                averageWeight[i] += getWeight(comparison);
            }
            averageWeight[i] /= neighbors.length;
        }
    }
}

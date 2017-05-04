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

package OnTheFlyMethods;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import Utilities.ComparisonIterator;
import MetaBlocking.WeightingScheme;
import java.util.List;

/**
 *
 * @author gap2
 */

public class RedundancyWeightedNodePruning extends MetaBlocking.EnhancedMetaBlocking.RedundancyWeightedNodePruning {
    
    protected double retainedComparisons;
    protected final AbstractDuplicatePropagation duplicatePropagation;
    
    public RedundancyWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        this(adp, "Redundancy Weighted Node Pruning", scheme);
    }
    
    public RedundancyWeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme) {
        super(description, scheme);
        duplicatePropagation = adp;
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        
        averageWeight = new double[entityIndex.getNoOfEntities()];
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);
        blocks.clear();
        
        retainedComparisons = 0;
        duplicatePropagation.resetDuplicates();
        processPartition(0, entityIndex.getDatasetLimit(), blocks);
        processPartition(entityIndex.getDatasetLimit(), entityIndex.getNoOfEntities(), blocks);
        filterComparisons(blocks);
        System.out.println("Executed comparisons\t:\t" + retainedComparisons);
        System.out.println("Detected Duplicates\t:\t" + duplicatePropagation.getNoOfDuplicates());
    }
    
    @Override
    protected void filterComparisons(List<AbstractBlock> blocks) {
        for (AbstractBlock block : blocksArray) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (isValidComparison(block.getBlockIndex(), comparison)) {
                    retainedComparisons++;
                    duplicatePropagation.isSuperfluous(comparison);
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
            
            averageWeight[i] = 0;
            for (Integer neighborId : neighbors) {
                Comparison comparison = getComparison(i, neighborId);
                averageWeight[i] += getWeight(comparison);
            }
            averageWeight[i] /= neighbors.length;
        }
    }
}
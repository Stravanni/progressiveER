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
import MetaBlocking.AbstractNodePruning;
import MetaBlocking.WeightingScheme;
import java.util.List;

/**
 *
 * @author gap2
 */

public class WeightedNodePruning extends AbstractNodePruning {
    
    protected double totalComparisons;
    protected final AbstractDuplicatePropagation duplicatePropagation;
    
    public WeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        this(adp, "Weighted Node Pruning", scheme);
    }
    
    public WeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme) {
        super(description, scheme);
        duplicatePropagation = adp;
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);
        blocks.clear();
        
        totalComparisons = 0;
        duplicatePropagation.resetDuplicates();
        processPartition(0, entityIndex.getDatasetLimit(), blocks);
        processPartition(entityIndex.getDatasetLimit(), entityIndex.getNoOfEntities(), blocks);
    }
    
    public double[] getPerformance() {
        double[] metrics = new double[3];
        metrics[0] = duplicatePropagation.getNoOfDuplicates() / ((double) duplicatePropagation.getExistingDuplicates()); //PC
        metrics[1] = duplicatePropagation.getNoOfDuplicates() / totalComparisons; //PQ
        metrics[2] = totalComparisons;
        return metrics;
    }
    
    @Override
    protected void processPartition(int firstId, int lastId, List<AbstractBlock> blocks) {
        for (int i = firstId; i < lastId; i++) {
            final Integer[] neighbors = getAdjacentEntities(i);
            if (neighbors == null) {
                continue;
            }
            
            double averageWeight = 0;
            final double[] weights = new double[neighbors.length];
            for (int j = 0; j < weights.length; j++) {
                Comparison comparison = getComparison(i, neighbors[j]);
                weights[j] = getWeight(comparison);
                averageWeight += weights[j];
            }
            averageWeight /= weights.length;
            
            for (int j = 0; j < weights.length; j++) {
                if (averageWeight <= weights[j]) {
                    totalComparisons++;
                    Comparison comparison = getComparison(i, neighbors[j]);
                    duplicatePropagation.isSuperfluous(comparison);
                }
            }
        }
    }
}
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

import DataStructures.Comparison;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import MetaBlocking.WeightingScheme;

/**
 *
 * @author gap2
 */

public class ReciprocalWeightedNodePruning extends RedundancyWeightedNodePruning {
    
    public ReciprocalWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        super(adp, "Reciprocal Weighted Node Pruning", scheme);
    }
    
    @Override
    protected boolean isValidComparison(int blockIndex, Comparison comparison) {
        double weight = getWeight(blockIndex, comparison);
        if (weight < 0) {
            return false;
        }

        int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();
        return averageWeight[comparison.getEntityId1()] <= weight
               && averageWeight[entityId2] <= weight;
    }
}
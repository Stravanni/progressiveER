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

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */

public class WeightedNodePruning extends AbstractNodePruning {
    
    public WeightedNodePruning(WeightingScheme scheme) {
        this("Weighted Node Pruning", scheme);
    }
    
    public WeightedNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
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
            
            final List<Integer> entitiesPart1 = new ArrayList<Integer>();
            final List<Integer> entitiesPart2 = new ArrayList<Integer>();
            for (int j = 0; j < weights.length; j++) {
                if (averageWeight <= weights[j]) {
                    Comparison comparison = getComparison(i, neighbors[j]);
                    entitiesPart1.add(comparison.getEntityId1());
                    entitiesPart2.add(comparison.getEntityId2());
                }
            }
            blocks.add(getDecomposedBlock(cleanCleanER, entitiesPart1, entitiesPart2));
        }
    }
}
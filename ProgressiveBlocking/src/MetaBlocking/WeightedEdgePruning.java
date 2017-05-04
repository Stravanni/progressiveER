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
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import Utilities.ComparisonIterator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */
public class WeightedEdgePruning extends AbstractMetablocking {

    protected double averageWeight;

    public WeightedEdgePruning(WeightingScheme scheme) {
        this("Weighted Edge Pruning", scheme);
    }
    
    protected WeightedEdgePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        setAverageWeight(blocks);
        filterComparisons(blocks);
    }

    protected void filterComparisons(List<AbstractBlock> blocks) {
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock block : blocks) {
            final List<Integer> entities1 = new ArrayList<Integer>();
            final List<Integer> entities2 = new ArrayList<Integer>();

            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                double weight = getWeight(block.getBlockIndex(), comparison);
                if (weight < averageWeight) {
                    continue;
                }

                entities1.add(comparison.getEntityId1());
                entities2.add(comparison.getEntityId2());
            }
            newBlocks.add(getDecomposedBlock(cleanCleanER, entities1, entities2));
        }
        blocks.clear();
        blocks.addAll(newBlocks);
    }

    protected void setAverageWeight(List<AbstractBlock> blocks) {
        double totalComparisons = 0;
        averageWeight = 0;
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                double weight = getWeight(block.getBlockIndex(), comparison);
                if (weight < 0) {
                    continue;
                }

                averageWeight += weight;
                totalComparisons++;
            }
        }
        averageWeight /= totalComparisons;
    }
}

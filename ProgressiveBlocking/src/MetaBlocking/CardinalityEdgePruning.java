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
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import Utilities.ComparisonIterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author gap2
 */
public class CardinalityEdgePruning extends AbstractMetablocking {

    protected long kThreshold;
    protected double minimumWeight;
    protected Queue<Comparison> topKEdges;

    public CardinalityEdgePruning(WeightingScheme scheme) {
        super("Cardinality Edge Pruning (Top-K Edges)", scheme);
    }
    
    protected CardinalityEdgePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    private void addComparison(Comparison comparison) {
        if (comparison.getUtilityMeasure() < minimumWeight) {
            return;
        }

        topKEdges.add(comparison);
        if (kThreshold < topKEdges.size()) {
            Comparison lastComparison = topKEdges.poll();
            minimumWeight = lastComparison.getUtilityMeasure();
        }
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        getKThreshold(blocks);
        filterComparisons(blocks);
        gatherComparisons(blocks);
    }

    protected void filterComparisons(List<AbstractBlock> blocks) {
        minimumWeight = Double.MIN_VALUE;
        topKEdges = new PriorityQueue<Comparison>((int) (2 * kThreshold), new ComparisonWeightComparator());
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                double weight = getWeight(block.getBlockIndex(), comparison);
                if (weight < 0) {
                    continue;
                }

                comparison.setUtilityMeasure(weight);
                addComparison(comparison);
            }
        }
    }

    private void gatherComparisons(List<AbstractBlock> blocks) {
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocks.clear();
        blocks.add(getDecomposedBlock(cleanCleanER, topKEdges));
    }

    protected void getKThreshold(List<AbstractBlock> blocks) {
        long blockAssingments = 0;
        for (AbstractBlock block : blocks) {
            blockAssingments += block.getTotalBlockAssignments();
        }
        kThreshold = blockAssingments / 2;
    }
}

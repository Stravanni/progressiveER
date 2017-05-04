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
package MetaBlocking.FastImplementations;

import Comparators.ComparisonWeightComparator;
import Comparators.ComparisonWeightComparatorMax;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.List;
import java.util.PriorityQueue;

/**
 * @author gap2
 */
public class CardinalityNodePruning extends CardinalityEdgePruning {

    protected int firstId;
    protected int lastId;

    public CardinalityNodePruning(WeightingScheme scheme) {
        this("Fast Cardinality Node Pruning (" + scheme + ")", scheme);
    }

    protected CardinalityNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
        nodeCentric = true;
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        setLimits();
        //topKEdges = new PriorityQueue<Comparison>((int) (2 * threshold), new ComparisonWeightComparator());
        topKEdges = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparator()).create();
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = firstId; i < lastId; i++) {
                minimumWeight = Double.MIN_VALUE;
                topKEdges.clear();
                processArcsEntity(i);
                verifyValidEntities(i);
                addDecomposedBlock(topKEdges, newBlocks);
            }
        } else {
            for (int i = firstId; i < lastId; i++) {
                minimumWeight = Double.MIN_VALUE;
                topKEdges.clear();
                processEntity(i);
                verifyValidEntities(i);
                addDecomposedBlock(topKEdges, newBlocks);
            }
        }
    }

    protected void setLimits() {
        firstId = 0;
        lastId = noOfEntities;
    }

    @Override
    protected void setThreshold() {
        threshold = 10 * Math.max(1, blockAssingments / noOfEntities);
        System.out.println("Threshold\t:\t" + threshold);
    }
}

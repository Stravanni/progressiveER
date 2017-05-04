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

package BlockProcessing.BlockRefinement;

import DataStructures.AbstractBlock;
import BlockProcessing.AbstractEfficiencyMethod;
import Comparators.BlockCardinalityComparator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author gap2
 */

public class ComparisonsBasedBlockPurging extends AbstractEfficiencyMethod {

    private double SMOOTHING_FACTOR = 1.025;

    public ComparisonsBasedBlockPurging() {
        super("(Comparisons-based) Block Purging");
    }

    public ComparisonsBasedBlockPurging(double smoothingFactor) {
        super("(Comparisons-based) Block Purging");
        SMOOTHING_FACTOR = smoothingFactor;
        System.out.println("Smoothing factor\t:\t" + SMOOTHING_FACTOR);
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        double maxComparisonsPerBlock = getMaxComparisonsPerBlock(blocks);

        Iterator blocksIterator = blocks.iterator();
        while (blocksIterator.hasNext()) {
            AbstractBlock currentBlock = (AbstractBlock) blocksIterator.next();
            if (maxComparisonsPerBlock < currentBlock.getNoOfComparisons()) {
                blocksIterator.remove();
            }
        }
    }

    private double getMaxComparisonsPerBlock(List<AbstractBlock> blocks) {
        Collections.sort(blocks, new BlockCardinalityComparator());
        final Set<Double> distinctComparisonsLevel = new HashSet<Double>();
        for (AbstractBlock block : blocks) {
            distinctComparisonsLevel.add(block.getNoOfComparisons());
        }

        int index = -1;
        double[] blockAssignments = new double[distinctComparisonsLevel.size()];
        double[] comparisonsLevel = new double[distinctComparisonsLevel.size()];
        double[] totalComparisonsPerLevel = new double[distinctComparisonsLevel.size()];
        for (AbstractBlock block : blocks) {
            if (index == -1) {
                index++;
                comparisonsLevel[index] = block.getNoOfComparisons();
                blockAssignments[index] = 0;
                totalComparisonsPerLevel[index] = 0;
            } else if (block.getNoOfComparisons() != comparisonsLevel[index]) {
                index++;
                comparisonsLevel[index] = block.getNoOfComparisons();
                blockAssignments[index] = blockAssignments[index - 1];
                totalComparisonsPerLevel[index] = totalComparisonsPerLevel[index - 1];
            }

            blockAssignments[index] += block.getTotalBlockAssignments();
            totalComparisonsPerLevel[index] += block.getNoOfComparisons();
        }

        double currentBC = 0;
        double currentCC = 0;
        double currentSize = 0;
        double previousBC = 0;
        double previousCC = 0;
        double previousSize = 0;
        int arraySize = blockAssignments.length;
        for (int i = arraySize - 1; 0 <= i; i--) {
            previousSize = currentSize;
            previousBC = currentBC;
            previousCC = currentCC;

            currentSize = comparisonsLevel[i];
            currentBC = blockAssignments[i];
            currentCC = totalComparisonsPerLevel[i];

            if (currentBC * previousCC < SMOOTHING_FACTOR * currentCC * previousBC) {
                break;
            }
        }

        System.out.println("previousSize: " + previousSize);
        return previousSize;
    }
}
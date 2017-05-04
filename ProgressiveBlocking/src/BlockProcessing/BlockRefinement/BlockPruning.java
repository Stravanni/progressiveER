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
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import java.util.List;

/**
 *
 * @author gap2
 */

public class BlockPruning extends AbstractEfficiencyMethod {

    public BlockPruning() {
        super("Block Pruning");
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks, AbstractDuplicatePropagation adp) {
        int latestDuplicates = 0;
        double latestComparisons = 0;
        double totalComparisons = 0;
        double maxDuplicateOverhead = getMaxDuplicateOverhead(blocks);
        System.out.println("Maximum Duplicate Overhead\t:\t" + maxDuplicateOverhead);
        
        for (AbstractBlock block : blocks) {
            latestComparisons += block.processBlock(adp);
            
            int currentDuplicates = adp.getNoOfDuplicates();
            if (currentDuplicates == latestDuplicates) {
                continue;
            }

            int noOfNewDuplicates = currentDuplicates - latestDuplicates;
            double duplicateOverhead = latestComparisons / noOfNewDuplicates;
            if (maxDuplicateOverhead < duplicateOverhead) {
                totalComparisons += latestComparisons;
                break;
            }

            totalComparisons += latestComparisons;
            latestComparisons = 0;
            latestDuplicates = adp.getNoOfDuplicates();
        }

        System.out.println("Detected duplicates\t:\t" + adp.getNoOfDuplicates());
        System.out.println("Executed comparisons\t:\t" + totalComparisons);
    }

    private double getMaxDuplicateOverhead(List<AbstractBlock> blocks) {
        double totalComparisons = 0;
        for (AbstractBlock block : blocks) {
            totalComparisons += block.getNoOfComparisons();
        }
        return Math.pow(10, Math.log10(totalComparisons) / 2.0);
    }
}
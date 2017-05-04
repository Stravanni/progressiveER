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

package BlockProcessing;

import DataStructures.AbstractBlock;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import java.util.List;

/**
 *
 * @author gap2
 */

public abstract class AbstractEfficiencyMethod {
    
    private final String name;
    
    public AbstractEfficiencyMethod(String nm) {
        name = nm;
    }

    public String getName() {
        return name;
    }
    
    public abstract void applyProcessing(List<AbstractBlock> blocks);
    
    public void applyProcessing(List<AbstractBlock> blocks, AbstractDuplicatePropagation adp) {
        applyProcessing(blocks);

        double comparisons = 0;
        for (AbstractBlock block : blocks) {            
            comparisons += block.processBlock(adp);
        }

        System.out.println("Detected duplicates\t:\t" + adp.getNoOfDuplicates());
        System.out.println("Executed comparisons\t:\t" + comparisons);
    }
}
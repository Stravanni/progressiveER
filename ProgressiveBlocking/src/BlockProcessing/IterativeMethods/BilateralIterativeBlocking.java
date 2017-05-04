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

package BlockProcessing.IterativeMethods;

import Comparators.BlockCardinalityComparator;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.IdDuplicates;
import Utilities.ComparisonIterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class BilateralIterativeBlocking {
    
    private final Set<IdDuplicates> duplicates;
    private final Set<Integer> entities1;
    private final Set<Integer> entities2;
    
    public BilateralIterativeBlocking(Set<IdDuplicates> matches) {
        duplicates = matches;
        entities1 = new HashSet<Integer>(2*duplicates.size());
        entities2 = new HashSet<Integer>(2*duplicates.size());
    }
    
    public void applyProcessing(List<AbstractBlock> blocks) {
        System.out.println("\n\nApplying processing...");
        
        double comparisons = 0;
        Collections.sort(blocks, new BlockCardinalityComparator());
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (!isSuperfluous(comparison)) {
                    comparisons++;
                }
            }
        }
        
        System.out.println("Detected duplicates\t:\t" + entities1.size());
        System.out.println("Executed comparisons\t:\t" + comparisons);
    } 
    
    private boolean isSuperfluous(Comparison comparison) {
        Integer id1 = comparison.getEntityId1();
        Integer id2 = comparison.getEntityId2();
        if (entities1.contains(id1) && entities2.contains(id2)) { 
            return true;
        }
        
        final IdDuplicates tempDuplicates = new IdDuplicates(id1, id2);
        if (duplicates.contains(tempDuplicates)) {
            entities1.add(id1);
            entities2.add(id2);
        }
        
        return false;
    }
}
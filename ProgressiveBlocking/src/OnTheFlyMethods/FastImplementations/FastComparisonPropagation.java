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
package OnTheFlyMethods.FastImplementations;

import DataStructures.AbstractBlock;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author gap2
 */
public class FastComparisonPropagation extends BlockProcessing.ComparisonRefinement.FastComparisonPropagation {

    private double uniqueComparisons;
    
    public FastComparisonPropagation() {
        super("On-the-fly Fast Comparisons Propagation");
    }
    
    @Override
    protected void addDecomposedBlock(int entityId, Collection<Integer> neighbors, List<AbstractBlock> newBlocks) {
        uniqueComparisons += validEntities.size();
    }
    
    public double getUniqueComparisons() {
        return uniqueComparisons;
    }
}

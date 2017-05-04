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

package BlockProcessing.ComparisonRefinement;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.DecomposedBlock;
import DataStructures.EntityIndex;
import BlockProcessing.AbstractEfficiencyMethod;
import Utilities.ComparisonIterator;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */

public class ComparisonPropagation extends AbstractEfficiencyMethod {
    
    protected EntityIndex entityIndex;
    
    public ComparisonPropagation() {
        super("Comparisons Propagation");
    }
    
    public ComparisonPropagation(EntityIndex eIndex) {
        super("Comparisons Propagation");
        entityIndex = eIndex;
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        if (entityIndex == null) {
            entityIndex = new EntityIndex(blocks);
        }

        final List<AbstractBlock> redundancyFreeBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock block : blocks) {
            final List<Integer> entities1 = new ArrayList<Integer>();
            final List<Integer> entities2 = new ArrayList<Integer>();
        
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (!entityIndex.isRepeated(block.getBlockIndex(), comparison)) {
                    entities1.add(comparison.getEntityId1());
                    entities2.add(comparison.getEntityId2());
                }
            }
            
            int[] entityIds1 = Converter.convertCollectionToArray(entities1);
            int[] entityIds2 = Converter.convertCollectionToArray(entities2);
            boolean cleanCleanER = block instanceof BilateralBlock;
            redundancyFreeBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
        }
        blocks.clear();
        blocks.addAll(redundancyFreeBlocks);
    }
}
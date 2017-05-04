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
import BlockProcessing.AbstractFastEfficiencyMethod;
import java.util.List;

/**
 *
 * @author gap2
 */
public class FastComparisonPropagation extends AbstractFastEfficiencyMethod {

    public FastComparisonPropagation() {
        this("Comparisons Propagation");
    }
    
    public FastComparisonPropagation(String description) {
        super(description);
    }

    @Override
    protected void applyMainProcessing(List<AbstractBlock> blocks) {
        if (cleanCleanER) {
            processBilateralBlocks(blocks);
        } else {
            processUnilateralBlocks(blocks);
        }
    }

    private void processBilateralBlocks(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < datasetLimit; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                validEntities.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : bBlocks[blockIndex].getIndex2Entities()) {
                        validEntities.add(neighborId);
                    }
                }
                addDecomposedBlock(i, validEntities, newBlocks);
            }
        }
    }

    private void processUnilateralBlocks(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < noOfEntities; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                validEntities.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : uBlocks[blockIndex].getEntities()) {
                        if (neighborId < i) {
                            validEntities.add(neighborId);
                        }
                    }
                }
                addDecomposedBlock(i, validEntities, newBlocks);
            }
        }
    }
}

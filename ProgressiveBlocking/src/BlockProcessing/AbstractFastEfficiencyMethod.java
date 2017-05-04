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
import DataStructures.BilateralBlock;
import DataStructures.DecomposedBlock;
import DataStructures.FastEntityIndex;
import DataStructures.UnilateralBlock;
import Utilities.Converter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */
public abstract class AbstractFastEfficiencyMethod extends AbstractEfficiencyMethod {

    protected boolean cleanCleanER;

    protected int datasetLimit;
    protected int noOfBlocks;
    protected int noOfEntities;

    protected FastEntityIndex entityIndex;
    protected BilateralBlock[] bBlocks;
    protected final Set<Integer> validEntities;
    protected UnilateralBlock[] uBlocks;

    public AbstractFastEfficiencyMethod(String nm) {
        super(nm);
        validEntities = new HashSet<>();
    }

    protected void addDecomposedBlock(int entityId, Collection<Integer> neighbors, List<AbstractBlock> newBlocks) {
        if (neighbors.isEmpty()) {
            return;
        }

        int[] entityIds1 = replicateId(entityId, neighbors.size());
        int[] entityIds2 = Converter.convertCollectionToArray(neighbors);
        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
    }
    
    protected void addReversedDecomposedBlock(int entityId, Collection<Integer> neighbors, List<AbstractBlock> newBlocks) {
        if (neighbors.isEmpty()) {
            return;
        }

        int[] entityIds1 = Converter.convertCollectionToArray(neighbors);
        int[] entityIds2 = replicateId(entityId, neighbors.size());
        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
    }
    
    protected abstract void applyMainProcessing(List<AbstractBlock> blocks);

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        entityIndex = new FastEntityIndex(blocks);
        
        cleanCleanER = entityIndex.isCleanCleanER();
        datasetLimit = entityIndex.getDatasetLimit();
        noOfBlocks = blocks.size();
        noOfEntities = entityIndex.getNoOfEntities();
        bBlocks = entityIndex.getBilateralBlocks();
        uBlocks = entityIndex.getUnilateralBlocks();

        blocks.clear();
        applyMainProcessing(blocks);
    }
    
    protected int[] replicateId(int entityId, int times) {
        int counter = 0;
        int[] array = new int[times];
        for (int i = 0; i < times; i++) {
            array[counter++] = entityId;
        }
        return array;
    }
}

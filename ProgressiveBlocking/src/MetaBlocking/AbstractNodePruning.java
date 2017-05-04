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

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.UnilateralBlock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */

public abstract class AbstractNodePruning extends AbstractMetablocking {
    
    protected boolean cleanCleanER;
    protected AbstractBlock[] blocksArray;
    
    public AbstractNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);
        blocks.clear();
        
        processPartition(0, entityIndex.getDatasetLimit(), blocks);
        processPartition(entityIndex.getDatasetLimit(), entityIndex.getNoOfEntities(), blocks);
    }
    
    protected Integer[] getAdjacentEntities(int entityId) { // continuous entity id
        int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) { // singleton entity
            return null;
        }
        
        Set<Integer> adjacentEntities = new HashSet<Integer>();
        if (!cleanCleanER) {
            for (int blockIndex : associatedBlocks) {
                UnilateralBlock block = (UnilateralBlock) blocksArray[blockIndex];
                for (int neighborId : block.getEntities()) {
                    adjacentEntities.add(neighborId);
                }
            }
            adjacentEntities.remove(entityId);
        } else {
            boolean firstPartition = entityId < entityIndex.getDatasetLimit();
            for (int blockIndex : associatedBlocks) {
                BilateralBlock block = (BilateralBlock) blocksArray[blockIndex];
                if (firstPartition) {
                    for (int neighborId : block.getIndex2Entities()) {
                        adjacentEntities.add(neighborId);
                    }
                } else {
                    for (int neighborId : block.getIndex1Entities()) {
                        adjacentEntities.add(neighborId);
                    }
                }
            }
        }
        
        return adjacentEntities.toArray(new Integer[adjacentEntities.size()]);
    }
    
    protected Comparison getComparison(int entityId1, int entityId2) {
        if (cleanCleanER) {
            if (entityIndex.getDatasetLimit() <= entityId1) {
                //entity 1 belongs to the second/right partition and its id should be normalized
                return new Comparison(cleanCleanER, entityId2, entityId1-entityIndex.getDatasetLimit());
            } else {
                return new Comparison(cleanCleanER, entityId1, entityId2);
            }
        }
        
        if (entityId1 < entityId2) {
            return new Comparison(cleanCleanER, entityId1, entityId2);
        }
        
        return new Comparison(cleanCleanER, entityId2, entityId1);
    }
    
    protected abstract void processPartition(int firstId, int lastId, List<AbstractBlock> blocks);
}
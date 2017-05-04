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
package BlockBuilding;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.EntityIndex;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */
public abstract class AbstractCanopies extends AbstractQGramsBlocking {

    protected int noOfBlocks;
    protected int datasetLimit;
    protected int totalEntities;
    protected int[] flags;
    protected double[] counters;

    protected BilateralBlock[] bBlocks;
    protected EntityIndex entityIndex;
    protected final List<Integer> neighbors;
    protected final List<Integer> retainedNeighbors;
    protected final Set<Integer> removedEntities;
    protected final Set<Integer> validEntities;    
    protected UnilateralBlock[] uBlocks;

    public AbstractCanopies(int n, String description, List<EntityProfile>[] profiles) {
        super(n, description, profiles);
        neighbors = new ArrayList<>();
        retainedNeighbors = new ArrayList<>();
        removedEntities = new HashSet<>();
        validEntities = new HashSet<>();
    }
    
    public AbstractCanopies(int n, String description, String[] entities, String[] index) {
        super(n, description, entities, index);
        neighbors = new ArrayList<>();
        retainedNeighbors = new ArrayList<>();
        removedEntities = new HashSet<>();
        validEntities = new HashSet<>();
    }

    protected abstract void getBilateralBlocks();

    protected abstract void getUnilateralBlocks();

    protected void addBilateralBlock(int entityId) {
        if (!retainedNeighbors.isEmpty()) {
            int[] blockEntityIds1 = {entityId};
            int[] blockEntityIds2 = Converter.convertCollectionToArray(retainedNeighbors);
            blocks.add(new BilateralBlock(blockEntityIds1, blockEntityIds2));
        }
    }

    protected void addUnilateralBlock(int entityId) {
        if (!retainedNeighbors.isEmpty()) {
            retainedNeighbors.add(entityId);
            int[] blockEntityIds = Converter.convertCollectionToArray(retainedNeighbors);
            blocks.add(new UnilateralBlock(blockEntityIds));
        }
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        List<AbstractBlock> qgramsBlocks = super.buildBlocks();
        entityIndex = new EntityIndex(qgramsBlocks);
        
        noOfBlocks = qgramsBlocks.size();
        datasetLimit = entityIndex.getDatasetLimit();
        totalEntities = entityIndex.getNoOfEntities();
        counters = new double[totalEntities];
        flags = new int[totalEntities];
        for (int i = 0; i < totalEntities; i++) {
            flags[i] = -1;
        }

        int counter = 0;
        if (cleanCleanER) {
            bBlocks = new BilateralBlock[noOfBlocks];
            for (AbstractBlock block : qgramsBlocks) {
                bBlocks[counter++] = (BilateralBlock) block;
            }
            qgramsBlocks.clear();

            getBilateralBlocks();
        } else {
            uBlocks = new UnilateralBlock[noOfBlocks];
            for (AbstractBlock block : qgramsBlocks) {
                uBlocks[counter++] = (UnilateralBlock) block;
            }
            qgramsBlocks.clear();

            getUnilateralBlocks();
        }
        return blocks;
    }

    protected void setNeighborEntities(int blockIndex, int entityId) {
        neighbors.clear();
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                for (int originalId : bBlocks[blockIndex].getIndex2Entities()) {
                    neighbors.add(originalId);
                }
            } else {
                for (int originalId : bBlocks[blockIndex].getIndex1Entities()) {
                    neighbors.add(originalId);
                }
            }
        } else {
            for (int neighborId : uBlocks[blockIndex].getEntities()) {
                if (neighborId != entityId) {
                    neighbors.add(neighborId);
                }
            }
        }
    }

    protected void setUnilateralValidEntities(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            setNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (!removedEntities.contains(neighborId)) {
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    validEntities.add(neighborId);
                }
            }
        }
    }
    
    protected void setBilateralValidEntities(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            setNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (!removedEntities.contains(neighborId)) {
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId]++;
                    validEntities.add(neighborId);
                }
            }
        }
    }
}

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

package DataStructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */
public class GroundTruthEntityIndex {

    private int datasetLimit;
    private int noOfEntities;
    private int[][] entityBlocks;
    private Set<IdDuplicates> duplicates;

    public GroundTruthEntityIndex(List<AbstractBlock> blocks, Set<IdDuplicates> matches) {
        if (blocks.isEmpty()) {
            System.err.println("Entity index received an empty block collection as input!");
            return;
        }

        if (blocks.get(0) instanceof DecomposedBlock) {
            System.err.println("The entity index is incompatible with a set of decomposed blocks!");
            System.err.println("Its functionalities can be carried out with same efficiency through a linear search of all comparisons!");
            return;
        }

        duplicates = matches;
        enumerateBlocks(blocks);
        setNoOfEntities(blocks);
        indexEntities(blocks);
    }

    private void enumerateBlocks(List<AbstractBlock> blocks) {
        int blockIndex = 0;
        for (AbstractBlock block : blocks) {
            block.setBlockIndex(blockIndex++);
        }
    }

    public List<Integer> getCommonBlockIndices(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        boolean firstCommonIndex = false;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        final List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) {
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    if (!firstCommonIndex) {
                        firstCommonIndex = true;
                        if (blocks1[i] != blockIndex) {
                            return null;
                        }
                    }
                    indices.add(blocks1[i]);
                }
            }
        }

        return indices;
    }

    public int getDatasetLimit() {
        return datasetLimit;
    }

    public int[] getEntityBlocks(int entityId, int useDLimit) {
        entityId += useDLimit * datasetLimit;
        if (noOfEntities <= entityId) {
            return null;
        }
        return entityBlocks[entityId];
    }

    public int getNoOfCommonBlocks(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        boolean firstCommonIndex = false;
        int commonBlocks = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) {
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    commonBlocks++;
                    if (!firstCommonIndex) {
                        firstCommonIndex = true;
                        if (blocks1[i] != blockIndex) {
                            return -1;
                        }
                    }
                }
            }
        }

        return commonBlocks;
    }

    public int getNoOfEntities() {
        return noOfEntities;
    }

    public int getNoOfEntityBlocks(int entityId, int useDLimit) {
        entityId += useDLimit * datasetLimit;
        if (entityBlocks[entityId] == null) {
            return -1;
        }

        return entityBlocks[entityId].length;
    }

    public List<Integer> getTotalCommonIndices(Comparison comparison) {
        final List<Integer> indices = new ArrayList<Integer>();

        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];
        if (blocks1 == null || blocks2 == null) {
            return indices;
        }

        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) {
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    indices.add(blocks1[i]);
                }
            }
        }

        return indices;
    }

    public int getTotalNoOfCommonBlocks(Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];
        if (blocks1 == null || blocks2 == null) {
            return 0;
        }

        int commonBlocks = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) {
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    commonBlocks++;
                }
            }
        }

        return commonBlocks;
    }

    private void indexBilateralEntities(List<AbstractBlock> blocks) {
        //find matching entities
        Set<Integer> matchingEntities = new HashSet<Integer>();
        for (IdDuplicates pair : duplicates) {
            matchingEntities.add(pair.getEntityId1());
            matchingEntities.add(pair.getEntityId2() + datasetLimit);
        }
        
        //count blocks per matching entity
        int[] counters = new int[noOfEntities];
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                if (matchingEntities.contains(id1)) {
                    counters[id1]++;
                }
            }
            
            for (int id2 : bilBlock.getIndex2Entities()) {
                int entityId = datasetLimit+id2;
                if (matchingEntities.contains(entityId)) {
                    counters[entityId]++;
                }
            }
        }
        
        //initialize inverted index
        entityBlocks = new int[noOfEntities][];
        for (int i = 0; i < noOfEntities; i++) {
            entityBlocks[i] = new int[counters[i]];
            counters[i] = 0;
        }
        
        //build inverted index
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                if (matchingEntities.contains(id1)) {
                    entityBlocks[id1][counters[id1]] = block.getBlockIndex();
                    counters[id1]++;
                }
            }
            
            for (int id2 : bilBlock.getIndex2Entities()) {
                int entityId = datasetLimit+id2;
                if (matchingEntities.contains(entityId)) {
                    entityBlocks[entityId][counters[entityId]] = block.getBlockIndex();
                    counters[entityId]++;
                }
            }
        }
    }

    private void indexEntities(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            indexBilateralEntities(blocks);
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            indexUnilateralEntities(blocks);
        }
    }

    private void indexUnilateralEntities(List<AbstractBlock> blocks) {
        //find matching entities
        Set<Integer> matchingEntities = new HashSet<Integer>();
        for (IdDuplicates pair : duplicates) {
            matchingEntities.add(pair.getEntityId1());
            matchingEntities.add(pair.getEntityId2());
        }

        //count blocks per matching entity
        int[] counters = new int[noOfEntities];
        for (AbstractBlock block : blocks) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            for (int id : uniBlock.getEntities()) {
                if (matchingEntities.contains(id)) {
                    counters[id]++;
                }
            }
        }
        
        //initialize inverted index
        entityBlocks = new int[noOfEntities][];
        for (int i = 0; i < noOfEntities; i++) {
            entityBlocks[i] = new int[counters[i]];
            counters[i] = 0;
        }
        
        //build inverted index
        for (AbstractBlock block : blocks) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            for (int id : uniBlock.getEntities()) {
                if (matchingEntities.contains(id)) {
                    entityBlocks[id][counters[id]] = block.getBlockIndex();
                    counters[id]++;
                }
            }
        }
    }

    public boolean isRepeated(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) {
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    return blocks1[i] != blockIndex;
                }
            }
        }

        System.err.println("Error!!!!");
        return false;
    }

    private void setNoOfEntities(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof UnilateralBlock) {
            setNoOfUnilateralEntities(blocks);
        } else if (blocks.get(0) instanceof BilateralBlock) {
            setNoOfBilateralEntities(blocks);
        }
    }

    private void setNoOfBilateralEntities(List<AbstractBlock> blocks) {
        noOfEntities = Integer.MIN_VALUE;
        datasetLimit = Integer.MIN_VALUE;
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                if (noOfEntities < id1 + 1) {
                    noOfEntities = id1 + 1;
                }
            }

            for (int id2 : bilBlock.getIndex2Entities()) {
                if (datasetLimit < id2 + 1) {
                    datasetLimit = id2 + 1;
                }
            }
        }

        int temp = noOfEntities;
        noOfEntities += datasetLimit;
        datasetLimit = temp;
    }

    private void setNoOfUnilateralEntities(List<AbstractBlock> blocks) {
        noOfEntities = Integer.MIN_VALUE;
        datasetLimit = 0;
        for (AbstractBlock block : blocks) {
            UnilateralBlock bilBlock = (UnilateralBlock) block;
            for (int id : bilBlock.getEntities()) {
                if (noOfEntities < id + 1) {
                    noOfEntities = id + 1;
                }
            }
        }
    }
}

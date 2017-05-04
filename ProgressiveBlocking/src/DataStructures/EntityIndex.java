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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gap2
 * @author giovanni (bug fix O(n+m) vs. O(n*m))
 */

public class EntityIndex implements Serializable {

    private static final long serialVersionUID = 13483254243447L;

    private int datasetLimit;
    private int noOfEntities;
    private int validEntities1;
    private int validEntities2;
    private int[][] entityBlocks;

    public int countRepeatedComparisonCheck;

    public EntityIndex(List<AbstractBlock> blocks) {
        if (blocks.isEmpty()) {
            System.err.println("Entity index received an empty block collection as input!");
            return;
        }

        if (blocks.get(0) instanceof DecomposedBlock) {
            System.err.println("The entity index is incompatible with a set of decomposed blocks!");
            System.err.println("Its functionalities can be carried out with same efficiency through a linear search of all comparisons!");
            return;
        }

        enumerateBlocks(blocks);
        setNoOfEntities(blocks);
        indexEntities(blocks);

        countRepeatedComparisonCheck = 0;
    }

    private void enumerateBlocks(List<AbstractBlock> blocks) {
        int blockIndex = 0;
        if (blocks.get(0).getBlockIndex() < 0) {
            for (AbstractBlock block : blocks) {
                block.setBlockIndex(blockIndex++);
            }
        }
    }

    public List<Integer> getCommonBlockIndices(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        boolean firstCommonIndex = false;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        final List<Integer> indices = new ArrayList<>();

        int i = 0;
        int j = 0;

        // the original code did not have linear time execution
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
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
                    j++;
                }
            }
            i++;
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


        int i = 0;
        int j = 0;

        // the original code did not have linear time execution
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    j++;
                    commonBlocks++;
                    if (!firstCommonIndex) {
                        firstCommonIndex = true;
                        if (blocks1[i] != blockIndex) {
                            return -1;
                        }
                    }
                }
            }
            i++;
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
        final List<Integer> indices = new ArrayList<>();

        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];
        if (blocks1.length == 0 || blocks2.length == 0) {
            return indices;
        }

        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;

        int i = 0;
        int j = 0;

        // the original code did not have linear time execution
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    i++;
                    indices.add(blocks1[i]);
                }
            }
            i++;
        }

        return indices;
    }

    public int getTotalNoOfCommonBlocks(Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];
        if (blocks1.length == 0 || blocks2.length == 0) {
            return 0;
        }

        int commonBlocks = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;

        int i = 0;
        int j = 0;

        // the initial solution did not have linear time execution
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    j++;
                    commonBlocks++;
                }
            }
            i++;
        }

        return commonBlocks;
    }

    public int getValidEntities1() {
        return validEntities1;
    }

    public int getValidEntities2() {
        return validEntities2;
    }

    private void indexEntities(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            indexBilateralEntities(blocks);
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            indexUnilateralEntities(blocks);
        } else if (blocks.get(0) instanceof UnilateralHierarchicalBlock) {
            indexHierarchicalUnilateralEntities(blocks);
        } else if (blocks.get(0) instanceof BilateralHierarchicalBlock) {
            indexHierarchicalBilateralEntities(blocks);
        }
    }

    private void indexBilateralEntities(List<AbstractBlock> blocks) {
        //count valid entities & blocks per entity
        validEntities1 = 0;
        validEntities2 = 0;
        int[] counters = new int[noOfEntities];
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                if (counters[id1] == 0) {
                    validEntities1++;
                }
                counters[id1]++;
            }

            for (int id2 : bilBlock.getIndex2Entities()) {
                int entityId = datasetLimit + id2;
                if (counters[entityId] == 0) {
                    validEntities2++;
                }
                counters[entityId]++;
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
                entityBlocks[id1][counters[id1]] = block.getBlockIndex();
                counters[id1]++;
            }

            for (int id2 : bilBlock.getIndex2Entities()) {
                int entityId = datasetLimit + id2;
                entityBlocks[entityId][counters[entityId]] = block.getBlockIndex();
                counters[entityId]++;
            }
        }
    }

    private void indexHierarchicalBilateralEntities(List<AbstractBlock> blocks) {
        //count valid entities & blocks per entity
        validEntities1 = 0;
        validEntities2 = 0;
        int[] counters = new int[noOfEntities];
        for (AbstractBlock block : blocks) {
            BilateralHierarchicalBlock bilBlock = (BilateralHierarchicalBlock) block;
            for (int id1 : bilBlock.getEntitySet1()) {
                if (counters[id1] == 0) {
                    validEntities1++;
                }
                counters[id1]++;
            }

            for (int id2 : bilBlock.getEntitySet2()) {
                int entityId = datasetLimit + id2;
                if (counters[entityId] == 0) {
                    validEntities2++;
                }
                counters[entityId]++;
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
            BilateralHierarchicalBlock bilBlock = (BilateralHierarchicalBlock) block;
            for (int id1 : bilBlock.getEntitySet1()) {
                entityBlocks[id1][counters[id1]] = block.getBlockIndex();
                counters[id1]++;
            }

            for (int id2 : bilBlock.getEntitySet2()) {
                int entityId = datasetLimit + id2;
                entityBlocks[entityId][counters[entityId]] = block.getBlockIndex();
                counters[entityId]++;
            }
        }
    }

    private void indexUnilateralEntities(List<AbstractBlock> blocks) {
        //count valid entities & blocks per entity
        validEntities1 = 0;
        int[] counters = new int[noOfEntities];
        for (AbstractBlock block : blocks) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            for (int id : uniBlock.getEntities()) {
                if (counters[id] == 0) {
                    validEntities1++;
                }
                counters[id]++;
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
                entityBlocks[id][counters[id]] = block.getBlockIndex();
                counters[id]++;
            }
        }
    }

    private void indexHierarchicalUnilateralEntities(List<AbstractBlock> blocks) {
        //count valid entities & blocks per entity
        validEntities1 = 0;
        int[] counters = new int[noOfEntities];
        for (AbstractBlock block : blocks) {
            UnilateralHierarchicalBlock uniBlock = (UnilateralHierarchicalBlock) block;
            for (int id : uniBlock.entitySet) {
                if (counters[id] == 0) {
                    validEntities1++;
                }
                counters[id]++;
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
            UnilateralHierarchicalBlock uniBlock = (UnilateralHierarchicalBlock) block;
            for (int id : uniBlock.entitySet) {
                entityBlocks[id][counters[id]] = block.getBlockIndex();
                counters[id]++;
            }
        }
    }

    // O(n . m) // n + m
    // The second loop (for j) starts always from 0
    public boolean isRepeated(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;

        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) { // this loop starts from 0
                countRepeatedComparisonCheck++;
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break; // the next iteration, for i++, j starts from 0, insteas it should start from this value.
                }

                if (blocks1[i] == blocks2[j]) {
                    return blocks1[i] != blockIndex;
                }
            }
        }

        System.err.println("Error!!!! EntityIndexs");
        return false;
    }

    // So the linear time solution should be somthing like this:
    public boolean isRepeatedLinearTime(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;

        int i = 0;
        int j = 0;

        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                countRepeatedComparisonCheck++;
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                } else if (blocks1[i] < blocks2[j]) {
                    break;
                } else if (blocks1[i] == blocks2[j]) {
                    return blocks1[i] != blockIndex;
                }
            }
            i++;
        }
        return false;
    }

    // O(n . log m)
    // n is the length of the shortest array
    // m is the length of the longest array
    // if the size of m and n is "large" (~ to be defined how large ~) this solution is faster
    public boolean isRepeatedBinarySearch(int blockIndex, Comparison comparison) {
        // blocks1 is the shortest array, blocks2 the longest(/equals)
        int[] blocks1 = (entityBlocks[comparison.getEntityId1()].length < entityBlocks[comparison.getEntityId2() + datasetLimit].length) ? entityBlocks[comparison.getEntityId1()] : entityBlocks[comparison.getEntityId2() + datasetLimit];
        int[] blocks2 = (entityBlocks[comparison.getEntityId1()].length >= entityBlocks[comparison.getEntityId2() + datasetLimit].length) ? entityBlocks[comparison.getEntityId1()] : entityBlocks[comparison.getEntityId2() + datasetLimit];

        int latestPosition = -blocks2.length;
        int leastCommonBlockIndex = -1;

        while (leastCommonBlockIndex++ < blocks1.length) {
            int ret = Arrays.binarySearch(blocks2, blocks1[leastCommonBlockIndex]);
            if (ret >= 0) {
                break;
            } else if (ret <= (latestPosition)) {  // ret < 0 indicates the positions in which the element should be added (-1)
                //System.out.println("no more comparison needed");
                return false;
            }
        }
        return blocks1[leastCommonBlockIndex] != blockIndex;
    }

    public boolean isRepeatedBinarySubLinearTime(int blockIndex, Comparison comparison) {
        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];

        int noOfBlocks1 = -blocks1.length;
        int noOfBlocks2 = -blocks2.length;

        int i = 0;

        int position = 0;

        while (true) {
            countRepeatedComparisonCheck++;
            position = Arrays.binarySearch(blocks2, blocks1[i]);
            if (position >= 0) {
                return blocks1[i] != blockIndex;
            } else if (position <= noOfBlocks2) {
                return false;
            } else {
                position = Arrays.binarySearch(blocks1, blocks2[(-position - 1)]);
                if (position >= 0) {
                    return blocks1[position] != blockIndex;
                } else if (position <= noOfBlocks1) {
                    return false;
                } else {
                    i = (-position - 1);
                }
            }
        }
    }


//    public boolean isRepeatedBinarySubLinearTime(int blockIndex, Comparison comparison) {
//        int[] blocks1 = entityBlocks[comparison.getEntityId1()];
//        int[] blocks2 = entityBlocks[comparison.getEntityId2() + datasetLimit];
//
//        int noOfBlocks1 = blocks1.length;
//        int noOfBlocks2 = blocks2.length;
//
//        int b1 = 0;
//        int b2 = 1;
//
//        int i = 0;
//        int j = 0;
//
//        //int position = 0;
//
//        while (true) {
//            countRepeatedComparisonCheck++;
//            int position = Arrays.binarySearch(blocks2, blocks1[0]);
//            if (position >= 0) {
//                return blocks1[0] != blockIndex;
//            } else if (position <= -(blocks2.length)) {
//                return false;
//            } else {
//                int from = (-position - 1);
//                int to = blocks2.length;
//                blocks2 = Arrays.copyOfRange(blocks2, from, to);
//                position = Arrays.binarySearch(blocks1, blocks2[0]);
//                if (position >= 0) {
//                    return blocks1[position] != blockIndex;
//                } else if (position <= -(blocks1.length)) {
//                    return false;
//                } else {
//                    from = (-position - 1);
//                    to = blocks1.length;
//                    blocks1 = Arrays.copyOfRange(blocks1, from, to);
//                }
//            }
//        }
//    }

    private void setNoOfEntities(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            setNoOfBilateralEntities(blocks);
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            setNoOfUnilateralEntities(blocks);
        } else if (blocks.get(0) instanceof UnilateralHierarchicalBlock) {
            setNoOfHierarchicalUnilateralEntities(blocks);
        } else if (blocks.get(0) instanceof BilateralHierarchicalBlock) {
            setNoOfHierarchicalBilateralEntities(blocks);
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

    private void setNoOfHierarchicalBilateralEntities(List<AbstractBlock> blocks) {
        noOfEntities = Integer.MIN_VALUE;
        datasetLimit = Integer.MIN_VALUE;
        for (AbstractBlock block : blocks) {
            BilateralHierarchicalBlock bilBlock = (BilateralHierarchicalBlock) block;
            for (int id1 : bilBlock.getEntitySet1()) {
                if (noOfEntities < id1 + 1) {
                    noOfEntities = id1 + 1;
                }
            }

            for (int id2 : bilBlock.getEntitySet2()) {
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

    private void setNoOfHierarchicalUnilateralEntities(List<AbstractBlock> blocks) {
        noOfEntities = Integer.MIN_VALUE;
        datasetLimit = 0;
        for (AbstractBlock block : blocks) {
            UnilateralHierarchicalBlock bilBlock = (UnilateralHierarchicalBlock) block;
            for (int id : bilBlock.entitySet) {
                if (noOfEntities < id + 1) {
                    noOfEntities = id + 1;
                }
            }
        }
    }
}
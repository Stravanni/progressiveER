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
import java.util.List;

/**
 *
 * @author gap2
 */
public class FastEntityIndex implements Serializable {

    private static final long serialVersionUID = 13483254243447L;

    private boolean cleanCleanER;
    private int datasetLimit;
    private int noOfBlocks;
    private int noOfEntities;
    
    private double[] entityComparisons;
    private int[][] entityBlocks;
    
    private BilateralBlock[] bBlocks;
    private UnilateralBlock[] uBlocks;

    public FastEntityIndex(List<AbstractBlock> blocks) {
        if (blocks.isEmpty()) {
            System.err.println("Entity index received an empty block collection as input!");
            return;
        }

        if (blocks.get(0) instanceof DecomposedBlock) {
            System.err.println("The entity index is incompatible with a set of decomposed blocks!");
            System.err.println("Its functionalities can be carried out with same efficiency through a linear search of all comparisons!");
            return;
        }

        firstPass(blocks);
        if (cleanCleanER) {
            indexBilateralEntities();
        } else {
            indexUnilateralEntities();
        }
    }

    private void firstPass(List<AbstractBlock> blocks) {
        int counter = 0;
        noOfBlocks = blocks.size();
        cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        if (cleanCleanER) {
            noOfEntities = Integer.MIN_VALUE;
            datasetLimit = Integer.MIN_VALUE;
            bBlocks = new BilateralBlock[noOfBlocks];
            for (AbstractBlock block : blocks) {
                bBlocks[counter] = (BilateralBlock) block;
                for (int id1 : bBlocks[counter].getIndex1Entities()) {
                    if (noOfEntities < id1 + 1) {
                        noOfEntities = id1 + 1;
                    }
                }

                for (int id2 : bBlocks[counter].getIndex2Entities()) {
                    if (datasetLimit < id2 + 1) {
                        datasetLimit = id2 + 1;
                    }
                }
                counter++;
            }

            int temp = noOfEntities;
            noOfEntities += datasetLimit;
            datasetLimit = temp;
        } else {
            noOfEntities = Integer.MIN_VALUE;
            datasetLimit = 0;
            uBlocks = new UnilateralBlock[noOfBlocks];
            for (AbstractBlock block : blocks) {
                uBlocks[counter] = (UnilateralBlock) block;
                for (int id : uBlocks[counter].getEntities()) {
                    if (noOfEntities < id + 1) {
                        noOfEntities = id + 1;
                    }
                }
                counter++;
            }
        }
    }
    
    public BilateralBlock[] getBilateralBlocks() {
        return bBlocks;
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
    
    public double[] getEntityComparisons() {
        return entityComparisons;
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

    public UnilateralBlock[] getUnilateralBlocks() {
        return uBlocks;
    }
    
    public int[][] getWholeIndex() {
        return entityBlocks;
    }
    
    private void indexBilateralEntities() {
        int[] counters = new int[noOfEntities];
        entityComparisons = new double[noOfEntities];
        for (BilateralBlock block : bBlocks) {
            int innerSize1 = block.getIndex1Entities().length;
            int innerSize2 = block.getIndex2Entities().length;
            for (int id1 : block.getIndex1Entities()) {
                counters[id1]++;
                entityComparisons[id1] += innerSize2;
            }

            for (int id2 : block.getIndex2Entities()) {
                int entityId = datasetLimit + id2;
                counters[entityId]++;
                entityComparisons[entityId] += innerSize1;
            }
        }

        //initialize inverted index
        entityBlocks = new int[noOfEntities][];
        for (int i = 0; i < noOfEntities; i++) {
            entityBlocks[i] = new int[counters[i]];
            counters[i] = 0;
        }

        //build inverted index
        int counter = 0;
        for (BilateralBlock block : bBlocks) {
            for (int id1 : block.getIndex1Entities()) {
                entityBlocks[id1][counters[id1]] = counter;
                counters[id1]++;
            }

            for (int id2 : block.getIndex2Entities()) {
                int entityId = datasetLimit + id2;
                entityBlocks[entityId][counters[entityId]] = counter;
                counters[entityId]++;
            }
            counter++;
        }
    }

    private void indexUnilateralEntities() {
        //count valid entities & blocks per entity
        int[] counters = new int[noOfEntities];
        entityComparisons = new double[noOfEntities];
        for (UnilateralBlock block : uBlocks) {
            int blockSize = block.getEntities().length;
            for (int id : block.getEntities()) {
                counters[id]++;
                entityComparisons[id] += blockSize-1;
            }
        }

        //initialize inverted index
        entityBlocks = new int[noOfEntities][];
        for (int i = 0; i < noOfEntities; i++) {
            entityBlocks[i] = new int[counters[i]];
            counters[i] = 0;
        }

        //build inverted index
        int counter = 0;
        for (UnilateralBlock block : uBlocks) {
            for (int id : block.getEntities()) {
                entityBlocks[id][counters[id]] = counter;
                counters[id]++;
            }
            counter++;
        }
    }
    
    public boolean isCleanCleanER() {
        return cleanCleanER;
    }
}

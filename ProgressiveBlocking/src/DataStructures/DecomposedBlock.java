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

/**
 * @author gap2
 */

public class DecomposedBlock extends AbstractBlock {
    // A type of block that comprises blocks of minimum size in the form of 2
    // int[] for higher efficiency. Only comparisons between entities1 and entities2
    // and for the same index are allowed, i.e., entities1[i] is exclusively comparable 
    // with entities2[i]. No redundant comparisons should be present.
    private final boolean cleanCleanER;

    private int[] blockIndices;
    private final int[] entities1;
    private final int[] entities2;

    public DecomposedBlock(boolean ccER, int[] entities1, int[] entities2) {
        if (entities1.length != entities2.length) {
            System.err.println("\n\nCreating imbalanced decomposed block!!!!");
            System.err.println("Entities 1\t:\t" + entities1.length);
            System.err.println("Entities 2\t:\t" + entities2.length);
        }
        cleanCleanER = ccER;
        this.entities1 = entities1;
        this.entities2 = entities2;
        blockIndices = null;
    }

    public int[] getBlockIndices() {
        return blockIndices;
    }

    public int[] getEntities1() {
        return entities1;
    }

    public int[] getEntities2() {
        return entities2;
    }

    public boolean isCleanCleanER() {
        return cleanCleanER;
    }

    @Override
    public double getNoOfComparisons() {
        return entities1.length;
    }

    @Override
    public double getTotalBlockAssignments() {
        return 2 * entities1.length;
    }

    @Override
    public double getAggregateCardinality() {
        return entities1.length * entities2.length;
    }

    @Override
    public void setBlockIndex(int startingIndex) {
        blockIndex = startingIndex;
        blockIndices = new int[entities1.length];
        for (int i = 0; i < entities1.length; i++) {
            blockIndices[i] = startingIndex + i;
        }
    }

    @Override
    public void setUtilityMeasure() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
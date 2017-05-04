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
package ProgressiveSortedNeighborhood.DataStructures;

import java.io.Serializable;

/**
 * @author gap2
 */
public class SimplePositionIndex implements Serializable {

    private static final long serialVersionUID = 13483254243447435L;

    private final int noOfEntities;
    private final int[][] entityPositions;

    public SimplePositionIndex(int entities, int[] sortedEntities) {
        noOfEntities = entities;;

        int[] counters = new int[noOfEntities];
        for (int entityId : sortedEntities) {
            counters[entityId]++;
        }

        //initialize inverted index
        entityPositions = new int[noOfEntities][];
        for (int i = 0; i < noOfEntities; i++) {
            entityPositions[i] = new int[counters[i]];
            counters[i] = 0;
        }

        //build inverted index
        for (int i = 0; i < sortedEntities.length; i++) {
            int entityId = sortedEntities[i];
            entityPositions[entityId][counters[entityId]++] = i;
        }
    }

    public int[] getEntityPositions(int entityId) {
        return entityPositions[entityId];
    }
    
}

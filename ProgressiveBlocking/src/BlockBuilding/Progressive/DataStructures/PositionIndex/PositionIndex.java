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
package BlockBuilding.Progressive.DataStructures.PositionIndex;

import BlockBuilding.Progressive.DataStructures.WeightingSchemeSn;
import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;

import java.io.Serializable;
import java.util.List;

/**
 * @author gap2
 */
public class PositionIndex implements Serializable {

    private static final long serialVersionUID = 13483254243447435L;

    //-
    private final int noOfEntities;
    private static int[] windowIndex, counters;
    private final int[][] entityPositions;
    //--

    private MinHashIndex mhi;

    private WeightingSchemeSn weightingScheme;

    public PositionIndex(int entities, int[] sortedEntities, WeightingSchemeSn ws, List<EntityProfile>[] profiles) {
        this(entities, sortedEntities);
        this.weightingScheme = ws;
        if (weightingScheme == WeightingSchemeSn.MINHASH) {
            mhi = new MinHashIndex(profiles, 120);
            mhi.buildIndex();
        } else {
            mhi = null;
            //counters_neighbor_cooccurrence = new int[noOfEntities];
        }
    }

    public PositionIndex(int entities, int[] sortedEntities) {
        noOfEntities = entities;
        weightingScheme = null;

        counters = new int[noOfEntities];
        for (int entityId : sortedEntities) {
            counters[entityId]++;
        }

        windowIndex = new int[sortedEntities.length];

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

        // reset
        for (int i = 0; i < noOfEntities; i++) {
            counters[i] = 0;
        }
    }

    public int[] getEntityPositions(int entityId) {
        return entityPositions[entityId];
    }

    /**
     * Checks if the comparison is repeated or not
     *
     * @param entity1
     * @param entity2
     * @return
     */
    public boolean isRepeatedComparison(int entity1, int entity2) {
        for (int p1 : entityPositions[entity1]) {
            for (int p2 : entityPositions[entity2]) {
                // p1 is the lower
                //-
                if (p1 < p2) {
                    int current_window_size = p2 - p1;
                    int min_window_btw_profiles = windowIndex[p1];
                    if (current_window_size <= min_window_btw_profiles) {
                        return true;
                    }
                }

                // p2 is the lower
                if (p2 < p1) {
                    int current_window_size = p1 - p2;
                    int min_window_btw_profiles = windowIndex[p2];
                    if (current_window_size <= min_window_btw_profiles) {
                        return true;
                    }
                }
                //--
            }
        }
        return false;
    }

    public void setWindowSizeMap(int position, int w) {
        windowIndex[position] = w;
    }

    /**
     * An empty method. Just to be compatible with PositionIndexExtended.
     *
     * @param a
     * @param b
     */
    public void addPosition(int a, int b) {
        //
    }

    public double getWeight(int entity1, int entity2, int position1, int position2) {
        switch (weightingScheme) {
            case MINHASH:
                return mhi.getApproximateSimilarity(entity1, entity2);
            case ACF:
                return counters[entity2];
            case NCF:
                double denominator = getEntityPositions(entity1).length + getEntityPositions(entity2).length - counters[entity2];
                return counters[entity2] / denominator;
            default:
                return counters[entity2];
        }
    }

    public int[] getCounters() {
        return counters;
    }

    public void cearCounters() {
        counters = new int[noOfEntities];
    }
}
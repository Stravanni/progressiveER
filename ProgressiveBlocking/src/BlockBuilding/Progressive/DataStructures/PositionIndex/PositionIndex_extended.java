package BlockBuilding.Progressive.DataStructures.PositionIndex;

import BlockBuilding.Progressive.DataStructures.WeightingSchemeSn;
import BlockBuilding.Progressive.SortedNeighborhood.ProgressiveSnBuilder;
import DataStructures.Attribute;
import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;
import Utilities.ProfileComparison;

import java.io.Serializable;
import java.util.*;

/**
 * @author giovanni
 */
public class PositionIndex_extended implements Serializable {

    private static final long serialVersionUID = 13483254243447435L;

    private final int noOfEntities;
    private final List<EntityProfile>[] profiles;
    private final int datasetLimits;
    private final boolean cleanCleanER;
    private MinHashIndex mhi;

    private ProgressiveSnBuilder snb;

    public LinkedHashSet<Integer>[] entityPositions; // position list of each entity
    public final int[] windowIndex; // size of the window for a position (it can be that the pairs inserted in the heap have different distances)

    private final WeightingSchemeSn weightingScheme;

    public PositionIndex_extended(int entities, List<EntityProfile>[] profiles, WeightingSchemeSn wScheme) {
        noOfEntities = entities;
        weightingScheme = wScheme;
        this.profiles = profiles;
        datasetLimits = profiles[0].size();
        cleanCleanER = (profiles.length == 2) ? true : false;

        entityPositions = new LinkedHashSet[noOfEntities];
        windowIndex = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            entityPositions[i] = new LinkedHashSet<>();
            windowIndex[i] = 0;
        }

        if (weightingScheme == WeightingSchemeSn.MINHASH) {
            mhi = new MinHashIndex(profiles, 60);
            mhi.buildIndex();
        } else {
            mhi = null;
        }
    }

    public PositionIndex_extended(int entities, ProgressiveSnBuilder snb, WeightingSchemeSn wScheme) {
        noOfEntities = entities;
        weightingScheme = wScheme;
        this.profiles = snb.getProfileList();
        this.snb = snb;
        datasetLimits = profiles[0].size();
        cleanCleanER = (profiles.length == 2) ? true : false;

        entityPositions = new LinkedHashSet[noOfEntities];
        windowIndex = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            entityPositions[i] = new LinkedHashSet<>();
            windowIndex[i] = 0;
        }

        if (weightingScheme == WeightingSchemeSn.MINHASH) {
            mhi = new MinHashIndex(profiles, 60);
            mhi.buildIndex();
        } else {
            mhi = null;
        }
    }

    public void setWindowSizeMap(int position, int w) {
        windowIndex[position] = w;
    }

    public void addPosition(int entityId, int visitedPosition) {
        entityPositions[entityId].add(visitedPosition);
    }

    public double getWeight(int entity1, int entity2, int position1, int position2) {
        switch (weightingScheme) {
            /*case JS:
                Set<Attribute> attr1 = profiles[0].get(entity1).getAttributes();
                //Set<Attribute> attr2 = (!cleanCleanER) ? profiles[0].get(entity2).getAttributes() : profiles[1].get(entity2).getAttributes();
                Set<Attribute> attr2 = (!cleanCleanER) ? profiles[0].get(entity2).getAttributes() : profiles[1].get(entity2 - profiles[0].size()).getAttributes();
                return ProfileComparison.getJaccardSimilarity(attr1, attr2);*/
            case MINHASH:
                //return mhi.getApproximateSimilarity(entity1, entity2 + profiles[0].size());
                return mhi.getApproximateSimilarity(entity1, entity2);
        }

        //return -1;
        return position2 - position1;
    }


    public boolean isRepeatedComparison(int entity1, int entity2) {
        for (int p1 : entityPositions[entity1]) {
            for (int p2 : entityPositions[entity2]) {
                // p1 is the lower
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
            }
        }
        return false;
    }
}
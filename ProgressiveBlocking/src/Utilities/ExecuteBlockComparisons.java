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

package Utilities;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */
public class ExecuteBlockComparisons {
 
    private final EntityProfile[] dataset1;
    private final EntityProfile[] dataset2;
    
    public ExecuteBlockComparisons(String[] profilesPath) {
        dataset1 = loadProfiles(profilesPath[0]);
        System.out.println("Entities 1\t:\t" + dataset1.length);
        if (profilesPath.length == 2) {
            dataset2 = loadProfiles(profilesPath[1]);
            System.out.println("Entities 2\t:\t" + dataset2.length);
        } else {
            dataset2 = null;
        }
    }
    
    public ExecuteBlockComparisons(List<EntityProfile>[] profiles) {
        dataset1 = profiles[0].toArray(new EntityProfile[profiles[0].size()]);
        System.out.println("Entities 1\t:\t" + dataset1.length);
        if (profiles.length == 2) {
            dataset2 = profiles[1].toArray(new EntityProfile[profiles[1].size()]);
            System.out.println("Entities 2\t:\t" + dataset2.length);
        } else {
            dataset2 = null;
        }
    }
    
    public long comparisonExecution(List<AbstractBlock> blocks) {
        long startingTime = System.currentTimeMillis();
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (dataset2 != null) {
                    ProfileComparison.getJaccardSimilarity(dataset1[comparison.getEntityId1()].getAttributes(), 
                                                           dataset2[comparison.getEntityId2()].getAttributes());
                } else {
                    ProfileComparison.getJaccardSimilarity(dataset1[comparison.getEntityId1()].getAttributes(), 
                                                           dataset1[comparison.getEntityId2()].getAttributes());
                }
            }
        }
        long endingTime = System.currentTimeMillis();
        return endingTime-startingTime;
    }
    
    private EntityProfile[] loadProfiles(String profilesPath) {
        List<EntityProfile> entityProfiles = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(profilesPath);
        return entityProfiles.toArray(new EntityProfile[entityProfiles.size()]);
    }
}
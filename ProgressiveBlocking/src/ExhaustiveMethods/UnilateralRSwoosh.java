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

package ExhaustiveMethods;

import DataStructures.IdDuplicates;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class UnilateralRSwoosh {
    
    private double comparisons;
    private final Set<IdDuplicates> groundTruth;
    private final Set<String>[] entityProfiles;
    
    public UnilateralRSwoosh(Set<IdDuplicates> gt) {
        comparisons = 0;
        groundTruth = gt;
        entityProfiles = null;
    }
    
    public UnilateralRSwoosh(Set<IdDuplicates> gt, Set<String>[] profiles) {
        comparisons = 0;
        groundTruth = gt;
        entityProfiles = profiles;
    }
    
    public Set<HashSet<Integer>> applyProcessing(Set<HashSet<Integer>> entities) {
        final Set<HashSet<Integer>> cleanList = new HashSet<HashSet<Integer>>();      
        final Set<HashSet<Integer>> dirtyList = new HashSet<HashSet<Integer>>();
        dirtyList.addAll(entities);
        while (!dirtyList.isEmpty()) {
            Iterator iterator = dirtyList.iterator();
            HashSet<Integer> currentEntity = (HashSet<Integer>) iterator.next();
            iterator.remove();
            
            Set<Integer> matchingEntity = getMatchingEntity(currentEntity, cleanList);
            if (matchingEntity != null) {
                currentEntity.addAll(matchingEntity);
                cleanList.remove(matchingEntity);
                dirtyList.add(currentEntity);
            } else {
                cleanList.add(currentEntity);
            }
        }
        
        return cleanList;
    }
    
    public Set<HashSet<Integer>> applyPartialProcessing(Set<HashSet<Integer>> cleanEntities, Set<HashSet<Integer>> updatedEntities) {
        final Set<HashSet<Integer>> cleanList = new HashSet<HashSet<Integer>>();      
        cleanList.addAll(cleanEntities);
        
        final Set<HashSet<Integer>> dirtyList = new HashSet<HashSet<Integer>>();
        dirtyList.addAll(updatedEntities);
        
        while (!dirtyList.isEmpty()) {
            Iterator iterator = dirtyList.iterator();
            HashSet<Integer> currentEntity = (HashSet<Integer>) iterator.next();
            iterator.remove();
            
            Set<Integer> matchingEntity = getMatchingEntity(currentEntity, cleanList);
            if (matchingEntity != null) {
                currentEntity.addAll(matchingEntity);
                cleanList.remove(matchingEntity);
                dirtyList.add(currentEntity);
            } else {
                cleanList.add(currentEntity);
            }
        }
        
        return cleanList;
    }
    
    //Jaccard similarity co-efficient
    private double compareProfiles (Set<Integer> entity1, Set<Integer> entity2) {
        comparisons++;
        if (entityProfiles == null) {
            return 0;
        }
        
        final Set<String> totalProfile1 = new HashSet<String>(100*entity1.size());
        for (Integer eId : entity1) {
            totalProfile1.addAll(entityProfiles[eId]);
        }
        
        
        final Set<String> totalProfile2 = new HashSet<String>(100*entity2.size());
        for (Integer eId : entity2) {
            totalProfile2.addAll(entityProfiles[eId]);
        }
        double size2 = totalProfile2.size();
        totalProfile2.retainAll(totalProfile1);
        
        return totalProfile2.size()/(totalProfile1.size()+size2-totalProfile2.size());
    }
    
    public double getComparisons() {
        return comparisons;
    }
    
    private Set<Integer> getMatchingEntity(Set<Integer> entity, Set<HashSet<Integer>> cleanList) {       
        for (Set<Integer> cleanEntity : cleanList) {
            compareProfiles(entity, cleanEntity);
            for (Integer entityId1 : entity) {
                for (Integer entityId2 : cleanEntity) {
                    if (entityId1.equals(entityId2)) {
                        return cleanEntity;
                    }

                    final IdDuplicates duplicatePair1 = new IdDuplicates(entityId1, entityId2);
                    final IdDuplicates duplicatePair2 = new IdDuplicates(entityId2, entityId1);
                    if (groundTruth.contains(duplicatePair1) ||  groundTruth.contains(duplicatePair2)) {
                        return cleanEntity;
                    } 
                }
            }
        }
        
        return null;
    }
}
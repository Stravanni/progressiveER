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
package BlockBuilding.MemoryBased.SchemaBased;

import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.AbstractProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author gap2
 */
public abstract class AbstractCanopies extends CharacterNGrams {

    protected int[][] profiles1;
    protected int[][] profiles2;

    public AbstractCanopies(int n, int[] bkeys, ProfileType pType, String description, List<EntityProfile>[] profiles) {
        super(n, bkeys, pType, description, profiles);

        noOfEntities[0] = entityProfiles[0].size();
        
        final Map<String, Integer> distinctNGrams = new HashMap<>();
        updateDistinctNGrams(entityProfiles[0], distinctNGrams);
        
        if (profiles.length == 2) {
            noOfEntities[1] = entityProfiles[1].size();
            updateDistinctNGrams(entityProfiles[1], distinctNGrams);
        }

        profiles1 = buildProfiles(entityProfiles[0], distinctNGrams);
        profiles2 = null;
        if (profiles.length == 2) {
            profiles2 = buildProfiles(entityProfiles[1], distinctNGrams);
        }
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        if (profiles2 != null) {
            return getBilateralBlocks();
        }
        return getUnilateralBlocks();
    }

    private int[][] buildProfiles(List<EntityProfile> profiles, Map<String, Integer> distinctTokens) {
        int index = 0;
        final int[][] integerProfiles = new int[profiles.size()][];
        for (EntityProfile profile : profiles) {
            integerProfiles[index++] = getIntegerProfiles(profile, distinctTokens);
        }
        return integerProfiles;
    }

    protected abstract List<AbstractBlock> getBilateralBlocks();

    protected abstract List<AbstractBlock> getUnilateralBlocks();

    private int[] getIntegerProfiles(EntityProfile profile, Map<String, Integer> distinctTokens) {
        final Set<Integer> integers = new HashSet<>();
        AbstractProfile aProfile = getAbstractProfile(profile);
        for (int bKey : blockingKeys) {
            for (String nGramKey : getBlockingKeys(bKey, aProfile)) {
                integers.add(distinctTokens.get(nGramKey));
            }
        }

        final List<Integer> sortedIntegers = new ArrayList<>(integers);
        Collections.sort(sortedIntegers);
        return Converter.convertCollectionToArray(sortedIntegers);
    }

    private void updateDistinctNGrams(List<EntityProfile> profiles, Map<String, Integer> distinctTokens) {
        int index = distinctTokens.size();
        for (EntityProfile profile : profiles) {
            AbstractProfile aProfile = getAbstractProfile(profile);
            for (int bKey : blockingKeys) {
                for (String nGramKey : getBlockingKeys(bKey, aProfile)) {
                    if (!distinctTokens.containsKey(nGramKey)) {
                        distinctTokens.put(nGramKey, index);
                        index++;
                    }
                }
            }
        }
    }
}

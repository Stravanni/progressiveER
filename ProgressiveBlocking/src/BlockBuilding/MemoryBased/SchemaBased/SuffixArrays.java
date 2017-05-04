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
import DataStructures.SchemaBasedProfiles.AbstractProfile;
import BlockBuilding.Utilities;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import Utilities.Constants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */
public class SuffixArrays extends StandardBlocking implements Constants {

    protected final int maximumBlockSize;
    protected final int minimumSuffixLength;

    public SuffixArrays(int maxSize, int minLength, int[] bKeys, ProfileType pType, List<EntityProfile>[] profiles) {
        this(maxSize, minLength, bKeys, pType, "In-memory Suffix Array Schema-based Blocking", profiles);
    }

    public SuffixArrays(int maxSize, int minLength, int[] bKeys, ProfileType pType, String name, List<EntityProfile>[] profiles) {
        super(bKeys, pType, name, profiles);
        maximumBlockSize = maxSize;
        minimumSuffixLength = minLength;
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        blocks = super.buildBlocks();
        Utilities.purgeBlocksByAssignments(maximumBlockSize, blocks);
        return blocks;
    }

    @Override
    protected Set<String> getBlockingKeys(int keyId, AbstractProfile profile) {
        String currentKey = getOriginalBlockingKeys(keyId, profile);
        final Set<String> suffixes = new HashSet<>();
        suffixes.addAll(Utilities.getSuffixes(minimumSuffixLength, currentKey));
        return suffixes;
    }
}

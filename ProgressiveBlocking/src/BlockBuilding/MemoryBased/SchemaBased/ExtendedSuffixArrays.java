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
public class ExtendedSuffixArrays extends SuffixArrays implements Constants {

    public ExtendedSuffixArrays(int maxSize, int minLength, int[] bKeys, ProfileType pType, List<EntityProfile>[] profiles) {
        super(maxSize, minLength, bKeys, pType, "In-memory Extended Suffix Array Schema-based Blocking", profiles);
    }

    @Override
    protected Set<String> getBlockingKeys(int keyId, AbstractProfile profile) {
        String currentKey = getOriginalBlockingKeys(keyId, profile);
        final Set<String> extendedSuffixes = new HashSet<>();
        extendedSuffixes.addAll(Utilities.getExtendedSuffixes(minimumSuffixLength, currentKey));
        return extendedSuffixes;
    }
}

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
public class CharacterNGrams extends StandardBlocking implements Constants {

    protected final int nGramSize;

    public CharacterNGrams(int n, int[] bkeys, ProfileType pType, List<EntityProfile>[] profiles) {
        this(n, bkeys, pType, "In-memory Character N-Grams Schema-based Blocking", profiles);
    }

    public CharacterNGrams(int n, int[] bkeys, ProfileType pType, String description, List<EntityProfile>[] profiles) {
        super(bkeys, pType, description, profiles);
        nGramSize = n;
    }

    @Override
    protected Set<String> getBlockingKeys(int keyId, AbstractProfile profile) {
        String currentKey = getOriginalBlockingKeys(keyId, profile);
        final Set<String> nGrams = new HashSet<>();
        nGrams.addAll(Utilities.getNGrams(nGramSize, currentKey));
        return nGrams;
    }
}

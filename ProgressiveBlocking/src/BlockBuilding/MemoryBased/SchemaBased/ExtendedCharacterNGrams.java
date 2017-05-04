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
public class ExtendedCharacterNGrams extends CharacterNGrams implements Constants {

    private final double threshold;

    public ExtendedCharacterNGrams(double t, int n, int[] bkeys, ProfileType pType, List<EntityProfile>[] profiles) {
        super(n, bkeys, pType, "In-memory Extended Character N-Grams Schema-based Blocking", profiles);
        threshold = t;
    }

    @Override
    protected Set<String> getBlockingKeys(int keyId, AbstractProfile profile) {
        String currentKey = getOriginalBlockingKeys(keyId, profile);

        final Set<String> extendedQgrams = new HashSet<>();
        List<String> nGrams = Utilities.getNGrams(nGramSize, currentKey);
        if (nGrams.size() == 1) {
            extendedQgrams.add(nGrams.get(0));
        } else {
            if (MAX_Q_GRAMS < nGrams.size()) {
                nGrams = nGrams.subList(0, MAX_Q_GRAMS);
            }

            int minimumLength = (int) Math.max(1, Math.floor(nGrams.size() * threshold));
            for (int i = minimumLength; i <= nGrams.size(); i++) {
                extendedQgrams.addAll(Utilities.getCombinationsFor(nGrams, i));
            }
        }
        return extendedQgrams;
    }
}

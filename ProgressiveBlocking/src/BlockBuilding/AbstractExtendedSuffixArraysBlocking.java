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

package BlockBuilding;

import DataStructures.EntityProfile;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */
public abstract class AbstractExtendedSuffixArraysBlocking extends AbstractSuffixArraysBlocking {

    public AbstractExtendedSuffixArraysBlocking(int maxSize, int minLength, List<EntityProfile>[] profiles) {
        super(maxSize, minLength, "Memory-based Extended Suffix Array Blocking", profiles);
    }
    
    public AbstractExtendedSuffixArraysBlocking(int maxSize, int minLength, String[] entities, String[] index) {
        super(maxSize, minLength, "Disk-based Extended Suffix Array Blocking", entities, index);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(Utilities.getExtendedSuffixes(minimumSuffixLength, token));
        }
        return suffixes;
    }
}

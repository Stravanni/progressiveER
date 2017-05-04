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

public abstract class AbstractQGramsBlocking extends AbstractTokenBlocking {

    protected final int nGramSize;

    public AbstractQGramsBlocking(int n, List<EntityProfile>[] profiles) {
        this(n, "Memory-based Character N-Grams Blocking", profiles);
    }
    
    public AbstractQGramsBlocking(int n, String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
        nGramSize = n;
    }
    
    public AbstractQGramsBlocking(int n, String[] entities, String[] index) {
        this(n, "Disk-based Character N-Grams Blocking", entities, index);
    }
    
    public AbstractQGramsBlocking(int n, String description, String[] entities, String[] index) {
        super(description, entities, index);
        nGramSize = n;
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> nGrams = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            nGrams.addAll(Utilities.getNGrams(nGramSize, token));
        }

        return nGrams;
    }
}
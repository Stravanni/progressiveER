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
public abstract class AbstractExtendedQGrams extends AbstractQGramsBlocking {

    private final double threshold;
    private final static int MAX_Q_GRAMS = 15;

    public AbstractExtendedQGrams(double t, int n, List<EntityProfile>[] profiles) {
        super(n, "Memory-based Extended Character N-Grams Blocking", profiles);
        threshold = t;
    }
    
    public AbstractExtendedQGrams(double t, int n, String[] entities, String[] index) {
        super(n, "Disk-based Extended Character N-Grams Blocking", entities, index);
        threshold = t;
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> keys = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            List<String> nGrams = Utilities.getNGrams(nGramSize, token);
            if (nGrams.size() == 1) {
                keys.add(nGrams.get(0));
            } else {
                if (MAX_Q_GRAMS < nGrams.size()) {
                    nGrams = nGrams.subList(0, MAX_Q_GRAMS);
                }

                int minimumLength = (int) Math.max(1, Math.floor(nGrams.size() * threshold));
                for (int i = minimumLength; i <= nGrams.size(); i++) {
                    keys.addAll(Utilities.getCombinationsFor(nGrams, i));
                }
            }
        }
        return keys;
    }
}

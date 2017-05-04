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

import DataStructures.Attribute;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gap2
 */

public class ProfileComparison {

    public static Set<String> getDistinctTokens(Set<Attribute> nameValuePairs) {
        final Set<String> tokensFrequency = new HashSet<String>(5 * nameValuePairs.size());
        for (Attribute attribute : nameValuePairs) {
            String[] tokens = attribute.getValue().split("[\\W_]");
            tokensFrequency.addAll(Arrays.asList(tokens));
        }

        return tokensFrequency;
    }

    public static double getJaccardSimilarity(Set<Attribute> profile1, Set<Attribute> profile2) {
        final Set<String> tokenizedProfile1 = getDistinctTokens(profile1);
        final Set<String> tokenizedProfile2 = getDistinctTokens(profile2);

        final Set<String> allTokens = new HashSet<String>(tokenizedProfile1);
        allTokens.addAll(tokenizedProfile2);

        tokenizedProfile1.retainAll(tokenizedProfile2);
        return ((double) tokenizedProfile1.size()) / allTokens.size();
    }
}
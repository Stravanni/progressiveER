// Dirty ER
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

package BlockProcessing.ComparisonRefinement;

import DataStructures.Comparison;
import DataStructures.IdDuplicates;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class UnilateralDuplicatePropagation extends AbstractDuplicatePropagation {

    private Set<IdDuplicates> detectedDuplicates;

    public UnilateralDuplicatePropagation (Set<IdDuplicates> matches) {
        super(matches);
        detectedDuplicates = new HashSet<IdDuplicates>(2*matches.size());
    }

    public UnilateralDuplicatePropagation (String groundTruthPath) {
        super(groundTruthPath);
        detectedDuplicates = new HashSet<IdDuplicates>(2*duplicates.size());
    }

    @Override
    public int getNoOfDuplicates() {
        return detectedDuplicates.size();
    }

    @Override
    public boolean isSuperfluous(Comparison comparison) {
        final IdDuplicates duplicatePair1 = new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2());
        final IdDuplicates duplicatePair2 = new IdDuplicates(comparison.getEntityId2(), comparison.getEntityId1());
        if (detectedDuplicates.contains(duplicatePair1) ||
                detectedDuplicates.contains(duplicatePair2)) {
            return true;
        }

        if (duplicates.contains(duplicatePair1) ||
                duplicates.contains(duplicatePair2)) {
            if (comparison.getEntityId1() < comparison.getEntityId2()) {
                detectedDuplicates.add(new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2()));
            } else {
                detectedDuplicates.add(new IdDuplicates(comparison.getEntityId2(), comparison.getEntityId1()));
            }
        }

        return false;
    }

    @Override
    public void resetDuplicates() {
        detectedDuplicates = new HashSet<IdDuplicates>();
    }
}
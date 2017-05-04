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

package Comparators;

import DataStructures.Comparison;

import java.util.Comparator;

/**
 * @author G.A.P. II
 */

public class ComparisonWeightComparatorMax implements Comparator<Comparison> {

    @Override
    public int compare(Comparison o1, Comparison o2) {
        if (o1.getUtilityMeasure() == o2.getUtilityMeasure()) {
            return 0;
        } else {
            return (o2.getUtilityMeasure() - o1.getUtilityMeasure() > 0) ? 1 : -1;
        }
    }

}
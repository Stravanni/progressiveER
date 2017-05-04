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

package BlockBuilding.Progressive.DataStructures;

import DataStructures.Comparison;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class ComparisonUtilityComparator implements Comparator<Comparison> {

    @Override
    public int compare(Comparison o1, Comparison o2) {
        double test = o1.getUtilityMeasure()-o2.getUtilityMeasure(); 
        if (0 < test) {
            return -1;
        }

        if (test < 0) {
            return 1;
        }

        return 0;
    }
    
    
    public static void main (String[] args) {
        List<Comparison> comparisons = new ArrayList<>();
        
        Comparison comparison = new Comparison(true, 1, 2);
        comparison.setUtilityMeasure(0.5);
        comparisons.add(comparison);
        
        Comparison comparison1 = new Comparison(true, 2, 3);
        comparison1.setUtilityMeasure(0.25);
        comparisons.add(comparison1);
        
        Comparison comparison2 = new Comparison(true, 3, 4);
        comparison2.setUtilityMeasure(0.75);
        comparisons.add(comparison2);
        
        Collections.sort(comparisons);
        
        for (Comparison cmp : comparisons) {
            System.out.println(cmp.getEntityId1() + "\t\t" + cmp.getEntityId2() + "\t\t" + cmp.getUtilityMeasure());
        }
    }
}

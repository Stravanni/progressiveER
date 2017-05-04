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

import java.util.Collection;

/**
 *
 * @author gap2
 */
public class Converter {

    public static int[] convertCollectionToArray(Collection<Integer> ids) {
        int index = 0;
        int[] array = new int[ids.size()];
        for (Integer id : ids) {
            array[index++] = id;
        }

        return array;
    }

    public static int getSortedListsOverlap(int[] list1, int[] list2) {
        if (list1 == null || list2 == null) {
            return 0;
        }
        int commonEntities = 0;
        for (int i = 0; i < list1.length; i++) {
            for (int j = 0; j < list2.length; j++) {
                if (list2[j] < list1[i]) {
                    continue;
                }
                if (list1[i] < list2[j]) {
                    break;
                }
                commonEntities++;
            }
        }
        return commonEntities;
    }

    public static int getSortedListsOverlap(Integer[] list1, Integer[] list2) {
        if (list1 == null || list2 == null) {
            return 0;
        }
        int commonEntities = 0;
        for (Integer id1 : list1) {
            for (Integer id2 : list2) {
                if (id2 < id1) {
                    continue;
                }
                if (id1 < id2) {
                    break;
                }
                commonEntities++;
            }
        }
        return commonEntities;
    }
}

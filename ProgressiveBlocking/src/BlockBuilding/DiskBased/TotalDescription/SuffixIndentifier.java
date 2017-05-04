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

package BlockBuilding.DiskBased.TotalDescription;

import DataStructures.Uri;

/**
 * created on 11.12.2009
 * by gap2
 */

public class SuffixIndentifier {

    static public String getPossibleSuffix(Uri uri) {
        String possibleSuffix = "";
        String[] tokens = uri.getNormalForm().getInfix().split("[#/]");
        if (1 < tokens.length) {
            possibleSuffix = tokens[tokens.length - 1];
        }

        return possibleSuffix;
    }
}
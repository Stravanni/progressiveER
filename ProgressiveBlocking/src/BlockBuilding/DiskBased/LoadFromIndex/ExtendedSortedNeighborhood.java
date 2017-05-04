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
package BlockBuilding.DiskBased.LoadFromIndex;

import BlockBuilding.DiskBased.ExtendedSortedNeighborhoodBlocking;
import DataStructures.AbstractBlock;
import Utilities.Constants;
import java.util.List;

/**
 *
 * @author gap2
 */
public class ExtendedSortedNeighborhood extends ExtendedSortedNeighborhoodBlocking implements Constants {

    protected final boolean cleanCleanER;

    public ExtendedSortedNeighborhood(boolean ccer, int w, String[] index) {
        super(w, null, index);
        cleanCleanER = ccer;
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        setDirectory();
        if (cleanCleanER) { // Clean-Clean ER
            parseIndices();
        } else { //Dirty ER
            parseIndex();
        }

        return blocks;
    }
}

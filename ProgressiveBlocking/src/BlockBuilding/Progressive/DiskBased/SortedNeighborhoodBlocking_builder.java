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

package BlockBuilding.Progressive.DiskBased;

import BlockBuilding.Progressive.AbstractSortedNeighborhoodBlocking_builder;

/**
 * @author gap2
 */
public class SortedNeighborhoodBlocking_builder extends AbstractSortedNeighborhoodBlocking_builder {

    public SortedNeighborhoodBlocking_builder(String[] entities, String[] index) {
        super(entities, index);
    }

    @Override
    protected void setDirectory() {
        setDiskDirectory();
    }
    //protected void setDirectory() {setMemoryDirectory();}
}

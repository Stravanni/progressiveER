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

package BlockBuilding.MemoryBased;

import BlockBuilding.AbstractExtendedCanopyClustering;
import DataStructures.EntityProfile;
import java.util.List;

/**
 *
 * @author gap2
 */

public class ExtendedCanopyClustering extends AbstractExtendedCanopyClustering {

    public ExtendedCanopyClustering(int n1, int n2, int n, List<EntityProfile>[] profiles) {
        super(n1, n2, n, profiles);
    }
    
    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}

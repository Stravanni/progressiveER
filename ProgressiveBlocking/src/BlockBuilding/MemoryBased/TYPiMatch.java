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

import BlockBuilding.AbstractTYPiMatch;
import DataStructures.EntityProfile;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class TYPiMatch extends AbstractTYPiMatch {

    public TYPiMatch(double ep, double th, Set<String> sWords, List<EntityProfile>[] profiles) {
        super(ep, th, sWords, profiles);
    }
    
    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}

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

package BlockBuilding.MemoryBased.SchemaBased;

import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.AbstractProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */


public class StandardBlocking extends AbstractSchemaBasedMethod {

    public StandardBlocking(int[] bkeys, ProfileType pType, List<EntityProfile>[] profiles) {
        this(bkeys, pType, "In-memory Standard Blocking", profiles);
    }

    public StandardBlocking(int[] bkeys, ProfileType pType, String name, List<EntityProfile>[] profiles) {
        super(bkeys, pType, name, profiles);
    }

    @Override
    protected Set<String> getBlockingKeys(int keyId, AbstractProfile profile) {
        HashSet<String> keys = new HashSet<>();
        keys.add(getOriginalBlockingKeys(keyId, profile));
        return keys;
    }
    
    protected String getOriginalBlockingKeys(int keyId, AbstractProfile profile) {
        return profile.getBlockingKey(keyId);
    }
}

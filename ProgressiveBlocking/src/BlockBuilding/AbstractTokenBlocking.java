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

package BlockBuilding;

import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.AbstractProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gap2
 */
public abstract class AbstractTokenBlocking extends AbstractIndexBasedMethod {

    public AbstractTokenBlocking(int bKeys, ProfileType pType, String description, List<EntityProfile>[] profiles) {
        super(bKeys, pType, description, profiles);
    }

    public AbstractTokenBlocking(List<EntityProfile>[] profiles) {
        super("Memory-based Token Blocking", profiles);
    }

    public AbstractTokenBlocking(String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
    }

    public AbstractTokenBlocking(String[] entities, String[] index) {
        this("Disk-based Token Blocking", entities, index);
    }

    public AbstractTokenBlocking(String description, String[] entities, String[] index) {
        super(description, entities, index);
    }

    /********************************************
     * SCHEMA AGNOSTIC
     ********************************************/

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        return new HashSet<>(Arrays.asList(getTokens(attributeValue)));
    }

    protected String[] getTokens(String attributeValue) {
        /*return new String[]{attributeValue};*/
        return attributeValue.split("[\\W_]");
    }

    /********************************************
     * SCHEMA BASED
     ********************************************/

    @Override
    protected Set<String> getBlockingKeysMixed(int keyId, AbstractProfile profile) {
        HashSet<String> keys = new HashSet<>();
        for (String k : getOriginalBlockingKeys(keyId, profile)) {
            keys.add(k);
        }
        return keys;
    }

    protected String[] getOriginalBlockingKeys(int keyId, AbstractProfile profile) {
        return profile.getBlockingKeys(keyId);
    }
}
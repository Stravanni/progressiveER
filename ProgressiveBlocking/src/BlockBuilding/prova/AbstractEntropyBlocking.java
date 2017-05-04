
/*
#PROVA
* TODO IDEA QUA E':
* TODO che record della stessa entita' dovrebbero avere simile entropia
* TODO non sara' tanto uguale, quindi la metto come nel Sorted Neighbor
* */
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

package BlockBuilding.prova;

import BlockBuilding.AbstractIndexBasedMethod;
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
public abstract class AbstractEntropyBlocking extends AbstractEntropyIndexBasedMethod {

    public AbstractEntropyBlocking(int bKeys, ProfileType pType, String description, List<EntityProfile>[] profiles) {
        super(bKeys, pType, description, profiles);
    }

    public AbstractEntropyBlocking(List<EntityProfile>[] profiles) {
        super("Memory-based Token Blocking", profiles);
    }

    public AbstractEntropyBlocking(String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
    }

    public AbstractEntropyBlocking(String[] entities, String[] index) {
        this("Disk-based Token Blocking", entities, index);
    }

    public AbstractEntropyBlocking(String description, String[] entities, String[] index) {
        super(description, entities, index);
    }

    /********************************************
     * SCHEMA AGNOSTIC
     ********************************************/

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        /*
        TODO qua devo leggere tutto il record (cambiare AbstractIndexBasedMethod che chiama su un attributo alla volta)
        TODO qua devo anche passare le probabilita' dei token, per stimare l'entropia
        * */
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
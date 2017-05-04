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

import BlockBuilding.AbstractIndexBasedMethod;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.AbstractProfile;
import DataStructures.SchemaBasedProfiles.CddbProfile;
import DataStructures.SchemaBasedProfiles.CensusProfile;
import DataStructures.SchemaBasedProfiles.CoraProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import DataStructures.SchemaBasedProfiles.RestaurantProfile;
import DataStructures.SchemaBasedProfiles.SyntheticProfile;
import Utilities.Constants;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

/**
 *
 * @author G.A.P. II
 */
public abstract class AbstractSchemaBasedMethod extends AbstractIndexBasedMethod implements Constants {

    protected int[] blockingKeys;
    protected ProfileType profileType;

    public AbstractSchemaBasedMethod(int[] bKeys, ProfileType pType, String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
        blockingKeys = bKeys;
        profileType = pType;
    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        System.out.println("Indexing " + entities.size() + " entities...");
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                AbstractProfile aProfile = getAbstractProfile(profile);
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter));
                for (int keyId : blockingKeys) {
                    for (String key : getBlockingKeys(keyId, aProfile)) {
                        if (0 < key.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, key.trim(), Field.Store.YES));
                        }
                    }
                }
                index.addDocument(doc);
                counter++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected AbstractProfile getAbstractProfile(EntityProfile profile) {
        switch (profileType) {
            case CDDB_PROFILE:
                return new CddbProfile(profile);
            case CENSUS_PROFILE:
                return new CensusProfile(profile);
            case CORA_PROFILE:
                return new CoraProfile(profile);
            case RESTAURANT_PROFILE:
                return new RestaurantProfile(profile);
            case SYNTHETIC_PROFILE:
                return new SyntheticProfile(profile);
            default:
                return null;
        }
    }

    protected abstract Set<String> getBlockingKeys(int keyId, AbstractProfile profile);

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}
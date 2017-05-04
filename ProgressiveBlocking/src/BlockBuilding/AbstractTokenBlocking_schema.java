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

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author giovnani
 */
public abstract class AbstractTokenBlocking_schema extends AbstractIndexBasedMethod {

    public AbstractTokenBlocking_schema(List<EntityProfile>[] profiles) {
        super("Memory-based Token Blocking", profiles);
    }

    public AbstractTokenBlocking_schema(String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
    }

    public AbstractTokenBlocking_schema(String[] entities, String[] index) {
        this("Disk-based Token Blocking", entities, index);
    }

    public AbstractTokenBlocking_schema(String description, String[] entities, String[] index) {
        super(description, entities, index);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        return new HashSet<>(Arrays.asList(getTokens(attributeValue)));
    }

    protected String[] getTokens(String attributeValue) {
        /*return new String[]{attributeValue};*/
        return attributeValue.split("[\\W_]");
    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                List<String> blockingKeys = getSchemaKey(profile);
                for (String key : blockingKeys) {
                    doc.add(new StringField(VALUE_LABEL, key.trim(), Field.Store.YES));
                    totalWords++;
                }
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected abstract List<String> getSchemaKey(EntityProfile profile);
}
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

import DataStructures.AbstractBlock;
import DataStructures.Attribute;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.*;
import Utilities.Constants;
import Utilities.ExportBlocks;
import Utilities.SerializationUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * @author gap2
 * @author giovanni
 */
public abstract class AbstractIndexBasedMethod extends AbstractBlockingMethod implements Constants {

    protected final boolean cleanCleanER;
    protected int sourceId;
    protected double[] noOfEntities;

    protected List<AbstractBlock> blocks;
    protected final String[] entitiesPath;
    protected final String[] indexPath;
    protected Directory[] indexDirectory;
    protected final List<EntityProfile>[] entityProfiles;

    protected int blockingKeys;
    protected ProfileType profileType;
    protected boolean schemaBased = false;

    protected int totalWords;

    public AbstractIndexBasedMethod(int bKeys, ProfileType pType, String description, List<EntityProfile>[] profiles) {
        this(description, profiles);
        blockingKeys = bKeys;
        profileType = pType;
        schemaBased = true;
    }

    public AbstractIndexBasedMethod(String description, List<EntityProfile>[] profiles) {
        super(description);
        totalWords = 0;
        entitiesPath = null;
        indexPath = null;
        blocks = new ArrayList<>();
        entityProfiles = profiles;
        noOfEntities = new double[entityProfiles.length];
        if (entityProfiles.length == 2) {
            cleanCleanER = true;
        } else {
            cleanCleanER = false;
        }
    }

    public AbstractIndexBasedMethod(String description, String[] entities, String[] index) {
        super(description);
        totalWords = 0;
        entitiesPath = entities;
        indexPath = index;
        blocks = new ArrayList<>();
        entityProfiles = new List[entitiesPath.length];
        noOfEntities = new double[entitiesPath.length];
        if (entitiesPath.length == 2) {
            cleanCleanER = true;
        } else {
            cleanCleanER = false;
        }
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        setDirectory();

        //create Lucene index on disk
        sourceId = 0; // used by Attribute Clustering, as well, that's why it's not an argument
        buildIndex();
        if (cleanCleanER) {
            sourceId = 1;
            buildIndex();
        }

        //extract blocks from Lucene index
        ExportBlocks exportBlocks = new ExportBlocks(indexDirectory);
        return exportBlocks.getBlocks();
    }

    protected void buildIndex() {
        final List<EntityProfile> profiles = getProfiles();
        final IndexWriter iWriter = openWriter(indexDirectory[sourceId]);
        indexEntities(iWriter, profiles);
        closeWriter(iWriter);
        noOfEntities[sourceId] = profiles.size();
    }

    protected void closeWriter(IndexWriter iWriter) {
        try {
            iWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected abstract Set<String> getBlockingKeys(String attributeValue);

    /*protected abstract Set<String> getBlockingKeys(int keyId, AbstractProfile profile);*/

    protected Set<String> getBlockingKeysMixed(int keyId, AbstractProfile profile) {
        return null;
    }


    public double getBruteForceComparisons() {
        if (noOfEntities.length == 1) {
            return noOfEntities[0] * (noOfEntities[0] - 1) / 2;
        }
        return noOfEntities[0] * noOfEntities[1];
    }

    public int getDatasetLimit() {
        if (noOfEntities.length == 1) {
            return -1;
        }
        return (int) noOfEntities[0];
    }

    protected List<EntityProfile> getProfiles() {
        if (entitiesPath != null) {
            entityProfiles[sourceId] = loadEntities(entitiesPath[sourceId]);
        }
        return entityProfiles[sourceId];
    }

    public List<EntityProfile>[] getProfileList() {
        return entityProfiles;
    }

    public double getTotalNoOfEntities() {
        if (noOfEntities.length == 1) {
            return noOfEntities[0];
        }
        return noOfEntities[0] + noOfEntities[1];
    }

    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));

                // schemaBased support only MixedKeys (i.e.)
                if (schemaBased) {
                    AbstractProfile aProfile = getAbstractProfile(profile);
                    /*for (int keyId : blockingKeys) {*/
                    for (String key : getBlockingKeysMixed(blockingKeys, aProfile)) {
                        if (0 < key.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, key.trim(), Field.Store.YES));
                        }
                    }
                    /*}*/
                } else {
                    for (Attribute attribute : profile.getAttributes()) {
                        getBlockingKeys(attribute.getValue()).stream().filter((key) -> (0 < key.trim().length())).forEach((key) -> {
                            doc.add(new StringField(VALUE_LABEL, key.trim(), Field.Store.YES));
                            totalWords++;
                        });
                    }
                }
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isClean() {
        return cleanCleanER;
    }

    protected List<EntityProfile> loadEntities(String entitiesPath) {
        return (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entitiesPath);
    }

    protected IndexWriter openWriter(Directory directory) {
        try {
            Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_40);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            return new IndexWriter(directory, config);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
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

    protected abstract void setDirectory();

    protected void setDiskDirectory() {
        indexDirectory = ExportBlocks.getDirectories(indexPath);
    }

    protected void setMemoryDirectory() {
        indexDirectory = new Directory[entityProfiles.length];
        for (int i = 0; i < entityProfiles.length; i++) {
            indexDirectory[i] = new RAMDirectory();
        }
    }
}

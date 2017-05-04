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

import BlockBuilding.Utilities;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import DataStructures.UnilateralBlock;
import Utilities.Constants;
import Utilities.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author gap2
 */
public class SortedNeighborhood extends StandardBlocking implements Constants {

    protected final int windowSize;

    public SortedNeighborhood(int w, int[] bKeys, ProfileType pType, List<EntityProfile>[] profiles) {
        this(w, bKeys, pType, "In-memory Sorted Neighborhood Schema-based Blocking", profiles);
    }

    public SortedNeighborhood(int w, int[] bKeys, ProfileType pType, String name, List<EntityProfile>[] profiles) {
        super(bKeys, pType, name, profiles);
        windowSize = w;
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        if (entitiesPath == null) {
            return null;
        }

        //create Lucene index
        setDirectory();
        int[] bKeys = blockingKeys;
        for (int keyId : bKeys) { // create a different index for each blocking key
            blockingKeys = new int[1];
            blockingKeys[0] = keyId;

            sourceId = 0;
            buildIndex();
            if (entitiesPath.length == 2) { // Clean-Clean ER
                sourceId = 1;
                buildIndex();
            }

            //extract blocks from Lucene index
            if (entitiesPath.length == 2) { // Clean-Clean ER
                parseIndices();
            } else { //Dirty ER
                parseIndex();
            }
        }
        return blocks;
    }

    protected void parseIndex() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getTermEntities(sortedTerms, d1Reader);

        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                entityIds.add(allEntityIds[i + j]);
            }

            if (1 < entityIds.size()) {
                int[] idsArray = Converter.convertCollectionToArray(entityIds);
                UnilateralBlock uBlock = new UnilateralBlock(idsArray);
                blocks.add(uBlock);
            }
        }

        Utilities.closeReader(d1Reader);
    }

    protected void parseIndices() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        blockingKeysSet.addAll(getTerms(d2Reader));
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getTermEntities(sortedTerms, d1Reader, d2Reader);

        int datasetLimit = d1Reader.numDocs();
        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds1 = new HashSet<>();
            final Set<Integer> entityIds2 = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                if (allEntityIds[i + j] < datasetLimit) {
                    entityIds1.add(allEntityIds[i + j]);
                } else {
                    entityIds2.add(allEntityIds[i + j] - datasetLimit);
                }
            }

            if (!entityIds1.isEmpty() && !entityIds2.isEmpty()) {
                int[] idsArray1 = Converter.convertCollectionToArray(entityIds1);
                int[] idsArray2 = Converter.convertCollectionToArray(entityIds2);
                BilateralBlock bBlock = new BilateralBlock(idsArray1, idsArray2);
                blocks.add(bBlock);
            }
        }

        Utilities.closeReader(d1Reader);
        Utilities.closeReader(d2Reader);
    }
    
    protected Set<String> getTerms(IndexReader iReader) {
        Set<String> sortedTerms = new HashSet<>();
        try {
            Fields fields = MultiFields.getFields(iReader);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    sortedTerms.add(text.utf8ToString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return sortedTerms;
    }
    
    protected List<Integer> getTermEntities(int[] docIds, IndexReader iReader, String blockingKey) {
        try {
            Term term = new Term(VALUE_LABEL, blockingKey);
            List<Integer> entityIds = new ArrayList<>();
            int docFrequency = iReader.docFreq(term);
            if (0 < docFrequency) {
                BytesRef text = term.bytes();
                DocsEnum de = MultiFields.getTermDocsEnum(iReader, MultiFields.getLiveDocs(iReader), VALUE_LABEL, text);
                int doc;
                while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                    entityIds.add(docIds[doc]);
                }
            }

            return entityIds;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    protected Integer[] getTermEntities(String[] sortedTerms, IndexReader iReader) {
        final List<Integer> sortedEntityIds = new ArrayList<>();

        int[] documentIds = Utilities.getDocumentIds(iReader);
        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = getTermEntities(documentIds, iReader, blockingKey);
            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
        }

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }
    
    protected Integer[] getTermEntities(String[] sortedTerms, IndexReader d1Reader, IndexReader d2Reader) {
        int datasetLimit = d1Reader.numDocs();
        final List<Integer> sortedEntityIds = new ArrayList<>();

        int[] documentIdsD1 = Utilities.getDocumentIds(d1Reader);
        int[] documentIdsD2 = Utilities.getDocumentIds(d2Reader);
        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = new ArrayList<>();
            sortedIds.addAll(getTermEntities(documentIdsD1, d1Reader, blockingKey));

            for (Integer entityId : getTermEntities(documentIdsD2, d2Reader, blockingKey)) {
                sortedIds.add(datasetLimit + entityId);
            }

            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
        }

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }
}

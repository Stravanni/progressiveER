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
import DataStructures.BilateralBlock;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import DataStructures.UnilateralBlock;
import Utilities.Constants;
import Utilities.Converter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

/**
 *
 * @author gap2
 */
public class ExtendedSortedNeighborhood extends SortedNeighborhood implements Constants {

    public ExtendedSortedNeighborhood(int w, int[] bKeys, ProfileType pType, List<EntityProfile>[] profiles) {
        super(w, bKeys, pType, "In-memory Extended Sorted Neighborhood Schema-based Blocking", profiles);
    }

    @Override
    protected void parseIndex() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        final Set<String> blockingKeysSet = getTerms(d1Reader);
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        //slide window over the sorted list of blocking keys
        int upperLimit = sortedTerms.length - windowSize;
        int[] documentIds = Utilities.getDocumentIds(d1Reader);
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                entityIds.addAll(getTermEntities(documentIds, d1Reader, sortedTerms[i + j]));
            }

            if (1 < entityIds.size()) {
                int[] idsArray = Converter.convertCollectionToArray(entityIds);
                UnilateralBlock uBlock = new UnilateralBlock(idsArray);
                blocks.add(uBlock);
            }
        }
    }

    @Override
    protected void parseIndices() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        blockingKeysSet.addAll(getTerms(d2Reader));
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        //slide window over the sorted list of blocking keys
        int upperLimit = sortedTerms.length - windowSize;
        int[] documentIdsD1 = Utilities.getDocumentIds(d1Reader);
        int[] documentIdsD2 = Utilities.getDocumentIds(d2Reader);
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds1 = new HashSet<>();
            final Set<Integer> entityIds2 = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                try {
                    int docFrequency = d1Reader.docFreq(new Term(VALUE_LABEL, sortedTerms[i + j]));
                    if (0 < docFrequency) {
                        entityIds1.addAll(getTermEntities(documentIdsD1, d1Reader, sortedTerms[i + j]));
                    }

                    docFrequency = d2Reader.docFreq(new Term(VALUE_LABEL, sortedTerms[i + j]));
                    if (0 < docFrequency) {
                        entityIds1.addAll(getTermEntities(documentIdsD2, d2Reader, sortedTerms[i + j]));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (!entityIds1.isEmpty() && !entityIds2.isEmpty()) {
                int[] idsArray1 = Converter.convertCollectionToArray(entityIds1);
                int[] idsArray2 = Converter.convertCollectionToArray(entityIds2);
                BilateralBlock bBlock = new BilateralBlock(idsArray1, idsArray2);
                blocks.add(bBlock);
            }
        }
    }
}

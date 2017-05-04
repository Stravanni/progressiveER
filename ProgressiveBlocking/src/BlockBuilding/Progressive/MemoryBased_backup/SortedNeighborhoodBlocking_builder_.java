/**
 * @author giovanni
 * <p>
 * This class builds the sorted list of pairs of profiles for psn;
 * it is basically a modified version of AbstractSortedNeighborhoodBlocking
 * (needed in AbstractProgressiveSortedNeighbor_heap)
 */
package BlockBuilding.Progressive.MemoryBased_backup;

import BlockBuilding.AbstractTokenBlocking;
import BlockBuilding.Utilities;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import Utilities.Constants;


import java.io.IOException;
import java.util.*;

public class SortedNeighborhoodBlocking_builder_ extends AbstractTokenBlocking implements Constants {

    protected int datasetLimit;
    protected int upperlimit;
    protected int numEntities;

    protected Integer[] sortedEntities;

    protected String[] indexKeys;

    public SortedNeighborhoodBlocking_builder_(List<EntityProfile>[] profiles) {
        this("Disk-based Sorted Neighborhood Blocking", profiles);
    }

    public SortedNeighborhoodBlocking_builder_(String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
        if (profiles.length == 1) {
            numEntities = profiles[0].size();
        } else {
            numEntities = profiles[0].size() + profiles[1].size();
        }
    }

    public SortedNeighborhoodBlocking_builder_(String[] entities, String[] index) {
        this("Disk-based Sorted Neighborhood Blocking", entities, index);
    }

    public SortedNeighborhoodBlocking_builder_(String description, String[] entities, String[] index) {
        super(description, entities, index);
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        setDirectory();

        //create Lucene index on disk
        sourceId = 0; // used by Attribute Clustering, as well, that's why it's not an argument
        buildIndex();
        if (cleanCleanER) { // Clean-Clean ER
            sourceId = 1;
            buildIndex();
        }

        //extract blocks from Lucene index
        if (cleanCleanER) { // Clean-Clean ER
            parseIndices();
        } else { //Dirty ER
            parseIndex();
        }

        return null;
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

    protected Integer[] getSortedEntities(String[] sortedTerms, IndexReader iReader) {
        final List<Integer> sortedEntityIds = new ArrayList<>();

        int[] documentIds = Utilities.getDocumentIds(iReader);

        LinkedList<String> indexKey = new LinkedList<>();

        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = getTermEntities(documentIds, iReader, blockingKey);
            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
            for (int i = 0; i < sortedIds.size(); i++) {
                indexKey.add(blockingKey);
            }
        }
        System.out.println("sorted terms: " + sortedEntityIds.size());

        indexKeys = indexKey.toArray(new String[indexKey.size()]);

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }

    protected Integer[] getSortedEntities(String[] sortedTerms, IndexReader d1Reader, IndexReader d2Reader) {
        final List<Integer> sortedEntityIds = new ArrayList<>();

        int[] documentIdsD1 = Utilities.getDocumentIds(d1Reader);
        int[] documentIdsD2 = Utilities.getDocumentIds(d2Reader);

        LinkedList<String> indexKey = new LinkedList<>();

        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = new ArrayList<>();
            sortedIds.addAll(getTermEntities(documentIdsD1, d1Reader, blockingKey));

            getTermEntities(documentIdsD2, d2Reader, blockingKey).stream().forEach((entityId) -> {
                sortedIds.add(datasetLimit + entityId);
            });

            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);

            for (int i = 0; i < sortedIds.size(); i++) {
                indexKey.add(blockingKey);
            }
        }

        indexKeys = indexKey.toArray(new String[indexKey.size()]);
        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
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

    protected void parseIndex() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[sourceId]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        datasetLimit = d1Reader.numDocs();
        upperlimit = datasetLimit - 1;
        noOfEntities = new double[1];
        noOfEntities[0] = d1Reader.numDocs();

        sortedEntities = getSortedEntities(sortedTerms, d1Reader);
        Utilities.closeReader(d1Reader);
    }

    protected void parseIndices() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        blockingKeysSet.addAll(getTerms(d2Reader));
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        datasetLimit = d1Reader.numDocs();
        upperlimit = datasetLimit - 1;
        noOfEntities[0] = d1Reader.numDocs();
        noOfEntities[1] = d2Reader.numDocs();

        sortedEntities = getSortedEntities(sortedTerms, d1Reader, d2Reader);

        Utilities.closeReader(d1Reader);
        Utilities.closeReader(d2Reader);
    }

    public String[] getIndexKey() {
        return indexKeys;
    }

    public String getIndexKey(int position) {
        return indexKeys[position];
    }

    public char getIndexKeyInitial(int position) {
        return indexKeys[position].charAt(0);
    }

    public Integer[] getSortedEntities() {
        return sortedEntities;
    }

    public int getDatasetLimit() {
        return datasetLimit;
    }

    public int getNumEntities() {
        return numEntities;
    }

    public boolean isClean() {
        return cleanCleanER;
    }
    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}

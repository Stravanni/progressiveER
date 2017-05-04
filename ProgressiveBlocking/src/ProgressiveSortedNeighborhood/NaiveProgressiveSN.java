package ProgressiveSortedNeighborhood;

import BlockBuilding.AbstractTokenBlocking;
import BlockBuilding.Utilities;
import Utilities.Constants;
import Utilities.Converter;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

/**
 * @author gap2
 */

public class NaiveProgressiveSN extends AbstractTokenBlocking implements Constants {

    protected int[] sortedEntityIds;
    
    public NaiveProgressiveSN(List<EntityProfile>[] profiles) {
        this("Memory-based Sorted Neighborhood Blocking", profiles);
    }

    public NaiveProgressiveSN(String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
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

        return blocks;
    }

    public int[] getEntityList() {
        return sortedEntityIds;
    }
    
    protected int[] getSortedEntities(String[] sortedTerms, IndexReader iReader) {
        final List<Integer> entityList = new ArrayList<>();

        final int[] documentIds = Utilities.getDocumentIds(iReader);
        for (String blockingKey : sortedTerms) {
            final List<Integer> sortedIds = getTermEntities(documentIds, iReader, blockingKey);
            Collections.shuffle(sortedIds);
            entityList.addAll(sortedIds);
        }
        
        return Converter.convertCollectionToArray(entityList);
    }

    protected int[] getSortedEntities(String[] sortedTerms, IndexReader d1Reader, IndexReader d2Reader) {
        final int datasetLimit = d1Reader.numDocs();
        final List<Integer> entityList = new ArrayList<>();

        final int[] documentIdsD1 = Utilities.getDocumentIds(d1Reader);
        final int[] documentIdsD2 = Utilities.getDocumentIds(d2Reader);
        for (String blockingKey : sortedTerms) {
            final List<Integer> sortedIds = new ArrayList<>();
            sortedIds.addAll(getTermEntities(documentIdsD1, d1Reader, blockingKey));

            getTermEntities(documentIdsD2, d2Reader, blockingKey).stream().forEach((entityId) -> {
                sortedIds.add(datasetLimit + entityId);
            });

            Collections.shuffle(sortedIds);
            entityList.addAll(sortedIds);
        }

        return Converter.convertCollectionToArray(entityList);
    }

    protected List<Integer> getTermEntities(int[] docIds, IndexReader iReader, String blockingKey) {
        try {
            final Term term = new Term(VALUE_LABEL, blockingKey);
            final List<Integer> entityIds = new ArrayList<>();
            final int docFrequency = iReader.docFreq(term);
            if (0 < docFrequency) {
                final BytesRef text = term.bytes();
                final DocsEnum de = MultiFields.getTermDocsEnum(iReader, MultiFields.getLiveDocs(iReader), VALUE_LABEL, text);
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
    
    protected Set<String> getTerms(IndexReader iReader) {
        final Set<String> sortedTerms = new HashSet<>();
        try {
            final Fields fields = MultiFields.getFields(iReader);
            for (String field : fields) {
                final Terms terms = fields.terms(field);
                final TermsEnum termsEnum = terms.iterator(null);
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
        final IndexReader d1Reader = Utilities.openReader(indexDirectory[sourceId]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        final String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        sortedEntityIds = getSortedEntities(sortedTerms, d1Reader);

        Utilities.closeReader(d1Reader);
    }

    protected void parseIndices() {
        final IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        final IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        blockingKeysSet.addAll(getTerms(d2Reader));
        
        final String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        sortedEntityIds = getSortedEntities(sortedTerms, d1Reader, d2Reader);

        Utilities.closeReader(d1Reader);
        Utilities.closeReader(d2Reader);
    }

    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}

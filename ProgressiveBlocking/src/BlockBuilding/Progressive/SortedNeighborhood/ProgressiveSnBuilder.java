package BlockBuilding.Progressive.SortedNeighborhood;

import BlockBuilding.AbstractTokenBlocking;
import BlockBuilding.Utilities;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
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
 * @author gap2
 * @author giovanni
 */

public class ProgressiveSnBuilder extends AbstractTokenBlocking implements Constants {

    protected int[] sortedEntityIds;
    protected int numEntities;
    protected int datasetLimit;

    public ProgressiveSnBuilder(int bKeys, ProfileType pType, List<EntityProfile>[] profiles) {
        super(bKeys, pType, "Memory-based Sorted Neighborhood Blocking with schema", profiles);
        if (profiles.length == 1) {
            numEntities = profiles[0].size();
        } else {
            numEntities = profiles[0].size() + profiles[1].size();
        }
    }

    public ProgressiveSnBuilder(List<EntityProfile>[] profiles) {
        this("Memory-based Sorted Neighborhood Blocking", profiles);
        if (profiles.length == 1) {
            numEntities = profiles[0].size();
        } else {
            numEntities = profiles[0].size() + profiles[1].size();
        }
    }

    public ProgressiveSnBuilder(String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
        if (profiles.length == 1) {
            numEntities = profiles[0].size();
        } else {
            numEntities = profiles[0].size() + profiles[1].size();
        }
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

        datasetLimit = d1Reader.numDocs();

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        final String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        sortedEntityIds = getSortedEntities(sortedTerms, d1Reader);

        Utilities.closeReader(d1Reader);
    }

    protected void parseIndices() {
        final IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        final IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        datasetLimit = d1Reader.numDocs();

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

    public int getNumEntities() {
        return numEntities;
    }

    public int[] getSortedEntities() {
        return sortedEntityIds;
    }

    public int getDatasetLimit() {
        return datasetLimit;
    }

    public boolean isClean() {
        return cleanCleanER;
    }
}

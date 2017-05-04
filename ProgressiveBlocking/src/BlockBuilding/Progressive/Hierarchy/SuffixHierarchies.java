package BlockBuilding.Progressive.Hierarchy;

import BlockBuilding.MemoryBased.SuffixArraysBlocking;
import BlockBuilding.Utilities;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.EntityIndex;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.ComparisonIterator;

import Utilities.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
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
 * @author G.A.P. II
 */
public class SuffixHierarchies extends SuffixArraysBlocking implements Iterator<Comparison> {

    public static final int MINIMUM_SUFFIX_LENGTH = 1;

    protected int blocksCounter;
    protected final AbstractBlock[] sortedBlocks;
    protected ComparisonIterator comparisonIterator;
    protected EntityIndex entityIndex;

    public SuffixHierarchies(List<EntityProfile>[] profiles) {
        super(Integer.MAX_VALUE, MINIMUM_SUFFIX_LENGTH, profiles);
        blocks = buildBlocks();

        blocksCounter = 0;
        sortedBlocks = blocks.toArray(new AbstractBlock[blocks.size()]);
        comparisonIterator = sortedBlocks[blocksCounter].getComparisonIterator();
        entityIndex = new EntityIndex(blocks);
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
        if (cleanCleanER) { // Clean-Clean ER
            parseIndices();
        } else { //Dirty ER
            parseIndex();
        }

        return blocks;
    }

    public void getOriginalComparisons() {
        System.out.println("Total blocks\t:\t" + blocks.size());

        double totalComparisons = 0;
        for (AbstractBlock aBlock : blocks) {
            totalComparisons += aBlock.getNoOfComparisons();
        }
        System.out.println("Original total comparisons\t:\t" + totalComparisons);

        Set<Comparison> distinctComparisons = new HashSet<Comparison>();
        for (AbstractBlock aBlock : blocks) {
            ComparisonIterator iterator = aBlock.getComparisonIterator();
            while (iterator.hasNext()) {
                distinctComparisons.add(iterator.next());
            }
        }
        System.out.println("Original distinct comparisons\t:\t" + distinctComparisons.size());
    }

    @Override
    public boolean hasNext() {
        return blocksCounter < sortedBlocks.length || comparisonIterator.hasNext();
    }

    protected Set<String> getSignatures(IndexReader iReader) {
        final Set<String> sortedTerms = new HashSet<>();
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

    @Override
    public Comparison next() {
        Comparison newComparison = null;
        while (true) {
            if (!comparisonIterator.hasNext()) {
                if (blocksCounter < sortedBlocks.length - 1) {
                    blocksCounter++;
                    comparisonIterator = sortedBlocks[blocksCounter].getComparisonIterator();
                } else {
                    break;
                }
            }
            newComparison = comparisonIterator.next();
            //if (!entityIndex.isRepeated(blocksCounter, newComparison)) {
            break;
            //}
        }
        return newComparison;
    }

    protected void parseIndex() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);

        final Set<String> signatures = getSignatures(d1Reader);
        String[] sortedSignatures = sortSignatures(signatures);

        int[] documentIds = Utilities.getDocumentIds(d1Reader);
        for (String key : sortedSignatures) {
            List<Integer> entityIds = getTermEntities(documentIds, d1Reader, key);

            int[] idsArray = Converter.convertCollectionToArray(entityIds);
            if (2 < idsArray.length) {
                UnilateralBlock block = new UnilateralBlock(idsArray);
                blocks.add(block);
            }
        }

        noOfEntities = new double[1];
        noOfEntities[0] = d1Reader.numDocs();

        Utilities.closeReader(d1Reader);
    }

    protected void parseIndices() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        final Set<String> signatures = getSignatures(d1Reader);
        signatures.retainAll(getSignatures(d2Reader));
        String[] sortedSignatures = sortSignatures(signatures);

        int[] documentIdsD1 = Utilities.getDocumentIds(d1Reader);
        int[] documentIdsD2 = Utilities.getDocumentIds(d2Reader);
        for (String key : sortedSignatures) {
            List<Integer> entityIdsD1 = getTermEntities(documentIdsD1, d1Reader, key);
            List<Integer> entityIdsD2 = getTermEntities(documentIdsD2, d2Reader, key);

            int[] idsArrayD1 = Converter.convertCollectionToArray(entityIdsD1);
            int[] idsArrayD2 = Converter.convertCollectionToArray(entityIdsD2);
            BilateralBlock block = new BilateralBlock(idsArrayD1, idsArrayD2);
            blocks.add(block);
        }

        noOfEntities = new double[2];
        noOfEntities[0] = d1Reader.numDocs();
        noOfEntities[1] = d2Reader.numDocs();

        Utilities.closeReader(d1Reader);
        Utilities.closeReader(d2Reader);
    }

    protected static String[] sortSignatures(Set<String> signatures) { // from longest to shortest; when having the same size, alphabetical order
        final String[] sortedSignatures = signatures.toArray(new String[signatures.size()]);
        Arrays.sort(sortedSignatures, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length() == o2.length()) {
                    o1.compareTo(o2);
                }
                return o2.length() - o1.length();
            }
        });
        return sortedSignatures;
    }
}

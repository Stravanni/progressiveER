package BlockBuilding.Progressive.Hierarchy;

import BlockBuilding.MemoryBased.SuffixArraysBlocking;
import BlockBuilding.Utilities;
import DataStructures.*;
import Utilities.ComparisonIterator;
import Utilities.Converter;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

/**
 * @author giovanni.simonini@unimore.it
 */
public class ProgressiveSuffixHierarchies extends SuffixArraysBlocking implements Iterator<Comparison> {

    protected static int MINIMUM_SUFFIX_LENGTH = 7;
    private static boolean STRICT_MINIMUM;// discard all the tokens with length < MINIMUM_SUFFIX_LENGTH (even single word)

    protected AbstractHierarchicalBlock[] sortedBlocks;
    protected Map<String, AbstractHierarchicalBlock> BlocksMapping; // key -> block
    public EntityIndex entityIndex;

    //protected String[] sortedSignatures;
    protected String[] sortedKeys;

    protected AbstractHierarchicalBlock currentBlock;
    protected int counterBacksIndex;

    //protected HashSet<Comparison> compared;
    protected HashSet<Integer> compared_blocks;

    public int counter_repeated;

    protected boolean removeRepeatedComparisons = false;

    public ProgressiveSuffixHierarchies(List<EntityProfile>[] profiles, boolean removeRepeated) {
        this(profiles);
        removeRepeatedComparisons = removeRepeated;
    }

    public ProgressiveSuffixHierarchies(List<EntityProfile>[] profiles, int min_suffix_len) {
        super(Integer.MAX_VALUE, min_suffix_len, profiles);
        MINIMUM_SUFFIX_LENGTH = min_suffix_len;
    }

    public ProgressiveSuffixHierarchies(List<EntityProfile>[] profiles, int min_suffix_len, boolean strict_minimum, boolean removeRepeated) {
        this(profiles);
        MINIMUM_SUFFIX_LENGTH = min_suffix_len;
        System.out.println("MINIMUM_SUFFIX_LENGTH: " + MINIMUM_SUFFIX_LENGTH);
        removeRepeatedComparisons = removeRepeated;
        STRICT_MINIMUM = strict_minimum;
    }

    public ProgressiveSuffixHierarchies(List<EntityProfile>[] profiles) {
        super(Integer.MAX_VALUE, MINIMUM_SUFFIX_LENGTH, profiles);
        BlocksMapping = new HashMap<>();
    }

    public void createHierarchy() {

        buildBlocksMapping();

        sortedBlocks = blocks.toArray(new AbstractHierarchicalBlock[BlocksMapping.size()]);
        //sortedKeys = sortSignatures(BlocksMapping.keySet());

        // Create the tree of blocks
        buildTree();

        for (int i = 0; i < sortedKeys.length; i++) {
            String key = sortedKeys[i];
            sortedBlocks[i] = BlocksMapping.get(key);
            /*System.out.println(sortedBlocks[i].getAggregateCardinality());*/
        }

        counterBacksIndex = 0;
        currentBlock = sortedBlocks[counterBacksIndex];
        while (currentBlock.getAggregateCardinality() == 0) {
            currentBlock = sortedBlocks[++counterBacksIndex];
        }

        compared_blocks = new HashSet<>();
        compared_blocks.add(currentBlock.getBlockIndex());

        entityIndex = new EntityIndex(Arrays.asList(sortedBlocks)); // needed to check for repeated comparisons

        for (AbstractHierarchicalBlock block : sortedBlocks) {
            block.processSubBlocks();
        }

        //compared = new HashSet<>();
        counter_repeated = 0;
    }

    protected void buildTree() {
        for (int i = 0; i < sortedKeys.length; i++) {
            String key = sortedKeys[i];
            if (key.length() > MINIMUM_SUFFIX_LENGTH + 1) {
                String parentKey = key.substring(1);
                AbstractHierarchicalBlock k = BlocksMapping.get(key);
                /*System.out.println("parentKey: " + parentKey);*/
                if (BlocksMapping.containsKey(parentKey))
                    BlocksMapping.get(parentKey).addChild(k);
            }
        }
    }

    public void buildBlocksMapping() {
        setDirectory();

        //create Lucene index on disk
        sourceId = 0; // used by Attribute Clustering, as well, that's why it's not an argument
        buildIndex();
        if (cleanCleanER) {
            System.out.println("Clean-clean ER");
            sourceId = 1;
            buildIndex();
        }

        //extract blocks from Lucene index
        if (cleanCleanER) { // Clean-Clean ER
            parseIndices();
        } else { //Dirty ER
            parseIndex();
        }
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
        //return currentBlock.hasNext();
        return true;
    }


    @Override
    public Comparison next() {
        /*System.out.println("currentBlock: " + currentBlock.getAggregateCardinality());*/
        while (true) {
            /*System.out.println("children:\t" + currentBlock.getChildren().size() + "\tbid: " + currentBlock.getBlockIndex());*/
            if (currentBlock.hasNext()) {
                int currentBlockIndex = currentBlock.getBlockIndex();
                Comparison next = currentBlock.next();
                while (!currentBlock.hasNext() && ++counterBacksIndex < sortedBlocks.length) {
                    currentBlock = sortedBlocks[counterBacksIndex];
                }
                //if (entityIndex.isRepeated(currentBlockIndex, next)) {
                //if (entityIndex.isRepeatedBinarySubLinearTime(currentBlockIndex, next)) {
                if (entityIndex.isRepeatedLinearTime(currentBlockIndex, next)) {
                    //if (entityIndex.isRepeatedBinarySearch(currentBlockIndex, next)) {
                    if (entityIndex.isRepeated(currentBlockIndex, next)) {
                        continue;
                    }
                }
                return next;
            } else {
                return null;
            }
        }
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

    protected void parseIndex() {
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);

        final Set<String> signatures = getSignatures(d1Reader);
        sortedKeys = sortSignatures(signatures);

        int blockIndex = 0;

        int[] documentIds = Utilities.getDocumentIds(d1Reader);
        for (String key : sortedKeys) {
            List<Integer> entityIds = getTermEntities(documentIds, d1Reader, key);

            int[] idsArray = Converter.convertCollectionToArray(entityIds);
            /*TODO here should be > 1*/
            if (0 < idsArray.length) {
                AbstractHierarchicalBlock block = new UnilateralHierarchicalBlock(idsArray);
                //block.setBlockIndex(blockIndex++);
                block.setKey(key);
                BlocksMapping.put(key, block);
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
        sortedKeys = sortSignatures(signatures);

        int[] documentIdsD1 = Utilities.getDocumentIds(d1Reader);
        int[] documentIdsD2 = Utilities.getDocumentIds(d2Reader);

        int blockIndex = 0;

        for (String key : sortedKeys) {
            List<Integer> entityIdsD1 = getTermEntities(documentIdsD1, d1Reader, key);
            List<Integer> entityIdsD2 = getTermEntities(documentIdsD2, d2Reader, key);

            int[] idsArrayD1 = Converter.convertCollectionToArray(entityIdsD1);
            int[] idsArrayD2 = Converter.convertCollectionToArray(entityIdsD2);
            BilateralHierarchicalBlock block = new BilateralHierarchicalBlock(idsArrayD1, idsArrayD2);
            //block.setBlockIndex(blockIndex++);
            BlocksMapping.put(key, block);
        }

        noOfEntities = new double[2];
        noOfEntities[0] = d1Reader.numDocs();
        noOfEntities[1] = d2Reader.numDocs();

        Utilities.closeReader(d1Reader);
        Utilities.closeReader(d2Reader);
    }

    /**
     * Sorts the signatures (blocking keys) from the longest to the shortest (secondary: alphabetically)
     *
     * @param signatures
     * @return a string array containing all the sorted signatures
     */
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

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            if (!token.equals("")) {
                suffixes.addAll(Utilities.getSuffixes(MINIMUM_SUFFIX_LENGTH, token, STRICT_MINIMUM));
            }
        }
        return suffixes;
    }
}
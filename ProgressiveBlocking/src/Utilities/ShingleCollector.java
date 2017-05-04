package Utilities;

import BlockBuilding.AbstractIndexBasedMethod;
import BlockBuilding.Utilities;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;
import DataStructures.UnilateralBlock;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

/**
 * @author Giovanni
 *
 * ###################################################################################
 * Collect all the shingles that appears in at least 2 entity profiles.
 * MinHashing usgin this set of shingles is comparable at Token Blocking
 * ###################################################################################
 *
 */
public class ShingleCollector extends AbstractIndexBasedMethod {

    private HashMap<String, Integer> shingleMap;
    private int shingleCounter;

    public ShingleCollector(List<EntityProfile>[] profiles) {
        super("Memory-based Token Blocking", profiles);
        shingleMap = new HashMap<>();
        shingleCounter = 0;
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        return new HashSet<>(Arrays.asList(getTokens(attributeValue)));
    }

    @Override
    protected void setDirectory() {
        setMemoryDirectory();
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


        IndexReader iReader = null;
        try {
            iReader = IndexReader.open(indexDirectory[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parseIndex(iReader);
        Utilities.closeReader(iReader);

        return null;
    }

    @Override
    protected void buildIndex() {
        List<EntityProfile> entityProfiles = getProfiles();
        IndexWriter iWriter = openWriter(indexDirectory[0]); // use only one index for building the shingles (if clean-clean)
        indexEntities(iWriter, entityProfiles);
        closeWriter(iWriter);
        noOfEntities[sourceId] = entityProfiles.size();
    }

    @Override
    protected void setMemoryDirectory() {
        indexDirectory = new Directory[1];
        indexDirectory[0] = new RAMDirectory();
    }

    protected void parseIndex(IndexReader index) {
        try {
            int[] documentIds = Utilities.getDocumentIds(index);
            Fields fields = MultiFields.getFields(index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    if (termsEnum.docFreq() < 2) {
                        continue;
                    }
                    //System.out.println(text.utf8ToString());
                    shingleMap.put(text.utf8ToString(), shingleCounter++);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public HashMap<String, Integer> getShingleMap() {
        return shingleMap;
    }

    protected String[] getTokens(String attributeValue) {
        return attributeValue.split("[\\W_]");
    }
}
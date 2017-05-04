package BlockBuilding.Progressive.Hierarchy;

import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.List;

/**
 * @author giovanni.simonini@unimore.it
 */
public class ProgressiveMinHashHierarchies extends ProgressivePrefixHierarchies {

    private MinHashIndex minHashIndex;
    private int datasetLimit;
    private int signature_size;
    private int nBands;
    private int nRows;
    private int[] intervals;


    public ProgressiveMinHashHierarchies(List<EntityProfile>[] profiles, boolean removeRepeated) {
        super(profiles, removeRepeated);
        /*signature_size = 2018;*/
        signature_size = 256;
        /*nBands = 128;*/
        nBands = 32;
        nRows = signature_size / nBands;
        minHashIndex = new MinHashIndex(profiles, signature_size);
        minHashIndex.buildIndex();
        System.out.println("mhIndex: " + minHashIndex.getSignatures().length);
        System.out.println("mhIndex: " + minHashIndex.getSignatures()[0].length);
        datasetLimit = 0;
        if (profiles.length > 1) {
            datasetLimit = profiles[0].size();
        }
        /*intervals = new int[]{1, 2, 4, 8, 16};*/
        intervals = new int[]{1, 2, 4, 8};
    }

    public ProgressiveMinHashHierarchies(List<EntityProfile>[] profiles, int sigSize, int bands, boolean removeRepeated) {
        super(profiles, removeRepeated);
        signature_size = sigSize;
        nBands = bands;
        nRows = signature_size / nBands;
        minHashIndex = new MinHashIndex(profiles, signature_size);
        minHashIndex.buildIndex();
        intervals = new int[]{1, 2, 4, 8, 16};
    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            System.out.println("source: " + sourceId);
            int counter = 0;
            for (int profileIndex = 0; profileIndex < entities.size(); profileIndex++) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));

                int pIndex = (sourceId == 1) ? datasetLimit : 0;
                pIndex += profileIndex;

                int[] signature = minHashIndex.getSignatures()[pIndex];
                int indexSignature = 0;
                int indexIntervals = 0;
                StringBuilder keyBuilder = new StringBuilder();
                for (int band = 0; band < nBands; band++) {
                    indexIntervals = 0;
                    keyBuilder.setLength(0);
                    keyBuilder.append(indexSignature++);
                    keyBuilder.append("#");
                    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
                        keyBuilder.append(signature[rowIndex + (band * nRows)]);
                        keyBuilder.append("-");
                        if ((rowIndex + 1) == intervals[indexIntervals]) {
                            indexIntervals++;
                            String key = keyBuilder.toString();
                            doc.add(new StringField(VALUE_LABEL, key, Field.Store.YES));
                            totalWords++;
                        }
                    }
                }
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void buildTree() {
        for (int i = 0; i < sortedKeys.length; i++) {
            String key = sortedKeys[i];
            String[] keyArray = key.split("-");
            StringBuilder keyParentBuilder = new StringBuilder();
            //if (keyArray.length > 2) {
            //for (int j = 0; j < (((keyArray.length - 1) / 2) + 1); j++) {
            if (keyArray.length > 2) {
                for (int j = 0; j < keyArray.length / 2; j++) {
                    keyParentBuilder.append(keyArray[j]);
                    keyParentBuilder.append("-");
                }
                BlocksMapping.get(keyParentBuilder.toString()).addChild(BlocksMapping.get(key)); // no need to check for the key
            }
        }
    }
}
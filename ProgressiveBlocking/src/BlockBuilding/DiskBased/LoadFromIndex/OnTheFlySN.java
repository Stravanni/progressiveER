package BlockBuilding.DiskBased.LoadFromIndex;

import BlockBuilding.Utilities;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.UnilateralBlock;
import Utilities.ComparisonIterator;
import Utilities.Converter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author G.A.P. II
 */
public class OnTheFlySN extends SortedNeighborhood {

    protected double totalComparisons;
    protected AbstractDuplicatePropagation dPropagation;
    
    public OnTheFlySN(boolean ccer, int w, String[] index, AbstractDuplicatePropagation adp) {
        super(ccer, w, index);
        dPropagation = adp;
    }
    
    public double[] getPerformance() {
        double[] metrics = new double[3];
        metrics[0] = dPropagation.getNoOfDuplicates()/((double)dPropagation.getExistingDuplicates()); //PC
        metrics[1] = dPropagation.getNoOfDuplicates()/totalComparisons; //PQ
        metrics[2] = totalComparisons;
        return metrics;
    }
    
    @Override
    protected void parseIndex() {
        dPropagation.resetDuplicates();
        IndexReader d1Reader = Utilities.openReader(indexDirectory[sourceId]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getSortedEntities(sortedTerms, d1Reader);

        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                entityIds.add(allEntityIds[i + j]);
            }

            if (1 < entityIds.size()) {
                int[] idsArray = Converter.convertCollectionToArray(entityIds);
                processBlock(new UnilateralBlock(idsArray));
            }
        }
        
        Utilities.closeReader(d1Reader);
        
        System.out.println("Total comparisons\t:\t" + totalComparisons);
        System.out.println("Detected duplicates\t:\t" + dPropagation.getNoOfDuplicates());
    }

    @Override
    protected void parseIndices() {
        dPropagation.resetDuplicates();
        
        IndexReader d1Reader = Utilities.openReader(indexDirectory[0]);
        IndexReader d2Reader = Utilities.openReader(indexDirectory[1]);

        final Set<String> blockingKeysSet = getTerms(d1Reader);
        blockingKeysSet.addAll(getTerms(d2Reader));
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getSortedEntities(sortedTerms, d1Reader, d2Reader);

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
                processBlock(new BilateralBlock(idsArray1, idsArray2));
            }
        }
        
        Utilities.closeReader(d1Reader);
        Utilities.closeReader(d2Reader);
        
        System.out.println("Total comparisons\t:\t" + totalComparisons);
        System.out.println("Detected duplicates\t:\t" + dPropagation.getNoOfDuplicates());
    }
    
    public void processBlock(AbstractBlock block) {
        ComparisonIterator iterator = block.getComparisonIterator();
        while (iterator.hasNext()) {
            totalComparisons++;
            Comparison comparison = iterator.next();
            dPropagation.isSuperfluous(comparison);
        }
    }
}
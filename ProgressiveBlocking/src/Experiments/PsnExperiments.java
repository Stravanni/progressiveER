package Experiments;

import BlockBuilding.Progressive.SortedNeighborhood.Hybrid.ProgressiveSnHeap;
import BlockBuilding.Progressive.DataStructures.WeightingSchemeSn;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.Comparison;
import DataStructures.EntityProfile;

import java.time.Instant;
import java.util.List;

/**
 * @author giovanni
 */

/*********************************************************************************************************************
 * Progressive Sorted Neighbor Experiments
 * *******************************************************************************************************************
 * <p>
 * <p>
 * psn sorts the profiles by their bk (as classic sn), then it starts to compare pairs of profiles taking into account:
 * (a) in simple psn, the distance in the sorted list: first pair with distance 1 (w=1), then w=2, .. w=max_w
 * (b) in weighted psn, the distance in the sorted list and the weight in the blocking graph
 * <p>
 * The candidate pairs of profiles are put in a heap data structure (MinMaxPriorityQueue of Guava)
 * (a) for simple psn the weight of the pair is the number of generation of the pair (with MinHeap)
 * (b) for weighted psn the weight is weight(p1,p2)/windows_size (with MaxHeap)
 * <p>
 * Give the sorted list of entities l[], when a pair (l[i],l[i+w]) is pulled out of the heap, the pair (l[i],l[i+w+1])
 * is inserted in the list, i.e., when p1 and p2 are compared, the next closer neighbor of p1 is inserted in the heap.
 * This does not takes into account the result of the comparison (i.e., if p1 and p2 are match); considering that
 * we could have a ~ look ahead (e.g. in (b) assigning a bonus weight, could be more general than classic look ahead).
 ********************************************************************************************************************/
public class PsnExperiments {

    private static int DATASET = 0;
    private static boolean CLEAN = false; // todo bug with weighted psn + dirtyER
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    public static void main(String[] args) {
        //String profilesFile = "E:\\Data\\profailes\\cddbProfiles";
        //String groundTruthFile = "E:\\Data\\groundtruth\\cddbIdDuplicates";
        List<EntityProfile>[] profiles;
        if (args.length > 0) {
            BASEPATH = args[0] + "/";
            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
        } else {
            //profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
            profiles = Utilities.getEntities(DATASET, CLEAN);
        }

        Instant start = Instant.now();

        ProgressiveSnHeap psn = new ProgressiveSnHeap(profiles, WeightingSchemeSn.MINHASH, false, 50);
        // weighted psn needs a block collection and a wighing scheme
        //psn.buildEntityList(50);
        // passing no argument simple psn is performed
        //psn.buildEntityList();

        //MinMaxPriorityQueue<Comparison> comparisons = psn.get_heap();

        double num_comparisons = 0;
        double comparisons_old = 0;
        //AbstractDuplicatePropagation adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
        AbstractDuplicatePropagation adp = Utilities.getGroundTruth(DATASET, CLEAN);

        double pc = 0.0;
        double pc_old = 0.0;
        double pq = 0.0;
        double detectedDuplicates = 0;

        double w_old = Double.MAX_VALUE;
        //while (!comparisons.isEmpty()) {
        while (psn.hasNext()) {
            //Comparison c = comparisons.pollFirst();
            Comparison c = (Comparison) psn.next();
            //double w = c.getUtilityMeasure();
            //if (w_old < w) {
            //  System.out.println("error weight: " + w_old + " " + w);
            //}
            //System.out.println("weight: " + w);
            //w_old = w;


            num_comparisons++;
            detectedDuplicates = adp.getNoOfDuplicates();
            pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
            if ((pc - pc_old) > .1) {
                pc_old = pc;
                //pq = detectedDuplicates / (double) comparisons;
                System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (num_comparisons - comparisons_old));
                comparisons_old = num_comparisons;
            }
            adp.isSuperfluous(c);

            if (pc > 0.8) {
                break;
            }
        }

        detectedDuplicates = adp.getNoOfDuplicates();
        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
        pq = detectedDuplicates / (double) num_comparisons;

        System.out.println("partial res1");
        System.out.println("pc: " + pc);
        System.out.println("pq: " + pq);
    }
}

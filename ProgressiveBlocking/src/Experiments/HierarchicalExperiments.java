package Experiments;

import BlockBuilding.Progressive.Hierarchy.PrefixHierarchies;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.BilateralDuplicatePropagation;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import Utilities.SerializationUtilities;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class HierarchicalExperiments {

    public static void main(String[] args) {
        String[] entityPaths = {"E:\\Data\\CCERdata\\abt-buy\\dataset1",
            "E:\\Data\\CCERdata\\abt-buy\\dataset2"};
        List<EntityProfile>[] profiles = new List[2];
        profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[0]);
        profiles[1] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[1]);
//        String[] entityPaths = {"E:\\Data\\DERdata\\abt-buy\\dataset"};
//        List<EntityProfile>[] profiles = new List[1];
//        profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[0]);

//        SuffixHierarchies sh = new SuffixHierarchies(profiles);
        PrefixHierarchies sh = new PrefixHierarchies(profiles);
        sh.getOriginalComparisons();
        System.out.println("BF comparisons\t:\t" + sh.getBruteForceComparisons());

//        String duplicatesPath = "E:\\Data\\DERdata\\abt-buy\\groundtruth";
//        AbstractDuplicatePropagation adp = new UnilateralDuplicatePropagation(duplicatesPath);
        String duplicatesPath = "E:\\Data\\CCERdata\\abt-buy\\groundtruth";
        AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(duplicatesPath);

        double pc = 0.0;
        double pc_old = 0.0;
        double pq = 0.0;
        double detectedDuplicates = 0;
        double num_comparisons = 0;
        double comparisons_old = 0;
        Comparison c;
        while ((c = (Comparison) sh.next()) != null) {
            num_comparisons++;
            detectedDuplicates = adp.getNoOfDuplicates();
            pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
            if ((pc - pc_old) > .05) {
                pc_old = pc;
                pq = detectedDuplicates / (double) num_comparisons;
                System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (num_comparisons - comparisons_old) + "\t\t" + pq);
                comparisons_old = num_comparisons;
            }
            adp.isSuperfluous(c);

//            if (pc > 0.95) {
//                break;
//            }
        }

        detectedDuplicates = adp.getNoOfDuplicates();
        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
        pq = detectedDuplicates / (double) num_comparisons;

        System.out.println("partial res1");
        System.out.println("pc: " + pc);
        System.out.println("pq: " + pq + "\t\t" + num_comparisons);
    }
}

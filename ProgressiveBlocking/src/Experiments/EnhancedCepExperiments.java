package Experiments;

import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepBlockScheduling;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepCnp;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.BilateralDuplicatePropagation;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import MetaBlocking.WeightingScheme;
import Utilities.SerializationUtilities;

import java.util.List;

/**
 * @author G.A.P. II
 */
public class EnhancedCepExperiments {

    public static void main(String[] args) {
        String[] entityPaths = {"/Users/gio/Desktop/umich/data/data_blockingFramework/articles/profiles/dataset1",
                "/Users/gio/Desktop/umich/data/data_blockingFramework/articles/profiles/dataset2"};
        List<EntityProfile>[] profiles = new List[2];
        profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[0]);
        profiles[1] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[1]);
//        String[] entityPaths = { "E:\\Data\\DERdata\\abt-buy\\dataset" };
//        List<EntityProfile>[] profiles = new List[1];
//        profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[0]);

        String duplicatesPath = "/Users/gio/Desktop/umich/data/data_blockingFramework/articles/groundtruth";
//        String duplicatesPath = "E:\\Data\\DERdata\\abt-buy\\groundtruth";
        System.out.println("\n\nCurrent weighting scheme\t:\t" + WeightingScheme.JS);

//            AbstractDuplicatePropagation adp = new UnilateralDuplicatePropagation(duplicatesPath);
        AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(duplicatesPath);
        CepBlockScheduling cbs = new CepBlockScheduling(profiles, WeightingScheme.JS);
            /*CepCnp cbs = new CepCnp(profiles, wScheme);*/

        double pc = 0.0;
        double pc_old = 0.0;
        double pq = 0.0;
        double detectedDuplicates = 0;
        double num_comparisons = 0;
        double comparisons_old = 0;
        while (cbs.hasNext()) {
            Comparison c = (Comparison) cbs.next();

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

//                if (pc > 0.95) {
//                    break;
//                }
        }

        detectedDuplicates = adp.getNoOfDuplicates();
        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
        pq = detectedDuplicates / (double) num_comparisons;

        System.out.println("partial res1");
        System.out.println("pc: " + pc);
        System.out.println("pq: " + pq + "\t\t" + num_comparisons);
    }

}

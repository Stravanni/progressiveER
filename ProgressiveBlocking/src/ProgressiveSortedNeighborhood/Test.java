package ProgressiveSortedNeighborhood;

import BlockBuilding.Progressive.SortedNeighborhood.Local.NaiveProgressiveSn;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.UnilateralDuplicatePropagation;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import Utilities.SerializationUtilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author G.A.P. II
 */

public class Test {

    public static void main(String[] args) {
        String[] entityPaths = {"E:\\Data\\DERdata\\abt-buy\\dataset"};
        List<EntityProfile>[] profiles = new List[1];
        profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[0]);

        String duplicatesPath = "E:\\Data\\DERdata\\abt-buy\\groundtruth";
        AbstractDuplicatePropagation adp = new UnilateralDuplicatePropagation(duplicatesPath);

//        String[] entityPaths = {"E:\\Data\\CCERdata\\abt-buy\\dataset1","E:\\Data\\CCERdata\\abt-buy\\dataset2"};
//        List<EntityProfile>[] profiles = new List[2];
//        profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[0]);
//        profiles[1] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(entityPaths[1]);
//        
//        String duplicatesPath = "E:\\Data\\CCERdata\\abt-buy\\groundtruth";
//        AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(duplicatesPath);

//        NaiveProgressiveSnIterator npsni = new NaiveProgressiveSnIterator(profiles);
//        NaiveProgressiveSnIterator npsni = new LocalCpProgressiveSnIterator(profiles);
//        NaiveProgressiveSnIterator npsni = new LocalAcfWeightedProgressiveSnIterator(profiles);
//        NaiveProgressiveSnIterator npsni = new LocalNcfWeightedProgressiveSnIterator(profiles);
//        NaiveProgressiveSnIterator npsni = new GlobalCpProgressiveSnIterator(profiles);
//        NaiveProgressiveSnIterator npsni = new GlobalAcfWeightedProgressiveSnIterator(profiles);
//        NaiveProgressiveSnIterator npsni = new GlobalNcfWeightedProgressiveSnIterator(profiles);
        NaiveProgressiveSn npsni = new NaiveProgressiveSn(profiles, true);

        double pc = 0.0;
        double pc_old = 0.0;
        double pq = 0.0;
        double detectedDuplicates = 0;
        double num_comparisons = 0;
        double comparisons_old = 0;

        Set<Comparison> comparisons = new HashSet<>();

        Comparison c;
        adp.resetDuplicates();
        while ((c = npsni.next()) != null) {

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

            comparisons.add(c);
        }

        detectedDuplicates = adp.getNoOfDuplicates();
        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
        pq = detectedDuplicates / (double) num_comparisons;

        System.out.println("partial res1");
        System.out.println("pc: " + pc);
        System.out.println("pq: " + pq + "\t\t" + num_comparisons + "\t\t" + comparisons.size());
        System.out.println("duplicates: " + detectedDuplicates);
    }
}

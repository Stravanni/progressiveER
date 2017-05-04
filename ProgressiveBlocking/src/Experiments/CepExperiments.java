package Experiments;

import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.*;
import MetaBlocking.WeightingScheme;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

/**
 * @author giovanni
 */
public class CepExperiments {

    private static int DATASET = 0;
    private static boolean CLEAN = true;
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

        WeightingScheme[] ws = WeightingScheme.values();
        //WeightingScheme[] ws = new WeightingScheme[4];
        //ws[0] = WeightingScheme.CBS;
        //ws[1] = WeightingScheme.ECBS;
        //ws[2] = WeightingScheme.JS;
        //ws[3] = WeightingScheme.EJS;

        Instant start = Instant.now();
        Instant end = Instant.now();

        for (WeightingScheme wScheme : ws) {
            System.out.println("\n\nCurrent weighting scheme\t:\t" + wScheme);

            start = Instant.now();

            TokenBlocking tb;
            if (profiles.length > 1) {
                tb = new TokenBlocking(new List[]{profiles[0], profiles[1]});
            } else {
                tb = new TokenBlocking(new List[]{profiles[0]});
            }
            List<AbstractBlock> blocks = tb.buildBlocks();

            double SMOOTH_FACTOR = 1.005;
            double FILTER_RATIO = 0.8;
            double PC_LIMIT = 0.9;

            ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTH_FACTOR);
            cbbp.applyProcessing(blocks);

            BlockFiltering bf = new BlockFiltering(FILTER_RATIO);
            bf.applyProcessing(blocks);

            ProgressiveCardinalityEdgePruning cep = new ProgressiveCardinalityEdgePruning(wScheme, 200);
            cep.applyProcessing(blocks);

            end = Instant.now();


            double comparisons = 0;
            double comparisons_old = 0;
            //AbstractDuplicatePropagation adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
            AbstractDuplicatePropagation adp = Utilities.getGroundTruth(DATASET, CLEAN);

            double pc = 0.0;
            double pc_old = 0.0;
            double pq = 0.0;
            double detectedDuplicates = 0;

            while (cep.hasNext()) {
                comparisons++;
                detectedDuplicates = adp.getNoOfDuplicates();
                pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                if ((pc - pc_old) > .1) {
                    pc_old = pc;
                    //pq = detectedDuplicates / (double) comparisons;
                    System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                    comparisons_old = comparisons;
                }
                adp.isSuperfluous((Comparison) cep.next());
            }

            detectedDuplicates = adp.getNoOfDuplicates();
            pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
            pq = detectedDuplicates / (double) comparisons;

            System.out.println("partial res1");
            System.out.println("pc: " + pc);
            System.out.println("pq: " + pq);
            //System.out.println("remaining pair to compare: " + cep.getNumCandidate());

            System.out.println("Total time: " + Duration.between(start, end).toString());
            //System.out.println("comparisons: "+ comparisons);
        }
    }
}
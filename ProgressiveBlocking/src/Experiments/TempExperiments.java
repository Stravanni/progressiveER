package Experiments;

import BlockBuilding.AbstractTokenBlocking;
import BlockBuilding.MemoryBased.ExtendedSortedNeighborhoodBlocking;
import BlockBuilding.MemoryBased.SortedNeighborhoodBlocking;
import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import MetaBlocking.FastImplementations.CardinalityEdgePruning;
import MetaBlocking.WeightingScheme;
import Utilities.ComparisonIterator;
import Utilities.BlockStatistics;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author giovanni
 */
public class TempExperiments {

    private static int DATASET = 4;
    private static boolean CLEAN = true;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    public static void main(String[] args) {
        //String profilesFile = "E:\\Data\\profailes\\cddbProfiles";
        //String groundTruthFile = "E:\\Data\\groundtruth\\cddbIdDuplicates";

        //WeightingScheme[] ws = WeightingScheme.values();
        WeightingScheme[] ws = new WeightingScheme[1];
        ws[0] = WeightingScheme.ECBS;

        for (WeightingScheme wScheme : ws) {
            System.out.println("\n\nCurrent weighting scheme\t:\t" + wScheme);

            Instant start = Instant.now();

            List<EntityProfile>[] profiles;
            if (args.length > 0) {
                BASEPATH = args[0] + "/";
                profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
            } else {
                //profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
                profiles = Utilities.getEntities(DATASET, CLEAN);
            }

            //List<EntityProfile>[] profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
            AbstractTokenBlocking tb;
            if (profiles.length > 1) {
                tb = new TokenBlocking(new List[]{profiles[0], profiles[1]});
            } else {
                tb = new TokenBlocking(new List[]{profiles[0]});
            }
            tb = new SortedNeighborhoodBlocking(5, profiles);
            //tb = new ExtendedSortedNeighborhoodBlocking(1, profiles);
            List<AbstractBlock> blocks = tb.buildBlocks();

            double r = 0.55;

            ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(1.0);
            //cbbp.applyProcessing(blocks);

            BlockFiltering bf = new BlockFiltering(r);
            //bf.applyProcessing(blocks);

            List<AbstractBlock> copyOfBlocks = new ArrayList<>(blocks);

            CardinalityEdgePruning cep = new CardinalityEdgePruning(wScheme);
            //WeightedEdgePruning cep = new WeightedEdgePruning(wScheme);
            // BloomFilter.create(Funnels.unencodedCharsFunnel(), 250000000)
            //cep.applyProcessing(copyOfBlocks);
            Instant end = Instant.now();

            double comparisons = 0;
            //AbstractDuplicatePropagation adp = new UnilateralDuplicatePropagation(groundTruthFile);
            AbstractDuplicatePropagation adp = Utilities.getGroundTruth(DATASET, CLEAN);
            //BlockStatistics bStats = new BlockStatistics(copyOfBlocks, adp);
            //double[] stats = bStats.applyProcessing();

            double pc = 0.0;
            double pc_old = 0.0;
            double comparisons_old = 0;

            double pq = 0.0;

            double detectedDuplicates = 0;
            for (AbstractBlock block : copyOfBlocks) {
                //if (pc > 0.8) {
                //break;
                //}
                ComparisonIterator iterator = block.getComparisonIterator();
                while (iterator.hasNext()) {
                    comparisons++;

                    Comparison comparison = iterator.next();
                    adp.isSuperfluous(comparison);

                    detectedDuplicates = adp.getNoOfDuplicates();
                    pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                    pq = detectedDuplicates / (double) comparisons;
                    if ((pc - pc_old) > .1) {
                        pc_old = pc;
                        //pq = detectedDuplicates / (double) comparisons;
                        System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                        comparisons_old = comparisons;
                    }

                    //if (pc > 0.8) {
                    //System.out.println("pc: " + pc);
                    //System.out.println("pq: " + pq);
                    //break;
                    //}
                }
            }

            System.out.println("");
            System.out.println("final pc: " + pc);
            System.out.println("final pq: " + pq);
            System.out.println("");
            //System.out.println("final pc: " + stats[0]);
            //System.out.println("final pq: " + stats[1]);

            System.out.println("Total comparisons\t:\t" + comparisons);
            System.out.println("Total duplicates\t:\t" + adp.getNoOfDuplicates());

            System.out.println("Total time: " + Duration.between(start, end).toString());
        }
    }
}
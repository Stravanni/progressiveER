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
import Utilities.ComparisonIterator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author giovanni
 */
public class CepExperiments_tmp {

    private static int DATASET = 3;
    private static boolean CLEAN = false;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    public static void main(String[] args) {
        //String profilesFile = "E:\\Data\\profailes\\cddbProfiles";
        //String groundTruthFile = "E:\\Data\\groundtruth\\cddbIdDuplicates";
        List<EntityProfile>[] profiles;
        if (args.length > 0) {
            BASEPATH = args[0] + "/";
            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
        } else {
            profiles = Utilities.getEntities(DATASET, CLEAN);
        }

        //WeightingScheme[] ws = WeightingScheme.values();
        WeightingScheme[] ws = new WeightingScheme[1];
        ws[0] = WeightingScheme.ECBS;

        Instant start = Instant.now();
        Instant end = Instant.now();

        for (WeightingScheme wScheme : ws) {
            System.out.println("\n\nCurrent weighting scheme\t:\t" + wScheme);

            start = Instant.now();

            long time1 = System.currentTimeMillis();

            //List<EntityProfile> profiles = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(profilesFile);
            TokenBlocking tb;
            if (profiles.length > 1) {
                tb = new TokenBlocking(new List[]{profiles[0], profiles[1]});
            } else {
                tb = new TokenBlocking(new List[]{profiles[0]});
            }
            List<AbstractBlock> blocks = tb.buildBlocks();

            double SMOOTH_FACTOR = 1.0;
            double FILTER_RATIO = 0.55;
            double PC_LIMIT = 0.9999;

            ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTH_FACTOR);
            cbbp.applyProcessing(blocks);

            BlockFiltering bf = new BlockFiltering(FILTER_RATIO);
            bf.applyProcessing(blocks);


            //List<AbstractBlock> copyOfBlocks = new ArrayList<>(blocks);

            //ProgressiveCardinalityEdgePruning cep = new ProgressiveCardinalityEdgePruning(wScheme, BloomFilter.create(Funnels.unencodedCharsFunnel(), 250000000), true);
            ProgressiveCardinalityEdgePruning_SingleStep cep = new ProgressiveCardinalityEdgePruning_SingleStep(wScheme, new HashSet[1], true);
            //WeightedEdgePruning cep = new WeightedEdgePruning(wScheme);
            // BloomFilter.create(Funnels.unencodedCharsFunnel(), 250000000)
            cep.applyProcessing(blocks);

            end = Instant.now();

            long time2 = System.currentTimeMillis();

            double comparisons = 0;
            double comparisons_old = 0;
            //AbstractDuplicatePropagation adp = new UnilateralDuplicatePropagation(groundTruthFile);
            AbstractDuplicatePropagation adp = Utilities.getGroundTruth(DATASET, CLEAN);

            double pc = 0.0;
            double pc_old = 0.0;
            double pq = 0.0;
            double detectedDuplicates = 0;
//            for (AbstractBlock block : copyOfBlocks) {
//                ComparisonIterator iterator = block.getComparisonIterator();
//                while (iterator.hasNext()) {
//                    comparisons++;
//                    Comparison comparison = iterator.next();
//                    adp.isSuperfluous(comparison);
//                }
//            }

            while (cep.hasNext()) {
                comparisons++;
                detectedDuplicates = adp.getNoOfDuplicates();
                pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                //System.out.println(pc);
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
            System.out.println("remaining pair to compare: " + cep.getNumCandidate());

            System.out.println("Total time: " + Duration.between(start, end).toString());
            //System.out.println("comparisons: "+ comparisons);

            boolean continua = true;

            if (pc < PC_LIMIT) {

                while (!cep.isFinalIteration() && continua) {
                    //copyOfBlocks = new ArrayList<>(blocks);
                    HashSet[] bf_ = cep.getCandidatePairs();
                    cep = new ProgressiveCardinalityEdgePruning_SingleStep(wScheme, bf_, false);
                    cep.applyProcessing(blocks);

//                    for (AbstractBlock block : copyOfBlocks) {
//                        ComparisonIterator iterator = block.getComparisonIterator();
//                        while (iterator.hasNext()) {
//                            comparisons++;
//
//                            Comparison comparison = iterator.next();
//                            adp.isSuperfluous(comparison);
//
//                            detectedDuplicates = adp.getNoOfDuplicates();
//                            pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
//                            pq = detectedDuplicates / (double) comparisons;
//
//                            if (pc > 0.999) {
//                                System.out.println("fine\nfine\nfine\n");
//                                continuta = false;
//                                break;
//                            }
//                        }
//                    }

                    while (cep.hasNext()) {
                        comparisons++;
                        adp.isSuperfluous((Comparison) cep.next());

                        detectedDuplicates = adp.getNoOfDuplicates();
                        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                        pq = detectedDuplicates / (double) comparisons;

                        if ((pc - pc_old) > .1) {
                            pc_old = pc;
                            //pq = detectedDuplicates / (double) comparisons;
                            System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                            comparisons_old = comparisons;
                        }

                        if (pc > PC_LIMIT) {
                            System.out.println("\nfine\n");
                            continua = false;
                            break;
                        }
                    }

                    System.out.println("partial resN");
                    System.out.println("pc: " + pc);
                    System.out.println("pq: " + pq);
                    System.out.println("remaining pair to compare: " + cep.getNumCandidate());
                    //System.out.println("comparisons: "+ comparisons);
                }
            }

            System.out.println("");
            System.out.println("final pc: " + pc);
            System.out.println("final pq: " + pq);

            System.out.println("Total comparisons\t:\t" + comparisons);
            System.out.println("Total duplicates\t:\t" + adp.getNoOfDuplicates());
        }
    }
}
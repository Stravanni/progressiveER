package Experiments.complete_experiment;

import BlockBuilding.MemoryBased.TokenBlocking;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.*;
import BlockBuilding.Progressive.SortedEntities.CepCnpEntities;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import Experiments.Utilities;
import Utilities.BlockStatistics;
import MetaBlocking.WeightingScheme;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author giovanni
 */
public class CepExperiments {

    public static void main(String[] args) {
        boolean CLEAN = true;
        String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";
        double FILTER_RATIO = 0.8d;
        boolean RESERVE_BLOCK_FILTERING = false;

        PrintWriter writer = null;

        //for (int dataset : new int[]{0, 1, 2, 3, 4, 5}) {
        //for (int dataset : new int[]{0, 1, 2, 3}) {
        for (int dataset_num : new int[]{4}) {

            System.out.println("\nDataset: " + Utilities.getName(dataset_num, CLEAN) + "\n");

            List<EntityProfile>[] profiles;
            AbstractDuplicatePropagation adp;
            AbstractDuplicatePropagation adp_tmp;

            Instant start = Instant.now();

            if (args.length > 0) {
                BASEPATH = args[0] + "/";
                profiles = Utilities.getEntities(BASEPATH, dataset_num, CLEAN);
            } else {
                profiles = Utilities.getEntities(dataset_num, CLEAN);
            }

            String name = "";
            System.out.println("\n\nCurrent weighting scheme\t:\t");

            //WeightingScheme[] ws = WeightingScheme.values();
            WeightingScheme[] ws = new WeightingScheme[1];
            ws[0] = WeightingScheme.CBS;
            //ws[1] = WeightingScheme.EJS;
            //ws[2] = WeightingScheme.ARCS;


            Instant end = Instant.now();

            for (WeightingScheme wScheme : ws) {
                CepCnp[] methods = new CepCnp[1];
                //methods[0] = new ProgressiveCardinalityEdgePruning(wScheme, 421075225, true);
                //methods[0] = new ProgressiveCardinalityEdgePruning(wScheme, 1000, true);
                methods[0] = new CepCnp(wScheme);
                /*methods[0] = new CepCnpEntities(wScheme, profiles[0].size() + profiles[1].size());*/
                //methods[0] = new CepCnp(wScheme, 10);

                for (CepCnp progressiveBlocking : methods) {

                    try {
                        writer = new PrintWriter("res_out_" + Utilities.getName(dataset_num, CLEAN) + "_" + progressiveBlocking.getName() + "_" + wScheme + "_f_" + FILTER_RATIO + ".txt", "UTF-8");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    //for (AbstractProgressiveMetaBlocking progressiveBloccking :) {
                    if (args.length > 0) {
                        BASEPATH = args[0] + "/";
                        adp = Utilities.getGroundTruth(BASEPATH, dataset_num, CLEAN);
                        adp_tmp = Utilities.getGroundTruth(BASEPATH, dataset_num, CLEAN);
                    } else {
                        adp = Utilities.getGroundTruth(dataset_num, CLEAN);
                        adp_tmp = Utilities.getGroundTruth(dataset_num, CLEAN);
                    }

                    name = wScheme + "";
                    System.out.println("\n\nCurrent weighting scheme\t:\t" + wScheme);

                    //start = Instant.now();

                    TokenBlocking tb;
                    if (profiles.length > 1) {
                        tb = new TokenBlocking(new List[]{profiles[0], profiles[1]});
                    } else {
                        tb = new TokenBlocking(new List[]{profiles[0]});
                    }
                    List<AbstractBlock> blocks = tb.buildBlocks();

                    double SMOOTH_FACTOR = CLEAN ? 1.005 : 1.015;
                    if (!CLEAN && dataset_num == 0) {
                        SMOOTH_FACTOR = 1.25;
                    } else if (CLEAN && dataset_num == 5) {
                        SMOOTH_FACTOR = 1.0;
                    }

                    ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTH_FACTOR);
                    cbbp.applyProcessing(blocks);

                    BlockFiltering bf = new BlockFiltering(FILTER_RATIO, RESERVE_BLOCK_FILTERING);
                    bf.applyProcessing(blocks);
                    System.out.println("bf: " + blocks.size());
                    System.out.println("bf: " + bf.getReserveBlocks().size());

                    System.out.println("bf pc");
                    BlockStatistics blStats = new BlockStatistics(blocks, adp_tmp);
                    blStats.applyProcessing();
                    System.out.println("progressive start");

                    progressiveBlocking.applyProcessing(blocks);
                    //methods[0].applyProcessing(blocks);


                    double comparisons = 0;
                    double comparisons_old = 0;

                    double pc = 0.0;
                    double pc_old = 0.0;
                    double pq = 0.0;
                    double detectedDuplicates = 0;
                    double totalComparison = CLEAN ? profiles[0].size() * profiles[1].size() : ((profiles[0].size() * (profiles[0].size() - 1)) / 2);

                    ArrayList<Double> pcs = new ArrayList<>();
                    ArrayList<Double> counts = new ArrayList<>();

                    int print = 0;

                    pcs.add(0.0);
                    counts.add(0.0);

                    while (progressiveBlocking.hasNext()) {
                        comparisons++;
                        detectedDuplicates = adp.getNoOfDuplicates();
                        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                        if ((pc - pc_old) > .02) {
                            pc_old = pc;
                            print++;
                            if (print == 5) {
                                print = 0;
                                System.out.println("\npc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                                System.out.println("nc: " + comparisons);
                                System.out.println("partial time: " + Duration.between(start, Instant.now()).toString());
                            }
                            //pq = detectedDuplicates / (double) comparisons;
                            //System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                            pcs.add(Math.round(pc * 100) / 100.0);
                            counts.add(comparisons);
                            comparisons_old = comparisons;
                        }
                        adp.isSuperfluous(progressiveBlocking.next());
                    }

                    /*System.out.println("reserve: " + bf.getReserveBlocks().size());
                    //CepCnp pb = new CepCnp(wScheme);
                    CepBlockScheduling pb = new CepBlockScheduling(wScheme);
                    //pb.setThreshold(100000);
                    pb.applyProcessing(bf.getReserveBlocks());
                    System.out.println("reserve: " + bf.getReserveBlocks().size());

                    System.out.println("reserve comparisons");

                    while (pb.hasNext()) {
                        comparisons++;
                        detectedDuplicates = adp.getNoOfDuplicates();
                        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                        if ((pc - pc_old) > .02) {
                            pc_old = pc;
                            print++;
                            if (print == 5) {
                                print = 0;
                                System.out.println("\npc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                                System.out.println("nc: " + comparisons);
                                System.out.println("partial time: " + Duration.between(start, Instant.now()).toString());
                            }
                            pq = detectedDuplicates / (double) comparisons;
                            System.out.println("pc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                            pcs.add(Math.round(pc * 100) / 100.0);
                            counts.add(comparisons);
                            comparisons_old = comparisons;
                        }
                        adp.isSuperfluous(pb.next());
                    }*/

                    pcs.add(Math.round(pc * 100) / 100.0);
                    counts.add(comparisons);

                    totalComparison = (dataset_num == 5) ? 1 : totalComparison;
                    final double finalTotalComparison = totalComparison;
                    List nc = counts.stream().map(e -> (e / finalTotalComparison)).collect(Collectors.toList());

                    detectedDuplicates = adp.getNoOfDuplicates();
                    pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                    pq = detectedDuplicates / (double) comparisons;

                    System.out.println("\n\nr_comparisons = " + comparisons);
                    System.out.println("t_comparisons = " + totalComparison + "\n\n");

                    System.out.println("nc_" + name + " = " + nc.toString());
                    //System.out.println("res nc_" + name + " = " + counts.toString());
                    System.out.println("pc_" + name + " = " + pcs.toString());

                    end = Instant.now();

                    System.out.println("final pc: " + pc);

                    writer.println(" nc_" + name + " = " + nc.toString());
                    writer.println(" pc_" + name + " = " + pcs.toString());
                    writer.println("Total time: " + Duration.between(start, end).toString());
                    writer.println(" ");

                    System.out.println("Total time: " + Duration.between(start, end).toString());

                    writer.close();
                }
            }
        }
    }
}
/**
 * - NaiveProgressiveSn:                    naivePSN
 * - LocalWeightedProgressiveSn(ws=null):   naivePSN w/o duplicate comparisons within W
 * - LocalWeightedProgressiveSn(ws):        PSN local ordering w/o duplicate comparisons within W
 * - ProgressiveSnHeap:                     PSN hybrid ordering w/ duplicate comparison (they could be removed)
 * - GlobalNaiveProgressiveSnIterator:      naivePSN w/o duplicate comparisons (ALL duplicates comparisons are removed)
 * - GlobalWeightedProgressiveSn(ws=null):  For each entity retain a fixed num of comparisons
 * - GlobalWeightedProgressiveSn(ws):       For each entity retain a fixed num of comparisons + WEIGHTS
 */
package Experiments.complete_experiment;

import BlockBuilding.MemoryBased.TokenBlocking;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.AbstractProgressiveMetaBlocking;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepBlockScheduling;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepCnp;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.ProgressiveCardinalityEdgePruning;
import BlockBuilding.Progressive.SortedEntities.CepCnpEntities;
import BlockBuilding.Progressive.SortedEntities.EntityFiltering;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import Experiments.Utilities;
import Experiments.Utility.Result;
import Experiments.complete_experiment.MethodsList.ExperimentsSN_list;
import MetaBlocking.WeightingScheme;

import Utilities.BlockStatistics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author giovanni
 */
public class CepExperiments_complete {

    private static boolean CLEAN = true;
    private static boolean SCHEMA = false;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    private static double FILTER_RATIO = 0.8;
    /*private static WeightingScheme[] schemes = WeightingScheme.values();*/
    /**
     * First run completed cannot finish (pCEP + ARCS)
     */
    private static WeightingScheme[] schemes = new WeightingScheme[]{WeightingScheme.CBS};

    private static double RESOLUTION = 0.01;
    private static String FILE_OUT = "pmb_clean_2017_03_09.json";

    private static int[] DATASETS = new int[]{0};
    //private static int[] DATASETS = new int[]{4};

    private static ProfileType[] profileTypes = new ProfileType[]{
            ProfileType.CENSUS_PROFILE,
            ProfileType.RESTAURANT_PROFILE,
            ProfileType.CORA_PROFILE,
            ProfileType.CDDB_PROFILE,
    };

    public static void main(String[] args) {

        int DATASET = 0;

        PrintWriter writer = null;

        Result res_final = new Result();

        //for (int dataset : new int[]{0, 1, 2, 3, 4, 5}) {
        //for (int dataset : new int[]{0, 1, 2, 3}) {
        //for (int dataset : new int[]{0,1}) {
        for (int dataset : DATASETS) {
            DATASET = dataset;

            System.out.println("\nDataset: " + Utilities.getName(dataset, CLEAN) + "\n");

            List<EntityProfile>[] profiles;
            AbstractDuplicatePropagation adp;
            AbstractDuplicatePropagation adp_tmp;

            Instant start = Instant.now();
            Instant t_init = Instant.now();

            if (args.length > 0) {
                BASEPATH = args[0] + "/";
                profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
            } else {
                profiles = Utilities.getEntities(DATASET, CLEAN);
            }

            String name = "";
            System.out.println("\n\nCurrent weighting scheme\t:\t");

            //start = Instant.now();


            res_final.set_size(new Long[]{Long.valueOf(profiles[0].size()), CLEAN ? Long.valueOf(profiles[1].size()) : 0});

            System.out.println("build psn entity list");
            System.out.println("finish build psn entity list");


            TokenBlocking tb;
            if (profiles.length > 1) {
                tb = new TokenBlocking(new List[]{profiles[0], profiles[1]});
            } else {
                tb = new TokenBlocking(new List[]{profiles[0]});
                /*tb = new TokenBlocking(0, profileTypes[dataset], new List[]{profiles[0]});*/ // here schema-based
            }
            List<AbstractBlock> blocks = tb.buildBlocks();

            double SMOOTH_FACTOR = CLEAN ? 1.005 : 1.015;
            if (!CLEAN && DATASET == 0) {
                SMOOTH_FACTOR = 1.25;
            } else if (CLEAN && DATASET == 5) {
                SMOOTH_FACTOR = 1.0;
            }
            if (SCHEMA) {
                SMOOTH_FACTOR = 1.25;
            }

            ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTH_FACTOR);
            cbbp.applyProcessing(blocks);

            BlockFiltering bf = new BlockFiltering(FILTER_RATIO, false);
            bf.applyProcessing(blocks);
            System.out.println("bf: " + blocks.size());
            System.out.println("bf: " + bf.getReserveBlocks().size());

            if (args.length > 0) {
                BASEPATH = args[0] + "/";
                adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
                adp_tmp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
            } else {
                adp = Utilities.getGroundTruth(DATASET, CLEAN);
                adp_tmp = Utilities.getGroundTruth(DATASET, CLEAN);
            }

            /*List<AbstractBlock> blocks_stats = new ArrayList<>(blocks);
            System.out.println("bf pc");
            BlockStatistics blStats = new BlockStatistics(blocks_stats, adp_tmp);
            blStats.applyProcessing();
            System.out.println("progressive start");*/

            /*try {
                writer = new PrintWriter("psn_res_out_" + Utilities.getName(dataset, CLEAN) + "_" + "psn_new" + "_classic_rep_" + ".txt", "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/

            //List<AbstractBlock> blocks_tmp = new ArrayList<>(blocks);

            List<AbstractProgressiveMetaBlocking> progressive_methods = new ArrayList<>();

            for (WeightingScheme ws : schemes) {
                progressive_methods.add(new ProgressiveCardinalityEdgePruning(ws));
                /*progressive_methods.add(new CepCnp(ws));
                progressive_methods.add(new CepBlockScheduling(ws));
                progressive_methods.add(new CepCnpEntities(ws, CLEAN ? profiles[0].size() + profiles[1].size() : profiles[0].size()));*/
                /*progressive_methods.add(new EntityFiltering(ws, blocks, profiles[0].size()));*/
            }

            for (AbstractProgressiveMetaBlocking pm : progressive_methods) {

                List<AbstractBlock> blocks_tmp = new ArrayList<>(blocks);

                if (args.length > 0) {
                    BASEPATH = args[0] + "/";
                    adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
                    adp_tmp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
                } else {
                    adp = Utilities.getGroundTruth(DATASET, CLEAN);
                    adp_tmp = Utilities.getGroundTruth(DATASET, CLEAN);
                }

                if (args.length > 0) {
                    BASEPATH = args[0] + "/";
                    adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
                } else {
                    adp = Utilities.getGroundTruth(DATASET, CLEAN);
                }

                int profileSize = CLEAN ? profiles[0].size() + profiles[1].size() : profiles[0].size();

                //ProgressiveCardinalityEdgePruning method = new ProgressiveCardinalityEdgePruning(ws, profiles[0].size() * profiles[1].size());
                //ProgressiveCardinalityEdgePruning method = new ProgressiveCardinalityEdgePruning(ws, (profiles[0].size() * profiles[1].size()) * 10);
                /*ProgressiveCardinalityEdgePruning method = new ProgressiveCardinalityEdgePruning(ws);*/
                /*CepCnp method = new CepCnp(ws, 1);*/

                res_final.set_dupl_e((long) adp.getExistingDuplicates());

                /*res_final.add_res_set();*/
                /*start = Instant.now();*/

                res_final.start();

                pm.applyProcessing(blocks_tmp);

                res_final.init();

                String description = Utilities.getName(dataset, CLEAN) + "_";
                description += pm.getName();

                res_final.set_desription(description);

                double comparisons = 0;
                double comparisons_old = 0;

                double pc = 0.0;
                double pc_old = 0.0;
                double pq = 0.0;
                double detectedDuplicates = 0;
                long totalComparison = CLEAN ? profiles[0].size() * profiles[1].size() : ((profiles[0].size() * (profiles[0].size() - 1)) / 2);

                ArrayList<Double> pcs = new ArrayList<>();
                ArrayList<Double> counts = new ArrayList<>();

                int print = 0;

                pcs.add(0.0);
                counts.add(0.0);

            /*HashSet<Comparison> comp = new HashSet<>();*/

                while (pm.hasNext()) {
                    comparisons++;
                    detectedDuplicates = adp.getNoOfDuplicates();
                    pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                    if ((pc - pc_old) > RESOLUTION) {
                        res_final.add_res(detectedDuplicates, comparisons);
                        pc_old = pc;
                        print++;
                        if (print == 50) {
                            print = 0;
                            System.out.println("\npc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                            System.out.println("nc: " + comparisons);
                            System.out.println("partial time: " + Duration.between(start, Instant.now()).toString());
                        }
                        pcs.add(Math.round(pc * 100) / 100.0);
                        counts.add(comparisons);
                        comparisons_old = comparisons;
                    }
                    Comparison next = (Comparison) pm.next();
                    if (next != null) {
                    /*if (comp.contains(next)) {
                        System.out.println("not removing redundant comparisons");
                    }
                    comp.add(next);*/
                        adp.isSuperfluous(next);
                    }
                }
                res_final.add_res(detectedDuplicates, comparisons);

                /*res_final.set_comp_b((long) detectedDuplicates);*/

                //System.out.println("\n\nredundant comparison identifyied: " + ((NaiveProgressiveSn) psn).countRedundant + "\n");

                pcs.add(Math.round(pc * 100) / 100.0);
                counts.add(comparisons);

                List nc = new ArrayList<>();
                totalComparison = (dataset == 5) ? 1 : totalComparison;
                final long finalTotalComparison = totalComparison;
                nc = counts.stream().map(e -> (e / finalTotalComparison)).collect(Collectors.toList());


                detectedDuplicates = adp.getNoOfDuplicates();
                pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                pq = detectedDuplicates / (double) comparisons;

                System.out.println("\n\nr_comparisons = " + comparisons);
                System.out.println("t_comparisons = " + totalComparison + "\n\n");

                //System.out.println("nc_" + name + " = " + nc.toString());
                System.out.println("x_30_psn_t" + name + " = " + nc.toString());
                //System.out.println("res nc_" + name + " = " + counts.toString());
                //System.out.println("pc_" + name + " = " + pcs.toString());
                System.out.println("y_30_psn_t" + name + " = " + pcs.toString());

                Instant end = Instant.now();

            /*writer.println(" nc_" + name + " = " + nc.toString());
            writer.println(" pc_" + name + " = " + pcs.toString());
            writer.println(" ");*/

                /*res_final.set_t_end(end.toEpochMilli() - start.toEpochMilli());*/
                res_final.end();

                System.out.println("Total time: " + Duration.between(start, end).toString());

            /*writer.close();*/
            }
        }
        try {
            writer = new PrintWriter(FILE_OUT, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(res_final.toJson());
        writer.close();
    }
}
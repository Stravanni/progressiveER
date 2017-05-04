/**
 * - NaiveProgressiveSn:                    naivePSN
 * - LocalWeightedProgressiveSn(ws=null):   naivePSN w/o duplicate comparisons within W
 * - LocalWeightedProgressiveSn(ws):        PSN local ordering w/o duplicate comparisons within W
 * - ProgressiveSnHeap:                     PSN hybrid ordering w/ duplicate comparison (they could be removed)
 * - GlobalNaiveProgressiveSnIterator:      naivePSN w/o duplicate comparisons (ALL duplicates comparisons are removed)
 * - GlobalWeightedProgressiveSn(ws=null):  For each entity retain a fixed num of comparisons
 * - GlobalWeightedProgressiveSn(ws):       For each entity retain a fixed num of comparisons + WEIGHTS
 */
package Experiments.complete_experiment.time_experiment;

import BlockBuilding.MemoryBased.TokenBlocking;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.AbstractProgressiveMetaBlocking;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepBlockScheduling;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.CepCnp;
import BlockBuilding.Progressive.ProgressiveMetaBlocking.ProgressiveCardinalityEdgePruning;
import BlockBuilding.Progressive.SortedEntities.CepCnpEntities;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import Experiments.Utilities;
import Experiments.Utility.Result;
import MetaBlocking.WeightingScheme;
import info.debatty.java.stringsimilarity.Levenshtein;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author giovanni
 */
public class _time_pmb_record {

    private static boolean CLEAN = false;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    private static String METHOD = "cep";
    private static String DS = "1";

    private static int max_comparison_per_match = -1;

    private static boolean COMPUTE_DISTANCE = true;
    /*private static NormalizedLevenshtein distance = new NormalizedLevenshtein();*/
    private static Levenshtein distance = new Levenshtein();

    private static boolean SCHEMA = false;

    private static double FILTER_RATIO = 0.8;

    private static WeightingScheme[] schemes = new WeightingScheme[]{WeightingScheme.CBS};

    private static WeightingScheme WS = WeightingScheme.CBS;

    private static double RESOLUTION = 0.01;
    /*private static String FILE_OUT = "psn_dirty_completa_2017_03_09_local_heap_global.json";*/
    /*private static String FILE_OUT = "psn_dirty_completa_2017_03_09_local_heap_global.json";*/
    /*private static String FILE_OUT = "psn_clean_completa_2017_03_09_local_heap_global_.json";*/

    /*private static String PATH_OUT = "/Users/gio/Desktop/notebook progressive/data_json/paris/";*/
    private static String PATH_OUT = "";
    private static String FILE_OUT = PATH_OUT + "dirty_psn_time_distance_ds_" + DS + "_method_" + METHOD + "_" + ".json";

    private static int[] DATASETS = new int[]{0, 1};
    /*private static int[] DATASETS = new int[]{2};*/
    /*private static int[] DATASETS = new int[]{0};*/

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

        List<EntityProfile>[] profiles;
        AbstractDuplicatePropagation adp;
        AbstractDuplicatePropagation adp_tmp;

        Instant start = Instant.now();

        /**
         * ******************************************************************************************
         *
         * arguments
         *
         * ******************************************************************************************
         */
        if (args.length > 0) {
            BASEPATH = args[0] + "/";
            DS = args[1]; /* dataset number */
            METHOD = args[2]; /* method name */
            DATASET = Integer.parseInt(DS);

            String isClean = args[3];
            String compute_distance = args[4];
            String num_run = args[5];

            DATASET = Integer.parseInt(DS);

            if (compute_distance.equals("dist")) {
                COMPUTE_DISTANCE = true;
            } else {
                COMPUTE_DISTANCE = false;
            }

            if (isClean.equals("clean")) {
                CLEAN = true;
                FILE_OUT = PATH_OUT + "celan_pmn_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";
            } else {
                CLEAN = false;
                FILE_OUT = PATH_OUT + "dirty_pmn_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";
            }

            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
        } else {
            profiles = Utilities.getEntities(DATASET, CLEAN);
        }

        System.out.println("\nDataset: " + Utilities.getName(DATASET, CLEAN) + "\n");

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

        List<AbstractProgressiveMetaBlocking> progressive_methods = new ArrayList<>();

            /*for (WeightingScheme ws : schemes) {
                progressive_methods.add(new ProgressiveCardinalityEdgePruning(ws));
                progressive_methods.add(new CepCnp(ws));
                progressive_methods.add(new CepBlockScheduling(ws));
                progressive_methods.add(new CepCnpEntities(ws, CLEAN ? profiles[0].size() + profiles[1].size() : profiles[0].size()));
                *//*progressive_methods.add(new EntityFiltering(ws, blocks, profiles[0].size()));*//*
            }*/

        switch (METHOD) {
            case "cep":
                progressive_methods.add(new ProgressiveCardinalityEdgePruning(WS));
                break;
            case "cepCnp":
                progressive_methods.add(new CepCnp(WS));
                break;
            case "cepBlock":
                progressive_methods.add(new CepBlockScheduling(WS));
                break;
            case "cepCnpEntity":
                progressive_methods.add(new CepCnpEntities(WS, CLEAN ? profiles[0].size() + profiles[1].size() : profiles[0].size()));
                break;
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

            String description = Utilities.getName(DATASET, CLEAN) + "_";
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
                Comparison next = (Comparison) pm.next();
                if (next != null) {
                    if (COMPUTE_DISTANCE) {
                        EntityProfile e1 = profiles[0].get(next.getEntityId1());
                        EntityProfile e2 = profiles[0].get(next.getEntityId2());
                        /*distance.distance(e1.getString(), e2.getString());*/
                        minDistance(e1.getString(), e2.getString());
                        /*getEditDistance(e1.getString(), e2.getString());*/
                        /*System.out.println(e1.getString());
                        System.out.println(e2.getString());*/
                    }
                }
                if (comparisons == 1000) {
                    break;
                }
            }
            res_final.add_res(detectedDuplicates, comparisons);

                /*res_final.set_comp_b((long) detectedDuplicates);*/

            //System.out.println("\n\nredundant comparison identifyied: " + ((NaiveProgressiveSn) psn).countRedundant + "\n");

            pcs.add(Math.round(pc * 100) / 100.0);
            counts.add(comparisons);

            List nc = new ArrayList<>();
            totalComparison = (DATASET == 5 && CLEAN) ? 1 : totalComparison;
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


        /*try {
            writer = new PrintWriter(FILE_OUT, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(res_final.toJson());
        writer.close();*/
    }

    public static int minDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);

                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }
        return dp[len1][len2];
    }
}
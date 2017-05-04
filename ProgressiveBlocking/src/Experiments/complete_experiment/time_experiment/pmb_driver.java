/**
 * - NaiveProgressiveSn:                    naive
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
import info.debatty.java.stringsimilarity.JaroWinkler;
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
public class pmb_driver {

    private static boolean CLEAN = true;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    private static String METHOD = "cep";
    private static String DS = "0";

    private static int DISTANCE_MEASURE = 3;

    private static Double k_comparisons = 0.0;

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
    private static String FILE_OUT = PATH_OUT + "dirty_pmb_time_distance_ds_" + DS + "_method_" + METHOD + "_" + ".json";

    private static int[] DATASETS = new int[]{0, 1};
    /*private static int[] DATASETS = new int[]{2};*/
    /*private static int[] DATASETS = new int[]{0};*/

    private static ProfileType[] profileTypes = new ProfileType[]{
            ProfileType.CENSUS_PROFILE,
            ProfileType.RESTAURANT_PROFILE,
            ProfileType.CORA_PROFILE,
            ProfileType.CDDB_PROFILE,
    };

    private static EntityProfile e1;
    private static EntityProfile e2;

    private static JaroWinkler distance1 = new JaroWinkler();
    private static Levenshtein distance2 = new Levenshtein();

    public static void main(String[] args) {

        int DATASET = Integer.parseInt(DS);

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
            /*BASEPATH = args[0] + "/";
            DS = args[1]; *//* dataset number *//*
            METHOD = args[2]; *//* method name *//*
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

            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);*/
            BASEPATH = args[0] + "/";
            DS = args[1]; /* dataset number */
            METHOD = args[2]; /* method name */
            String weighting = args[3];
            String isClean = args[4];
            String compute_distance = args[5];
            String distance_measure = args[6]; /* not used here */
            String max_cpe = args[7];
            FILE_OUT = args[8];

            if (k_comparisons > 0)
                k_comparisons = Double.parseDouble(max_cpe);


            DATASET = Integer.parseInt(DS);

            switch (weighting) {
                case "arcs":
                    WS = WeightingScheme.ARCS;
                    break;
                case "cbs":
                    WS = WeightingScheme.CBS;
                    break;
                case "js":
                    WS = WeightingScheme.JS;
                    break;
                case "ecbs":
                    WS = WeightingScheme.ECBS;
                    break;
                case "ejs":
                    WS = WeightingScheme.EJS;
                    break;
            }

            DISTANCE_MEASURE = Integer.parseInt(distance_measure);
            COMPUTE_DISTANCE = false;
            if (compute_distance.equals("dist"))
                COMPUTE_DISTANCE = true;

            CLEAN = false;
            /*FILE_OUT = PATH_OUT + "dirty_pmb_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";*/
            if (isClean.equals("clean"))
                CLEAN = true;
                /*FILE_OUT = PATH_OUT + "celan_pmb_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";*/

            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
        } else {
            profiles = Utilities.getEntities(DATASET, CLEAN);
        }

        System.out.println("\nDataset: " + Utilities.getName(DATASET, CLEAN) + "\n");

        String name = "";
        System.out.println("\n\nCurrent weighting scheme\t:\t" + WS);

        //start = Instant.now();


        res_final.set_size(new Long[]{Long.valueOf(profiles[0].size()), CLEAN ? Long.valueOf(profiles[1].size()) : 0});

        System.out.println("build pmb entity list");
        System.out.println("finish build pmb entity list");


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
            FILTER_RATIO = 0.55;
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

        switch (METHOD) {
            case "cep":
                /*Double num_profiles = Double.valueOf(CLEAN? (profiles[0].size() + profiles[1].size()): (profiles[0].size()));*/
                if (k_comparisons == 0) {
                    Double manual_threshold = Double.valueOf(adp.getExistingDuplicates() * 25);
                    System.out.println("25 * duplicates = " + manual_threshold);
                    progressive_methods.add(new ProgressiveCardinalityEdgePruning(WS, manual_threshold));
                } else if (k_comparisons > 0) {
                    progressive_methods.add(new ProgressiveCardinalityEdgePruning(WS, k_comparisons));
                } else {
                    progressive_methods.add(new ProgressiveCardinalityEdgePruning(WS));
                }
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

            res_final.init();
            Instant init = Instant.now();

            while (pm.hasNext()) {
                comparisons++;
                detectedDuplicates = adp.getNoOfDuplicates();
                pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                if ((pc - pc_old) > RESOLUTION) {
                    res_final.add_res(detectedDuplicates, comparisons);
                    pc_old = pc;
                    /*print++;
                    if (print == 1) {
                        print = 0;
                        System.out.println("\npc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                        System.out.println("nc: " + comparisons);
                        System.out.println("partial time: " + Duration.between(start, Instant.now()).toString());
                        System.out.println("time per recall: " + res_final.latest_time);
                    }*/
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

                    if (COMPUTE_DISTANCE && DISTANCE_MEASURE >= 0) {
                        e1 = profiles[0].get(next.getEntityId1());
                        if (CLEAN) {
                            e2 = profiles[1].get(next.getEntityId2());
                        } else {
                            e2 = profiles[0].get(next.getEntityId2());
                        }

                        switch (DISTANCE_MEASURE) {
                            case 0:
                                break;
                            case 1:
                                distance1.distance(e1.getString(), e2.getString());
                                break;
                            case 2:
                                distance2.distance(e1.getString(), e2.getString());
                                break;
                            case 3:
                                minDistance(e1.getString(), e2.getString());
                                break;
                            default:
                                DISTANCE_MEASURE = -1;
                                break;
                        }
                    }
                }
            }
            res_final.add_res(detectedDuplicates, comparisons);
            Instant end = Instant.now();
            res_final.end();

            System.out.println("\npc_final = " + pc);

            System.out.println(METHOD + "_comparison_count = " + comparisons);

            double time_init = Duration.between(start, init).toMillis();
            System.out.println(METHOD + "t_init_ms = " + time_init);

            double time_comp = Duration.between(init, end).toMillis();
            System.out.println(METHOD + "t_comp_ms = " + time_comp);

            double time_per_comp = time_comp / comparisons;
            System.out.println(METHOD + "t_each_ms = " + (time_comp / comparisons));

            System.out.println("Total time: " + Duration.between(start, end).toString());

            /*writer.close();*/
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
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

/**
 * @author giovanni
 */
public class pmb_time_blocking {

    private static boolean CLEAN = true;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    private static String METHOD = "cep";
    private static String DS = "4";

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
        Instant end = Instant.now();


        /**
         * ******************************************************************************************
         *
         * arguments
         *
         * ******************************************************************************************
         */
        if (args.length > 0) {
            /*profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);*/
            BASEPATH = args[0] + "/";
            DS = args[1]; /* dataset number */
            String isClean = args[2];
            FILE_OUT = args[3];

            DATASET = Integer.parseInt(DS);

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


        res_final.set_size(new Long[]{Long.valueOf(profiles[0].size()), CLEAN ? Long.valueOf(profiles[1].size()) : 0});

        System.out.println("build pmb entity list");
        System.out.println("finish build pmb entity list");


        start = Instant.now();

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

        end = Instant.now();

        double time_init = Duration.between(start, end).toMillis();
        System.out.println(DS + "t_init_ms = " + time_init);
    }
}
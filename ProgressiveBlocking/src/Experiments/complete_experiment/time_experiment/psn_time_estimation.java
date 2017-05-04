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

import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import Experiments.Utilities;
import Experiments.Utility.Result;
import Experiments.complete_experiment.MethodsList.ExperimentsSN_list;
import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author giovanni
 */
public class psn_time_estimation {

    /*
    "censusProfiles",
    "restaurantProfiles",
    "coraProfiles",
    "cddbProfiles"
    */

    /**
     * For the baseline (psn):s
     * To do schema_aware modify getKey functino in ExperimentSN_list
     */

    private static boolean CLEAN = false;
    private static boolean COMPUTE_DISTANCE = false;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    private int aa = 0;
    private static String METHOD = "naivePSN";

    private static String DS = "0";

    private static int DISTANCE_MEASURE = 2;

    private static double MAX_PC = 1.0;
    private static int MAX_WINDOW = 100; /*20,1000 is ok for all (2c,20,1000) (3c,10,100) (4c,20,1000)*/
    private static int MAX_LOCAL_WIN = 10;
    private static int MAX_CPE = 100;

    private static int comparisons_per_run = 1000;
    private static int run_num = 10;

    private static double[] time_random = new double[run_num];
    private static double[] time_progr = new double[run_num];

    private static Jaccard distance1 = new Jaccard();
    private static Damerau distance2 = new Damerau(); /*this is the lowest*/
    /*private static Damerau distance2 = ;*/

    private static double RESOLUTION = 0.01;

    /*private static String PATH_OUT = "/Users/gio/Desktop/notebook progressive/data_json/paris/";*/
    private static String PATH_OUT = "";
    /*private static String FILE_OUT = PATH_OUT + "dirty_psn_time_distance_ds_" + DS + "_method_" + METHOD + "_" + ".json";*/
    private static String FILE_OUT = PATH_OUT + METHOD + "_dirty_" + DS + ".json";

    private static int[] DATASETS = new int[]{0, 1};
    /*private static int[] DATASETS = new int[]{2};*/
    /*private static int[] DATASETS = new int[]{0};*/

    static EntityProfile e1;
    static EntityProfile e2;

    private static ProfileType[] profileTypes = new ProfileType[]{
            ProfileType.CENSUS_PROFILE,
            ProfileType.RESTAURANT_PROFILE,
            ProfileType.CORA_PROFILE,
            ProfileType.CDDB_PROFILE,
    };

    public static void main(String[] args) {

        ExperimentsSN_list experiment;

        int DATASET = Integer.parseInt(DS);

        PrintWriter writer = null;

        /**
         * These are MAX_WIN for the methods.
         * For "exaustive" methods should be set to an high number (e.g. 10000)
         */
        switch (METHOD) {
            case "naivePSN_schema":
                if (!CLEAN && DATASET == 2) {
                    MAX_WINDOW = 1000;
                } else if (!CLEAN && DATASET == 3) {
                    MAX_WINDOW = 1000;
                } else {
                    MAX_WINDOW = 20;
                }
                break;
            case "naivePSN":
            case "naivePSN_norep_schema":
            case "naivePSN_norep":
                MAX_WINDOW = 20;
                break;
            case "globalACF":
            case "globalNCF":
            case "globalID":
                if (CLEAN && DATASET == 3) {
                    MAX_WINDOW = 20;
                    MAX_CPE = 2000;
                } else if (DATASET == 5) {
                    MAX_WINDOW = 20;
                    MAX_CPE = 100;
                } else {
                    MAX_WINDOW = 20;
                    MAX_CPE = 1000;
                }
                break;
            case "localACF":
            case "localNCF":
                MAX_WINDOW = CLEAN ? 20 : 10;
                MAX_PC = 0.98;
                break;
            case "localGlobalACF":
            case "localGlobalNCF":
                MAX_WINDOW = CLEAN ? 20 : 10;
                MAX_WINDOW = 100;
                MAX_LOCAL_WIN = 10;
                MAX_CPE = 1000;
                if (CLEAN && DATASET == 5) {
                    MAX_WINDOW = 50;
                    MAX_LOCAL_WIN = 10;
                    MAX_CPE = 300;
                }
            default: //
                MAX_WINDOW = 500;
                break;
        }

        //for (int dataset : new int[]{0, 1, 2, 3, 4, 5}) {
        //for (int dataset : new int[]{0, 1, 2, 3}) {
        //for (int dataset : new int[]{0,1}) {

        List<EntityProfile>[] profiles;
        AbstractDuplicatePropagation adp;

        if (args.length > 0) {
            BASEPATH = args[0] + "/";
            DS = args[1]; /* dataset number */
            String isClean = args[2];
            String distance_measure = args[3];
            String num_run = args[4];
            String num_iteration_per_run = args[5];

            DATASET = Integer.parseInt(DS);

            DISTANCE_MEASURE = Integer.parseInt(distance_measure);
            COMPUTE_DISTANCE = true;

            CLEAN = false;
            /*FILE_OUT = PATH_OUT + "dirty_psn_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";*/
            if (isClean.equals("clean")) {
                CLEAN = true;
                /*FILE_OUT = PATH_OUT + "celan_psn_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";*/
            }

            comparisons_per_run = Integer.parseInt(num_iteration_per_run);
            run_num = Integer.parseInt(num_run);

            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);

        } else {
            profiles = Utilities.getEntities(DATASET, CLEAN);
        }

        System.out.println("\n\n\nDataset: " + Utilities.getName(DATASET, CLEAN) + "\n");

        String name = "";
        System.out.println("\n\nCurrent weighting scheme\t:\t");


        experiment = new ExperimentsSN_list(profiles, MAX_WINDOW, MAX_LOCAL_WIN, MAX_CPE);

        System.out.println("build psn entity list");

        if (args.length > 0) {
            BASEPATH = args[0] + "/";
            adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
        } else {
            adp = Utilities.getGroundTruth(DATASET, CLEAN);
        }


        /**
         * ***************************************************************************
         * Time start
         * ***************************************************************************
         */

        Instant start_random;
        Instant end_random;
        double time_comparisons;

        EntityProfile p_random_1;
        EntityProfile p_random_2;

        int random_1;
        int random_2;

        int profiles_1 = profiles[0].size();
        int profiles_2 = 0;
        if (CLEAN)
            profiles_2 = profiles[1].size();

        for (int i = 0; i < run_num; i++) {
            start_random = Instant.now();
            for (int j = 0; j < comparisons_per_run; j++) {
                random_1 = (int) (Math.random() * profiles_1);
                p_random_1 = profiles[0].get(random_1);
                if (CLEAN) {
                    random_2 = (int) (Math.random() * profiles_2);
                    p_random_2 = profiles[1].get(random_2);
                } else {
                    random_2 = (int) (Math.random() * profiles_1);
                    p_random_2 = profiles[0].get(random_2);
                }

                switch (DISTANCE_MEASURE) {
                    case 0:
                        break;
                    case 1:
                        distance1.distance(p_random_1.getString(), p_random_2.getString());
                        break;
                    case 2:
                        distance2.distance(p_random_1.getString(), p_random_2.getString());
                        break;
                    case 3:
                        minDistance(p_random_1.getString(), p_random_2.getString());
                        break;
                    default:
                        DISTANCE_MEASURE = -1;
                        break;
                }

            }
            end_random = Instant.now();
            time_comparisons = Duration.between(start_random, end_random).toMillis();
            /*System.out.println("random_iteration_" + i + " = " + time_comparisons / comparisons_per_run);*/
            time_random[i] = time_comparisons / comparisons_per_run;
        }
        start_random = Instant.now();
        end_random = Instant.now();
        time_comparisons = Duration.between(start_random, end_random).toMillis();

        Method pns_method = null;
        try {
            pns_method = experiment.getClass().getMethod(METHOD, Integer.TYPE, ProfileType.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Iterator psn = null;
        try {
            if (!CLEAN) {
                if (METHOD.equals("naivePSN_schema"))
                    psn = (Iterator) pns_method.invoke(experiment, 1, profileTypes[DATASET]);
                else
                    psn = (Iterator) pns_method.invoke(experiment, -1, profileTypes[DATASET]);
            } else {
                psn = (Iterator) pns_method.invoke(experiment, -1, profileTypes[0]);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        String description = Utilities.getName(DATASET, CLEAN) + "_";

        description += METHOD;

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

        System.out.println("finish build psn entity list");

        /**
         * ***************************************************************************
         * Time init
         * ***************************************************************************
         */
        Instant init = Instant.now();

        int i = run_num;
        int j = comparisons_per_run;

        while (psn.hasNext() && pc < MAX_PC) {
            comparisons++;
            detectedDuplicates = adp.getNoOfDuplicates();
            pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
            if ((pc - pc_old) > RESOLUTION) {
                pc_old = pc;
                print++;
                pcs.add(Math.round(pc * 100) / 100.0);
                counts.add(comparisons);
            }
            Comparison next = (Comparison) psn.next();

            if (j == comparisons_per_run) {
                start_random = Instant.now();
            }

            if (next != null) {
                adp.isSuperfluous(next);


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

            if (j == 0) {
                end_random = Instant.now();
                time_comparisons = Duration.between(start_random, end_random).toMillis();
                time_progr[i - 1] = time_comparisons / comparisons_per_run;
                j = comparisons_per_run;
                i--;
            } else {
                j--;
            }
            if (i == 0) {
                pc = 1.1;
            }
        }
        System.out.println("time_random = " + Arrays.toString(time_random));
        System.out.println("time_progr  = " + Arrays.toString(time_progr));
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

    public int getEditDistance(String sourceString, String destinationString) {
        if (sourceString == null || destinationString == null) {
            throw new IllegalArgumentException("String cannot be null");
        }

        int sourceLength = sourceString.length();
        int destLength = destinationString.length();
        int len = Math.min(sourceLength, destLength);

        int distance = Math.abs(sourceLength - destLength);
        for (int i = 0; i < len; ++i) {
            if (sourceString.charAt(i) != destinationString.charAt(i)) {
                ++distance;
            }
        }

        return distance;
    }
}
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
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;

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
public class psn_driver_time {

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
    private static boolean COMPUTE_DISTANCE = true;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    private int aa = 0;
    private static String METHOD = "localGlobalACF";

    private static String DS = "1";

    private static int DISTANCE_MEASURE = 0;

    private static double MAX_PC = 1.0;
    private static int MAX_WINDOW = 100; /*20,1000 is ok for all (2c,20,1000) (3c,10,100) (4c,20,1000)*/
    private static int MAX_LOCAL_WIN = 1;
    private static int MAX_CPE = 100;
    /*private static boolean REMOVE_REPETED = false;*/

    private static int max_comparison_per_match = -1;
    private static long max_time_per_match = 100000000;

    /*private static NormalizedLevenshtein distance = new NormalizedLevenshtein();*/
    /*JaroWinkler distance = new JaroWinkler();*/
    private static JaroWinkler distance1 = new JaroWinkler();
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

        Result res_final = new Result();
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
                    MAX_LOCAL_WIN = 1;
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
            METHOD = args[2]; /* method name */

            String isClean = args[3];
            String compute_distance = args[4];
            String distance_measure = args[5];
            String max_window = args[6];
            String max_local_window = args[7];
            String max_cpe = args[8];

            FILE_OUT = args[9];

            int max_win_tmp = Integer.parseInt(max_window);
            int max_local_win_tmp = Integer.parseInt(max_local_window);

            if (MAX_CPE > 0) {
                MAX_CPE = Integer.parseInt(max_cpe);
            }
            if (max_win_tmp > 0) {
                MAX_WINDOW = max_win_tmp;
            }

            if (max_local_win_tmp > 0) {
                MAX_LOCAL_WIN = max_local_win_tmp;
            }

            DATASET = Integer.parseInt(DS);

            DISTANCE_MEASURE = Integer.parseInt(distance_measure);
            COMPUTE_DISTANCE = false;
            if (compute_distance.equals("dist")) {
                COMPUTE_DISTANCE = true;
            }

            CLEAN = false;
            /*FILE_OUT = PATH_OUT + "dirty_psn_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";*/
            if (isClean.equals("clean")) {
                CLEAN = true;
                /*FILE_OUT = PATH_OUT + "celan_psn_" + compute_distance + "_" + DS + "_method_" + METHOD + "_run_" + num_run + ".json";*/
            }

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

        res_final.set_size(new Long[]{Long.valueOf(profiles[0].size()), CLEAN ? Long.valueOf(profiles[1].size()) : 0});
        res_final.set_dupl_e((long) adp.getExistingDuplicates());
        res_final.start();


        /**
         * ***************************************************************************
         * Time start
         * ***************************************************************************
         */
        Instant start = Instant.now();

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

        System.out.println("finish build psn entity list");

        /**
         * ***************************************************************************
         * Time init
         * ***************************************************************************
         */
        Instant init = Instant.now();
        res_final.init();

        int l = 0;
        int c = 0;
        while (psn.hasNext() && pc < MAX_PC) {
            comparisons++;
            detectedDuplicates = adp.getNoOfDuplicates();
            pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
            if ((pc - pc_old) > RESOLUTION) {
                res_final.add_res(detectedDuplicates, comparisons);
                pc_old = pc;
                print++;
                /*if (print == 10) {
                    print = 0;
                    System.out.println("\npc: " + Math.round(pc * 100) / 100.0 + " - " + (comparisons - comparisons_old));
                    System.out.println("nc: " + comparisons);
                    System.out.println("partial time: " + Duration.between(start, Instant.now()).toString());
                }*/
                pcs.add(Math.round(pc * 100) / 1.0);
                counts.add(comparisons);

                comparisons_old = comparisons;
            }
            Comparison next = (Comparison) psn.next();

            if (next != null) {
                adp.isSuperfluous(next);

                if (COMPUTE_DISTANCE && DISTANCE_MEASURE >= 0) {
                    e1 = profiles[0].get(next.getEntityId1());
                    l += e1.getString().split(" ").length;
                    c++;
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
        System.out.println("mean tokens: " + l / c);
        res_final.add_res(detectedDuplicates, comparisons);

        /**
         * ***************************************************************************
         * Time start
         * ***************************************************************************
         */
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

        try {
            writer = new PrintWriter(FILE_OUT, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(res_final.toJson());
        writer.close();


        pcs.add(Math.round(pc * 100) / 1.0);
        counts.add(comparisons);

        List nc = new ArrayList<>();

        final long finalTotalComparison = totalComparison;
        nc = counts.stream().map(e -> (e / adp.getExistingDuplicates())).collect(Collectors.toList());


        detectedDuplicates = adp.getNoOfDuplicates();
        pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
        pq = detectedDuplicates / (double) comparisons;

        System.out.println("\n\nr_comparisons = " + comparisons);
        System.out.println("t_comparisons = " + totalComparison + "\n\n");

        System.out.println("nc_" + name + " = " + nc.toString());
        //System.out.println("res nc_" + name + " = " + counts.toString());
        System.out.println("pc_" + name + " = " + pcs.toString());

            /*writer.close();*//*
        try {
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
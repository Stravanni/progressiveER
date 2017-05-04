package Experiments.complete_experiment;

import BlockBuilding.Progressive.Hierarchy.ProgressiveMinHashHierarchies;
import BlockBuilding.Progressive.Hierarchy.ProgressivePrefixHierarchies;
import BlockBuilding.Progressive.Hierarchy.ProgressiveSuffixHierarchies;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import Experiments.Utilities;
import Experiments.Utility.Result;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author giovanni
 */
public class CepExperimentsHierarchic {

    private static final boolean CLEAN = false;
    private static final boolean REMOVE_REPETED = false;
    private static int minimum_prefix = 5;

    public static void main(String[] args) {

        String base_path = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

        //for (int dataset_num : new int[]{0, 1, 2, 3, 4, 5}) {
        for (int dataset_num : new int[]{1}) {

            String file_out = CLEAN ? "sab_clean_" : "sab_dirty_";
            file_out += dataset_num + ".json";

            System.out.println("\nDataset: " + Utilities.getName(dataset_num, CLEAN) + "\n");

            List<EntityProfile>[] profiles;
            AbstractDuplicatePropagation adp;

            Instant start = Instant.now();

            if (args.length > 0) {
                base_path = args[0] + "/";
                profiles = Utilities.getEntities(base_path, dataset_num, CLEAN);
            } else {
                profiles = Utilities.getEntities(dataset_num, CLEAN);
            }

            if (args.length > 0) {
                base_path = args[0] + "/";
                adp = Utilities.getGroundTruth(base_path, dataset_num, CLEAN);
            } else {
                adp = Utilities.getGroundTruth(dataset_num, CLEAN);
            }

            String name = "";
            System.out.println("\n\nCurrent weighting scheme\t:\t");

            //start = Instant.now();

            //ProgressiveSuffixHierarchies sh = new ProgressiveSuffixHierarchies(profiles, true);
            ProgressiveSuffixHierarchies sh = new ProgressiveSuffixHierarchies(profiles, minimum_prefix, true, REMOVE_REPETED);
            /*ProgressiveMinHashHierarchies sh = new ProgressiveMinHashHierarchies(profiles, REMOVE_REPETED);*/
            sh.createHierarchy();
            //sh.getOriginalComparisons();

            Result res_final = new Result();
            PrintWriter writer = null;

            res_final.set_size(new Long[]{Long.valueOf(profiles[0].size()), CLEAN ? Long.valueOf(profiles[1].size()) : 0});
            res_final.set_dupl_e((long) adp.getExistingDuplicates());
            res_final.start();

            String description = Utilities.getName(dataset_num, CLEAN) + "_";

            description += "SA-SAB";
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

            Comparison c;
            while ((c = (Comparison) sh.next()) != null) {
                comparisons++;
                detectedDuplicates = adp.getNoOfDuplicates();
                pc = ((double) detectedDuplicates) / adp.getExistingDuplicates();
                if ((pc - pc_old) > .01) {
                    res_final.add_res(detectedDuplicates, comparisons);
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
                    pcs.add(Math.round(pc * 100) / 1.0);
                    counts.add(comparisons);
                    comparisons_old = comparisons;
                }
                adp.isSuperfluous(c);
            }

            res_final.add_res(detectedDuplicates, comparisons);
            res_final.end();

            try {
                writer = new PrintWriter(file_out, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            writer.print(res_final.toJson());
            writer.close();

            pcs.add(Math.round(pc * 100) / 1.0);
            counts.add(comparisons);

            final double finalComparisons = comparisons;
            List nc = new ArrayList<>();

            totalComparison = (dataset_num == 5) ? 1 : totalComparison;
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

            Instant end = Instant.now();

            System.out.println("Total time: " + Duration.between(start, end).toString());
        }
    }
}
package BlockBuilding.Progressive.ProgressiveMetaBlocking;

import Comparators.ComparisonWeightComparatorMax;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.FastEntityIndex;
import MetaBlocking.FastImplementations.CardinalityEdgePruning;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author giovanni
 */
public class ProgressiveCardinalityEdgePruning_SingleStep extends CardinalityEdgePruning implements Iterator {

    protected double minimumWeight;
    // Guava MinMaxPriorityQueue is MinMax (both "peekFirst" and "peekLast()" at constant time).
    // http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/MinMaxPriorityQueue.html
    //protected MinMaxPriorityQueue<Comparison> topKEdges;
    protected HashSet[] entityFilters; // Could be a BloomFileter? Guava has an implementation.
    protected boolean single_iteration;
    protected boolean first_iteration;
    protected boolean final_iteration;
    protected double num_remaining_comparisons;
    protected int offset;

    protected boolean memory_free = true;

    /**
     * ProgressiveCEP "single-iteration"-mode
     * Basically, it's like standard CEP, but progressive
     *
     * @param scheme
     */
    public ProgressiveCardinalityEdgePruning_SingleStep(WeightingScheme scheme) {
        super("Fast Cardinality Edge Pruning (" + scheme + ")", scheme);

        if (!first_iteration) {
            entityFilters = new HashSet[1];
        }

        this.single_iteration = true;
        this.first_iteration = true;
        this.final_iteration = false;

        num_remaining_comparisons = 0;
    }

    /**
     * ProgressiveCEP for "iterative"-mode
     * Used for iterating:
     * - step one: like CEP it compares the most promising NT paris
     * - step two: it compares the second most promising NT, and so forth for the subsequent iterations
     *
     * @param scheme
     * @param bf
     * @param first_iteration
     */
    public ProgressiveCardinalityEdgePruning_SingleStep(WeightingScheme scheme, HashSet[] bf, boolean first_iteration) {
        super("Fast Cardinality Edge Pruning (" + scheme + ")", scheme);

        if (!first_iteration) {
            entityFilters = bf;
        }
        this.single_iteration = false;
        this.first_iteration = first_iteration;
        this.final_iteration = false;
        num_remaining_comparisons = 0;
    }

//    protected void addDecomposedBlock(MinMaxPriorityQueue<Comparison> comparisons, List<AbstractBlock> newBlocks) {
//        if (comparisons.isEmpty()) {
//            return;
//        }
//
//        int[] entityIds1 = new int[comparisons.size()];
//        int[] entityIds2 = new int[comparisons.size()];
//
//        int index = 0;
//
//        Iterator<Comparison> iterator = comparisons.iterator();
//        System.out.println("evaluating weight range: " + comparisons.peekFirst().getUtilityMeasure() + " - " + comparisons.peekLast().getUtilityMeasure());
//
//        while (comparisons.size() > 0) {
//            Comparison comparison = comparisons.poll();
//            entityIds1[index] = comparison.getEntityId1();
//            entityIds2[index] = comparison.getEntityId2();
//            index++;
//        }
//        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
//    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        entityIndex = new FastEntityIndex(blocks);

        cleanCleanER = entityIndex.isCleanCleanER();
        datasetLimit = entityIndex.getDatasetLimit();
        noOfBlocks = blocks.size();
        noOfEntities = entityIndex.getNoOfEntities();
        bBlocks = entityIndex.getBilateralBlocks();
        uBlocks = entityIndex.getUnilateralBlocks();

        applyMainProcessing(blocks);
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        minimumWeight = Double.MIN_VALUE;
        int countComparisons = 0;
        /*topKEdges = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparatorMax())
                *//*.maximumSize((int) threshold)*//*
                .create();*/
        /*topKEdges = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparatorMax()).create();*/
        topKEdges = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparatorMax()).create();

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        offset = cleanCleanER ? datasetLimit : 0;

        if (first_iteration) {
            entityFilters = new HashSet[limit + 1];
        }

        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                if (first_iteration) {
                    entityFilters[i] = new HashSet<Integer>();
                }
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                if (first_iteration) {
                    entityFilters[i] = new HashSet<Integer>();
                }
                processEntity(i);
                verifyValidEntities(i);
                countComparisons++;
            }
        }
        if (topKEdges.size() < threshold) {
            this.final_iteration = true;
            if (final_iteration) {
                System.out.println("FINAL ITERATION");
            }
        }
        System.out.println("\nheap actual size: " + topKEdges.size());

        System.out.println("heap max size     : " + (int) threshold);
        System.out.println("num comparisons   : " + countComparisons);

        System.out.println("available heap memory: " + getAvailableMemory()); //100000000 sono 100MB
        //addDecomposedBlock(topKEdges, newBlocks);
    }

    @Override
    protected void setThreshold() {
        if (threshold == 0) {
            System.out.println("set threhosld to: " + blockAssingments / 2);
            threshold = (int) blockAssingments / 2;
            // Here we can insert a control of the maximum threshold allowed given a certain amount of memory.
            // if all the comparisons fits in memory, threshold should be set to |E|^2 (DirtyER) or to |E_1|*|E_2| (CleanCleanER).
            System.out.println("aggregate cardinality: " + aggregateCardinality);
        }
    }

    protected void setThreshold(double th) {
        threshold = th;
    }

    @Override
    protected void verifyValidEntities(int entityId) {
        for (int neighborId : validEntities) {
            if (single_iteration) {
                double weight = getWeight(entityId, neighborId);
                Comparison comparison = getComparison(entityId, neighborId);
                comparison.setUtilityMeasure(weight);

                /*if (!isAvailableMemory()) {
                    System.out.println("memory finished");
                }*/

                /*if (topKEdges.size() >= threshold) {*/
                if (!isAvailableMemory() && topKEdges.size() > 2) {
                    //if (!isAvailableMemory()) {
                    /*System.out.println("heap size. " + topKEdges.size());*/
                    if (topKEdges.peekLast().getUtilityMeasure() > weight) {
                        //pairBloom_new.put(comparison.getSignature());
                        num_remaining_comparisons++;
                    } else {
                        //pairBloom_new.put(topKEdges.pollLast().getSignature());
                        topKEdges.pollLast();
                        topKEdges.offer(comparison);
                        //entityFilters[entityId].add(neighborId);
                    }
                } else {
                    topKEdges.offer(comparison);
                    //entityFilters[entityId].add(neighborId);
                }

                //topKEdges.offer(comparison);
            } else {
                //int entityId1_sig = entityId;
                //int entityId2_sig = neighborId;
                //String sign = (entityId1_sig < entityId2_sig) ? (Integer.toString(entityId1_sig) + "#" + Integer.toString(entityId2_sig)) : (Integer.toString(entityId2_sig) + "#" + Integer.toString(entityId1_sig));

                //if (pairBloom.mightContain(sign) || first_iteration) {
                if (!entityFilters[entityId].contains(neighborId) || first_iteration) {
                    double weight = getWeight(entityId, neighborId);

                    Comparison comparison = getComparison(entityId, neighborId);
                    comparison.setUtilityMeasure(weight);
                    //comparison.setSignature(sign);
                    /*if (!isAvailableMemory()) {
                        System.out.println("memory finished");
                    }*/
                    if (topKEdges.size() >= threshold) {
                        if (topKEdges.peekLast().getUtilityMeasure() > weight) {
                            //pairBloom_new.put(comparison.getSignature());
                            num_remaining_comparisons++;
                        } else {
                            //pairBloom_new.put(topKEdges.pollLast().getSignature());
                            topKEdges.pollLast();
                            topKEdges.offer(comparison);
                            entityFilters[entityId].add(neighborId);
                        }
                    } else {
                        topKEdges.offer(comparison);
                        entityFilters[entityId].add(neighborId);
                    }
                }
            }
        }
    }

    public boolean isFinalIteration() {
        return final_iteration;
    }

    public HashSet[] getCandidatePairs() {
        return entityFilters;
    }

    public double getNumCandidate() {
        return num_remaining_comparisons;
    }

    @Override
    public boolean hasNext() {
        return !topKEdges.isEmpty();
    }

    @Override
    public Object next() {
        return topKEdges.poll();
    }

    long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
        long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
        long maxMemory = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
        long usedMemory = totalMemory - freeMemory; // how much of the current heap the VM is using
        long availableMemory = maxMemory - usedMemory; // available memory i.e. Maximum heap size minus the current amount used
        return availableMemory;
    }

    boolean isAvailableMemory() {
        if (!memory_free) {
            return false;
        } else {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
            long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
            long maxMemory = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
            long usedMemory = totalMemory - freeMemory; // how much of the current heap the VM is using
            long availableMemory = maxMemory - usedMemory; // available memory i.e. Maximum heap size minus the current amount used
            //System.out.println("mem: " + availableMemory);
            memory_free = availableMemory > 10000000;
            /*memory_free = freeMemory > 500000000;*/
        }
        return memory_free;
    }
}


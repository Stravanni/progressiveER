/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */

package SupervisedMetablocking;

import Comparators.ComparisonWeightComparator;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.IdDuplicates;
import Utilities.ComparisonIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import weka.classifiers.Classifier;
import weka.core.Instance;

/**
 *
 * @author gap2
 */

public class SupervisedCNP extends AbstractSupervisedMetablocking {
    
    private long kThreshold;
    private double[] minimumWeight;
    private Queue<Comparison>[] nearestEntities;
    
    public SupervisedCNP (int noOfClassifiers, List<AbstractBlock> bls, Set<IdDuplicates> duplicatePairs) {
        super (noOfClassifiers, bls, duplicatePairs);
        getKThreshold();
    }

    private void addComparison(Comparison comparison) {
        if (minimumWeight[comparison.getEntityId1()] == -1) {
            nearestEntities[comparison.getEntityId1()] = new PriorityQueue<Comparison>((int)(2*kThreshold), new ComparisonWeightComparator());
            nearestEntities[comparison.getEntityId1()].add(comparison);
            minimumWeight[comparison.getEntityId1()] = 0;
        } else if (minimumWeight[comparison.getEntityId1()] < comparison.getUtilityMeasure()) {
            nearestEntities[comparison.getEntityId1()].add(comparison);
            if (kThreshold < nearestEntities[comparison.getEntityId1()].size()) {
                Comparison lastComparison = nearestEntities[comparison.getEntityId1()].poll();
                minimumWeight[comparison.getEntityId1()] = lastComparison.getUtilityMeasure();
            }
        }

        int entityId2 = comparison.getEntityId2()+entityIndex.getDatasetLimit();
        if (minimumWeight[entityId2] == -1) {
            nearestEntities[entityId2] = new PriorityQueue<Comparison>((int)(2*kThreshold), new ComparisonWeightComparator());
            nearestEntities[entityId2].add(comparison);
            minimumWeight[entityId2] = 0;
        } else if (minimumWeight[entityId2] < comparison.getUtilityMeasure()) {
            nearestEntities[entityId2].add(comparison);
            if (kThreshold < nearestEntities[entityId2].size()) {
                Comparison lastComparison = nearestEntities[entityId2].poll();
                minimumWeight[entityId2] = lastComparison.getUtilityMeasure();
            }
        }
    }
    
    @Override
    protected void applyClassifier(Classifier classifier) throws Exception {
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                final List<Integer> commonBlockIndices = entityIndex.getCommonBlockIndices(block.getBlockIndex(), comparison);
                if (commonBlockIndices == null) {
                    continue;
                }
                
                if (trainingSet.contains(comparison)) {
                    continue;
                }
                
                Instance currentInstance = getFeatures(NON_DUPLICATE, commonBlockIndices, comparison);
                double[] probabilities = classifier.distributionForInstance(currentInstance);
                if (probabilities[NON_DUPLICATE] < probabilities[DUPLICATE]) {
                    comparison.setUtilityMeasure(probabilities[DUPLICATE]);
                    addComparison(comparison);
                }
            }
        }
    }
    
    private List<AbstractBlock> gatherBilateralComparisons() {
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        for (int i = 0; i < entityIndex.getDatasetLimit(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }

            int index = 0;
            int[] entities1 = new int[1];
            int[] entities2 = new int[nearestEntities[i].size()];
            final Iterator<Comparison> iterator = nearestEntities[i].iterator();
            while (iterator.hasNext()) {
                final Comparison comparison = iterator.next();
                entities1[0] = comparison.getEntityId1();
                entities2[index++] = comparison.getEntityId2();
            }

            BilateralBlock bBlock = new BilateralBlock(entities1, entities2);
            newBlocks.add(bBlock);
        }

        for (int i = entityIndex.getDatasetLimit(); i < entityIndex.getNoOfEntities(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }

            int index = 0;
            int[] entities1 = new int[nearestEntities[i].size()];
            int[] entities2 = new int[1];
            final Iterator<Comparison> iterator = nearestEntities[i].iterator();
            while (iterator.hasNext()) {
                final Comparison comparison = iterator.next();
                entities1[index++] = comparison.getEntityId1();
                entities2[0] = comparison.getEntityId2();
            }

            BilateralBlock bBlock = new BilateralBlock(entities1, entities2);
            newBlocks.add(bBlock);
        }
        return newBlocks;
    }

    @Override
    protected List<AbstractBlock> gatherComparisons() {
        if (blocks.get(0) instanceof BilateralBlock) {
            return gatherBilateralComparisons();
        } else {
            return gatherUnilateralComparisons();
        }
    }

    private List<AbstractBlock> gatherUnilateralComparisons() {
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        for (int i = 0; i < entityIndex.getNoOfEntities(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }
            
            int index = 0;
            int[] entities1 = new int[1];
            int[] entities2 = new int[nearestEntities[i].size()];
            final Iterator<Comparison> iterator = nearestEntities[i].iterator();
            while (iterator.hasNext()) {
                final Comparison comparison = iterator.next();
                entities1[0] = i;
                entities2[index++] = (i == comparison.getEntityId1() ? comparison.getEntityId2() : comparison.getEntityId1());
            }

            BilateralBlock bBlock = new BilateralBlock(entities1, entities2);
            newBlocks.add(bBlock);
        }
        return newBlocks;
    }

    private void getKThreshold() {
        long blockAssingments = 0;
        for (AbstractBlock block : blocks) {
            blockAssingments += block.getTotalBlockAssignments();
        }

        kThreshold = (int) Math.max(1, blockAssingments / (entityIndex.getNoOfEntities()));
        System.out.println("K\t:\t" + kThreshold);
    }
    
    @Override
    protected void initializeDataStructures() {
        detectedDuplicates = new HashSet<IdDuplicates>();
        int noOfEntities = entityIndex.getNoOfEntities();
        minimumWeight = new double[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            minimumWeight[i] = -1;
        }

        nearestEntities = new PriorityQueue[entityIndex.getNoOfEntities()];
    }

    @Override
    protected void processComparisons(int classifierId) {
        double comparisons = 0;
        for (int i = 0; i < entityIndex.getNoOfEntities(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }

            final Iterator<Comparison> iterator = nearestEntities[i].iterator();
            while (iterator.hasNext()) {
                comparisons++;
                final Comparison comparison = iterator.next();
                if (areMatching(comparison)) {
                    final IdDuplicates matchingPair = new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2());
                    detectedDuplicates.add(matchingPair);  
                }
            }
        }
                
        System.out.println("Executed comparisons\t:\t" + comparisons);
        System.out.println("Detected duplicates\t:\t" + detectedDuplicates.size());
        sampleComparisons[classifierId].add(comparisons);
        sampleDuplicates[classifierId].add((double)detectedDuplicates.size());
    }  
}
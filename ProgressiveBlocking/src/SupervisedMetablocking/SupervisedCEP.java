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
import DataStructures.DecomposedBlock;
import DataStructures.IdDuplicates;
import Utilities.ComparisonIterator;
import Utilities.Converter;
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

public class SupervisedCEP extends AbstractSupervisedMetablocking {
    
    private double minimumWeight;
    private long kThreshold;
    private Queue<Comparison> topKEdges;
    
    public SupervisedCEP (int noOfClassifiers, List<AbstractBlock> bls, Set<IdDuplicates> duplicatePairs) {
        super (noOfClassifiers, bls, duplicatePairs);
        getKThreshold();
    }
    
    private void addComparison(Comparison comparison) {
        if (comparison.getUtilityMeasure() < minimumWeight) {
            return;
        }

        topKEdges.add(comparison);
        if (kThreshold < topKEdges.size()) {
            Comparison lastComparison = topKEdges.poll();
            minimumWeight = lastComparison.getUtilityMeasure();
        }
    }

    @Override
    protected void applyClassifier (Classifier classifier) throws Exception {
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
    
    @Override
    protected List<AbstractBlock> gatherComparisons() {
        final List<Integer> entities1 = new ArrayList<Integer>();
        final List<Integer> entities2 = new ArrayList<Integer>();
        final Iterator<Comparison> iterator = topKEdges.iterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            entities1.add(comparison.getEntityId1());
            entities2.add(comparison.getEntityId2());
        }
        
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        int[] entityIds1 = Converter.convertCollectionToArray(entities1);
        int[] entityIds2 = Converter.convertCollectionToArray(entities2);
        
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
        return newBlocks;
    }
    
    private void getKThreshold() {
        long blockAssingments = 0;
        for (AbstractBlock block : blocks) {
            blockAssingments += block.getTotalBlockAssignments();
        }

        kThreshold = blockAssingments / 2;
        System.out.println("K=" + kThreshold);
    }
    
    @Override
    protected void initializeDataStructures() {
        detectedDuplicates = new HashSet<IdDuplicates>();
        minimumWeight = Double.MIN_VALUE;
        topKEdges = new PriorityQueue<Comparison>((int)(2*kThreshold), new ComparisonWeightComparator());
    }

    @Override
    protected void processComparisons(int classifierId) {
        final Iterator<Comparison> iterator = topKEdges.iterator();
        while (iterator.hasNext()) {
            final Comparison comparison = iterator.next();
            if (areMatching(comparison)) {
                final IdDuplicates matchingPair = new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2());
                detectedDuplicates.add(matchingPair);  
            }
        }
                
        System.out.println("Executed comparisons\t:\t" + topKEdges.size());
        System.out.println("Detected duplicates\t:\t" + detectedDuplicates.size());
        sampleComparisons[classifierId].add((double)topKEdges.size());
        sampleDuplicates[classifierId].add((double)detectedDuplicates.size());
    }   
}
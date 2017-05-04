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

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.DecomposedBlock;
import DataStructures.IdDuplicates;
import Utilities.ComparisonIterator;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import weka.classifiers.Classifier;
import weka.core.Instance;

/**
 *
 * @author gap2
 */

public class SupervisedWEP extends AbstractSupervisedMetablocking {
    
    private List<Integer> retainedEntities1;
    private List<Integer> retainedEntities2;
    
    public SupervisedWEP (int noOfClassifiers, List<AbstractBlock> bls, Set<IdDuplicates> duplicatePairs) {
        super (noOfClassifiers, bls, duplicatePairs);
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
                int instanceLabel = (int) classifier.classifyInstance(currentInstance);  
                if (instanceLabel == DUPLICATE) {
                    retainedEntities1.add(comparison.getEntityId1());
                    retainedEntities2.add(comparison.getEntityId2());
                }
            }
        }
    }

    @Override
    protected List<AbstractBlock> gatherComparisons() {
        int[] entityIds1 = Converter.convertCollectionToArray(retainedEntities1);
        int[] entityIds2 = Converter.convertCollectionToArray(retainedEntities2);
        
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
        return newBlocks;
    }

    @Override
    protected void initializeDataStructures() {
        detectedDuplicates = new HashSet<IdDuplicates>();
        retainedEntities1 = new ArrayList<Integer>();
        retainedEntities2 = new ArrayList<Integer>();
    }

    @Override
    protected void processComparisons(int classifierId) {
        System.out.println("\n\nProcessing comparisons...");

        int[] entityIds1 = Converter.convertCollectionToArray(retainedEntities1);
        int[] entityIds2 = Converter.convertCollectionToArray(retainedEntities2);
        for (int i = 0; i < entityIds1.length; i++) {
            Comparison comparison = new Comparison(dirtyER, entityIds1[i], entityIds2[i]);
            if (areMatching(comparison)) {
                final IdDuplicates matchingPair = new IdDuplicates(entityIds1[i], entityIds2[i]);
                detectedDuplicates.add(matchingPair);  
            }
        }
                
        System.out.println("Executed comparisons\t:\t" + entityIds1.length);
        System.out.println("Detected duplicates\t:\t" + detectedDuplicates.size());
        sampleComparisons[classifierId].add((double)entityIds1.length);
        sampleDuplicates[classifierId].add((double)detectedDuplicates.size());
    }
}
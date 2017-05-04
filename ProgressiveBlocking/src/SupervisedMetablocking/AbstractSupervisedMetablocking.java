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
import DataStructures.Comparison;
import DataStructures.EntityIndex;
import DataStructures.IdDuplicates;
import DataStructures.UnilateralBlock;
import Utilities.ComparisonIterator;
import Utilities.Constants;
import Utilities.ExecuteBlockComparisons;
import Utilities.StatisticsUtilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author gap2
 */

public abstract class AbstractSupervisedMetablocking implements Constants {
 
    protected final boolean dirtyER;
 
    protected int noOfAttributes;
    protected int noOfClassifiers;
    protected double noOfBlocks;
    protected double validComparisons;
    protected double[] comparisonsPerBlock;
    protected double[] nonRedundantCPE;
    protected double[] redundantCPE;
    
    protected Attribute classAttribute;
    protected ArrayList<Attribute> attributes;
    protected final EntityIndex entityIndex;
    protected Instances trainingInstances;
    protected final List<AbstractBlock> blocks;
    protected List<Double>[] overheadTimes;
    protected List<Double>[] resolutionTimes;
    protected List<Double> sampleMatches;
    protected List<Double> sampleNonMatches;
    protected List<Double>[] sampleComparisons;
    protected List<Double>[] sampleDuplicates;
    protected List<String> classLabels;
    protected final Set<IdDuplicates> duplicates;
    protected Set<Comparison> trainingSet;
    protected Set<IdDuplicates> detectedDuplicates;
    
    public AbstractSupervisedMetablocking (int classifiers, List<AbstractBlock> bls, Set<IdDuplicates> duplicatePairs) {
        blocks = bls;
        dirtyER = blocks.get(0) instanceof UnilateralBlock;
        entityIndex = new EntityIndex(blocks);
        duplicates = duplicatePairs;
        noOfClassifiers = classifiers;
        
        getStatistics();
        prepareStatistics();
        getAttributes();
    }
    
    protected abstract void applyClassifier(Classifier classifier) throws Exception;
    protected abstract List<AbstractBlock> gatherComparisons();
    protected abstract void initializeDataStructures();
    protected abstract void processComparisons(int configurationId);
    
    public void applyProcessing(int iteration, Classifier[] classifiers, ExecuteBlockComparisons ebc) throws Exception {
        getTrainingSet(iteration);
        for (int i = 0; i < classifiers.length; i++) {
            System.out.println("\n\nClassifier id\t:\t" + i);
            initializeDataStructures();
            
            long startingTime = System.currentTimeMillis();
            classifiers[i].buildClassifier(trainingInstances);
            applyClassifier(classifiers[i]);
            List<AbstractBlock> newBlocks = gatherComparisons();
            double overheadTime = System.currentTimeMillis()-startingTime;
            System.out.println("CL"+i+" Overhead time\t:\t" + overheadTime);
            overheadTimes[i].add(overheadTime);
            
            //commented out for faster experiments
            //use when measuring resolution time
            long comparisonsTime = 0;//ebc.comparisonExecution(newBlocks);
            System.out.println("CL"+i+" Classification time\t:\t" + (comparisonsTime+overheadTime));
            resolutionTimes[i].add(new Double(comparisonsTime+overheadTime));
            
            processComparisons(i);
        }
    }
    
    protected boolean areMatching(Comparison comparison) {
        if (dirtyER) {
            final IdDuplicates duplicatePair1 = new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2());
            final IdDuplicates duplicatePair2 = new IdDuplicates(comparison.getEntityId2(), comparison.getEntityId1());
            return duplicates.contains(duplicatePair1) || duplicates.contains(duplicatePair2);
        }
        
        final IdDuplicates duplicatePair1 = new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2());
        return duplicates.contains(duplicatePair1);
    }
    
    private void getAttributes() {
        attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("ECBS"));
        attributes.add(new Attribute("RACCB"));
        attributes.add(new Attribute("JaccardSim"));
        attributes.add(new Attribute("NodeDegree1"));
        attributes.add(new Attribute("NodeDegree2"));
        
        classLabels = new ArrayList<String>();
        classLabels.add(NON_MATCH);
        classLabels.add(MATCH);
        
        classAttribute = new Attribute("class", classLabels);
        attributes.add(classAttribute);
        noOfAttributes = attributes.size();
    }
    
    private void getStatistics() {
        noOfBlocks = blocks.size();
        validComparisons = 0;
        int noOfEntities = entityIndex.getNoOfEntities();
        
        redundantCPE = new double[noOfEntities];
        nonRedundantCPE = new double[noOfEntities];
        comparisonsPerBlock = new double[(int)(blocks.size() + 1)];
        for (AbstractBlock block : blocks) {
            comparisonsPerBlock[block.getBlockIndex()] = block.getNoOfComparisons();
            
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                    
                int entityId2 = comparison.getEntityId2()+entityIndex.getDatasetLimit();
                redundantCPE[comparison.getEntityId1()]++;
                redundantCPE[entityId2]++;
                    
                if (!entityIndex.isRepeated(block.getBlockIndex(), comparison)) {
                    validComparisons++;
                    nonRedundantCPE[comparison.getEntityId1()]++;
                    nonRedundantCPE[entityId2]++;
                }
            }
        }
    }
    
    protected Instance getFeatures(int match, List<Integer> commonBlockIndices, Comparison comparison) {
        double[] instanceValues = new double[noOfAttributes];

        int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();

        double ibf1 = Math.log(noOfBlocks/entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0));
        double ibf2 = Math.log(noOfBlocks/entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), 1));
        instanceValues[0] = commonBlockIndices.size()*ibf1*ibf2;

        double raccb = 0;
        for (Integer index : commonBlockIndices) {
            raccb += 1.0 / comparisonsPerBlock[index];
        }
        if (raccb < 1.0E-6) {
            raccb = 1.0E-6;
        }
        instanceValues[1] = raccb;

        instanceValues[2] = commonBlockIndices.size() / (redundantCPE[comparison.getEntityId1()] + redundantCPE[entityId2] - commonBlockIndices.size());
        instanceValues[3] = nonRedundantCPE[comparison.getEntityId1()];
        instanceValues[4] = nonRedundantCPE[entityId2];
        instanceValues[5] = match;
        
        Instance newInstance = new DenseInstance(1.0, instanceValues);
        newInstance.setDataset(trainingInstances);
        return newInstance;
    }
    
    protected void getTrainingSet(int iteration) {
        int trueMetadata = 0;
        Random random = new Random(iteration);
        int matchingInstances = (int) (SAMPLE_SIZE*duplicates.size()+1);
        double nonMatchRatio = matchingInstances / (validComparisons - duplicates.size());

        trainingSet = new HashSet<Comparison>(4*matchingInstances);
        trainingInstances = new Instances("trainingSet", attributes, 2*matchingInstances);
        trainingInstances.setClassIndex(noOfAttributes - 1);

        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                final List<Integer> commonBlockIndices = entityIndex.getCommonBlockIndices(block.getBlockIndex(), comparison);
                if (commonBlockIndices == null) {
                    continue;
                }

                int match = NON_DUPLICATE; // false
                if (areMatching(comparison)) {
                    if (random.nextDouble() < SAMPLE_SIZE) {
                        trueMetadata++;
                        match = DUPLICATE; // true
                    } else {
                        continue;
                    }
                } else if (nonMatchRatio <= random.nextDouble()) {
                    continue;
                }

                trainingSet.add(comparison);
                Instance newInstance = getFeatures(match, commonBlockIndices, comparison);
                trainingInstances.add(newInstance);
            }
        }

        sampleMatches.add((double) trueMetadata);
        sampleNonMatches.add((double) (trainingSet.size() - trueMetadata));
    }
    
    private void prepareStatistics() {
        sampleMatches = new ArrayList<Double>();
        sampleNonMatches = new ArrayList<Double>();
        overheadTimes = new ArrayList[noOfClassifiers];
        resolutionTimes = new ArrayList[noOfClassifiers];
        sampleComparisons = new ArrayList[noOfClassifiers];
        sampleDuplicates = new ArrayList[noOfClassifiers];
        for (int i = 0; i < noOfClassifiers; i++) {
            overheadTimes[i] = new ArrayList<Double>();
            resolutionTimes[i] = new ArrayList<Double>();
            sampleComparisons[i] = new ArrayList<Double>();
            sampleDuplicates[i] = new ArrayList<Double>();
        }
    }
    
    public void printStatistics() {
        System.out.println("\n\n\n\n\n+++++++++++++++++++++++Printing overall statistics+++++++++++++++++++++++");
        
        double avSMatches = StatisticsUtilities.getMeanValue(sampleMatches);
        double avSNonMatches = StatisticsUtilities.getMeanValue(sampleNonMatches);
        System.out.println("Sample matches\t:\t" + avSMatches + "+-" + StatisticsUtilities.getStandardDeviation(avSMatches, sampleMatches));
        System.out.println("Sample non-matches\t:\t" + avSNonMatches + "+-" + StatisticsUtilities.getStandardDeviation(avSNonMatches, sampleNonMatches));
         
        for (int i = 0; i < overheadTimes.length; i++) {
            System.out.println("\n\n\n\n\nClassifier id\t:\t" + (i+1));
            double avOTime = StatisticsUtilities.getMeanValue(overheadTimes[i]);
            double avRTime = StatisticsUtilities.getMeanValue(resolutionTimes[i]);
            double avSEComparisons = StatisticsUtilities.getMeanValue(sampleComparisons[i]);
            double avSDuplicates = StatisticsUtilities.getMeanValue(sampleDuplicates[i]);

            final List<Double> pcs = new ArrayList<Double>();
            for (int j = 0; j < sampleMatches.size(); j++) {
                pcs.add(sampleDuplicates[i].get(j)/(duplicates.size() - sampleMatches.get(j))*100.0);
            }
            double avSPC = StatisticsUtilities.getMeanValue(pcs);

            System.out.println("Overhead time\t:\t" + avOTime + "+-" + StatisticsUtilities.getStandardDeviation(avOTime, overheadTimes[i]));
            System.out.println("Resolution time\t:\t" + avRTime + "+-" + StatisticsUtilities.getStandardDeviation(avRTime, resolutionTimes[i]));
            System.out.println("Sample duplicates\t:\t" + avSDuplicates + "+-" + StatisticsUtilities.getStandardDeviation(avSDuplicates, sampleDuplicates[i]));
            System.out.println("Sample PC\t:\t" + avSPC);
            System.out.println("Sample comparisons\t:\t" + avSEComparisons + "+-" + StatisticsUtilities.getStandardDeviation(avSEComparisons, sampleComparisons[i]));
        }
    }
}
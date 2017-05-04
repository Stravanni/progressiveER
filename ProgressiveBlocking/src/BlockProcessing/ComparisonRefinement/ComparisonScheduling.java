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

package BlockProcessing.ComparisonRefinement;

import Comparators.ComparisonUtilityComparator;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityIndex;
import BlockProcessing.AbstractEfficiencyMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gap2
 */

public class ComparisonScheduling extends AbstractEfficiencyMethod {

    private final boolean weighted;
    private final double a = 1.0;
    private final double b = 0.5;
    private final double c = 0.5;
    
    protected double[] comparisonsPerEntity;
    private final List<Comparison> comparisons;
    private EntityIndex entityIndex;
    
    public ComparisonScheduling(boolean wg) {
        super("Comparisons Scheduling");

        weighted = wg;
        comparisons = new ArrayList<Comparison>();
    }
    
    public ComparisonScheduling(boolean wg, EntityIndex ei) {
        super("Comparisons Scheduling");

        weighted = wg;
        comparisons = new ArrayList<Comparison>();
        entityIndex = ei;
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks, AbstractDuplicatePropagation dp) {
        if (entityIndex == null) {
            entityIndex = new EntityIndex(blocks);
        }
        
        setComparisons(blocks);
        setEntityFrequencies(blocks);
        setComparisonUtilities();

        double noOfComparisons = 0;
        for (Comparison comparison : comparisons) {
            if (!dp.isSuperfluous(comparison)) {
                noOfComparisons++;
            }
        }

        System.out.println("Detected duplicates\t:\t" + dp.getNoOfDuplicates());
        System.out.println("Executed comparisons\t:\t" + noOfComparisons);
    }

    private double getEntitiesSimilarity(Comparison comparison) {
        double commonBlocks = entityIndex.getTotalNoOfCommonBlocks(comparison);
        int noOfBlocks1 = entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0);
        int noOfBlocks2 = entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER()?1:0);
        return commonBlocks / (noOfBlocks1 + noOfBlocks2 - commonBlocks);
    }
    
    private void setComparisons(List<AbstractBlock> blocks) {
        for (AbstractBlock block : blocks) {
            comparisons.addAll(block.getComparisons());
        }

        System.out.println("Total Comparisons\t:\t" + comparisons.size());
    }

    private void setEntityFrequencies(List<AbstractBlock> blocks) {
        comparisonsPerEntity = new double[entityIndex.getNoOfEntities()];
        for (Comparison comparison : comparisons) {
            int entityId2 = comparison.getEntityId2()+entityIndex.getDatasetLimit();
            comparisonsPerEntity[comparison.getEntityId1()]++;
            comparisonsPerEntity[entityId2]++;
        }
    }

    private void setComparisonUtilities() {
        double noOfComparisons = comparisons.size();

        if (weighted) {
            double maxFreq1 = Double.MIN_VALUE;
            double maxFreq2 = Double.MIN_VALUE;
            for (Comparison comparison : comparisons) {
                double inverseCompFreq1 = Math.log(noOfComparisons / comparisonsPerEntity[comparison.getEntityId1()]);
                if (maxFreq1 < inverseCompFreq1) {
                    maxFreq1 = inverseCompFreq1;
                }

                int entityId2 = comparison.getEntityId2()+entityIndex.getDatasetLimit();
                double inverseCompFreq2 = Math.log(noOfComparisons / comparisonsPerEntity[entityId2]);
                if (maxFreq2 < inverseCompFreq2) {
                    maxFreq2 = inverseCompFreq2;
                }
            }

            for (Comparison comparison : comparisons) {
                int entityId2 = comparison.getEntityId2()+entityIndex.getDatasetLimit();
                double entitiesSim = comparison.getUtilityMeasure();
                double inverseCompFreq1 = Math.log(noOfComparisons / comparisonsPerEntity[comparison.getEntityId1()]);
                double inverseCompFreq2 = Math.log(noOfComparisons / comparisonsPerEntity[entityId2]);
                double utility = a*entitiesSim + b*inverseCompFreq1/maxFreq1 + c*inverseCompFreq2/maxFreq2;
                comparison.setUtilityMeasure(utility);
            }
        } else {
            for (Comparison comparison : comparisons) {
                int entityId2 = comparison.getEntityId2()+entityIndex.getDatasetLimit();
                double entitiesSimilarity = getEntitiesSimilarity(comparison);
                double inverseCompFreq1 = Math.log(noOfComparisons / comparisonsPerEntity[comparison.getEntityId1()]);
                double inverseCompFreq2 = Math.log(noOfComparisons / comparisonsPerEntity[entityId2]);
                double utility = entitiesSimilarity * inverseCompFreq1 * inverseCompFreq2;
                comparison.setUtilityMeasure(utility);
            }
        }

        Collections.sort(comparisons, new ComparisonUtilityComparator());
    }
}
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

package MetaBlocking;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.DecomposedBlock;
import DataStructures.EntityIndex;
import BlockProcessing.AbstractEfficiencyMethod;
import Utilities.ComparisonIterator;
import Utilities.Converter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gap2
 */
public abstract class AbstractMetablocking extends AbstractEfficiencyMethod {

    protected double blockAssingments;
    protected double totalBlocks;
    protected double validComparisons;
    protected double[] comparisonsPerBlock;
    protected double[] comparisonsPerEntity;

    protected EntityIndex entityIndex;
    protected WeightingScheme weightingScheme;

    public AbstractMetablocking(String description, WeightingScheme scheme) {
        super(description + " + " + scheme);
        weightingScheme = scheme;
    }

    protected DecomposedBlock getDecomposedBlock(boolean cleanCleanER, List<Integer> entities1, List<Integer> entities2) {
        int[] entityIds1 = Converter.convertCollectionToArray(entities1);
        int[] entityIds2 = Converter.convertCollectionToArray(entities2);
        return new DecomposedBlock(cleanCleanER, entityIds1, entityIds2);
    }

    protected DecomposedBlock getDecomposedBlock(boolean cleanCleanER, Collection<Comparison> comparisons) {
        int[] entityIds1 = new int[comparisons.size()];
        int[] entityIds2 = new int[comparisons.size()];
        
        int index = 0;
        Iterator<Comparison> iterator = comparisons.iterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            entityIds1[index] = comparison.getEntityId1();
            entityIds2[index] = comparison.getEntityId2();
            index++;
        }
        return new DecomposedBlock(cleanCleanER, entityIds1, entityIds2);
    }

    protected void getStatistics(List<AbstractBlock> blocks) {
        if (entityIndex == null) {
            entityIndex = new EntityIndex(blocks);
        }

        blockAssingments = 0;
        totalBlocks = blocks.size();
        comparisonsPerBlock = new double[(int) (totalBlocks + 1)];
        for (AbstractBlock block : blocks) {
            blockAssingments += block.getTotalBlockAssignments();
            comparisonsPerBlock[block.getBlockIndex()] = block.getNoOfComparisons();
        }

        if (weightingScheme.equals(WeightingScheme.EJS)) {
            validComparisons = 0;
            comparisonsPerEntity = new double[entityIndex.getNoOfEntities()];
            for (AbstractBlock block : blocks) {
                ComparisonIterator iterator = block.getComparisonIterator();
                while (iterator.hasNext()) {
                    Comparison comparison = iterator.next();
                    int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();
                    
                    if (!entityIndex.isRepeated(block.getBlockIndex(), comparison)) {
                        validComparisons++;
                        comparisonsPerEntity[comparison.getEntityId1()]++;
                        comparisonsPerEntity[entityId2]++;
                    }
                }
            }
        }
    }

    protected double getWeight(int blockIndex, Comparison comparison) {
        switch (weightingScheme) {
            case ARCS:
                final List<Integer> commonIndices = entityIndex.getCommonBlockIndices(blockIndex, comparison);
                if (commonIndices == null) {
                    return -1;
                }

                double totalWeight = 0;
                for (Integer index : commonIndices) {
                    totalWeight += 1.0 / comparisonsPerBlock[index];
                }
                return totalWeight;
            case CBS:
                return entityIndex.getNoOfCommonBlocks(blockIndex, comparison);
            case ECBS:
                double commonBlocks = entityIndex.getNoOfCommonBlocks(blockIndex, comparison);
                if (commonBlocks < 0) {
                    return commonBlocks;
                }
                return commonBlocks * Math.log10(totalBlocks / entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0)) * Math.log10(totalBlocks / entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0));
            case JS:
                double commonBlocksJS = entityIndex.getNoOfCommonBlocks(blockIndex, comparison);
                if (commonBlocksJS < 0) {
                    return commonBlocksJS;
                }
                return commonBlocksJS / (entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0) + entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0) - commonBlocksJS);
            case EJS:
                double commonBlocksEJS = entityIndex.getNoOfCommonBlocks(blockIndex, comparison);
                if (commonBlocksEJS < 0) {
                    return commonBlocksEJS;
                }

                double probability = commonBlocksEJS / (entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0) + entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0) - commonBlocksEJS);
                return probability * Math.log10(validComparisons / comparisonsPerEntity[comparison.getEntityId1()]) * Math.log10(validComparisons / comparisonsPerEntity[comparison.isCleanCleanER() ? comparison.getEntityId2() + entityIndex.getDatasetLimit() : comparison.getEntityId2()]);
        }

        return -1;
    }

    protected double getWeight(Comparison comparison) {
        switch (weightingScheme) {
            case ARCS:
                final List<Integer> commonIndices = entityIndex.getTotalCommonIndices(comparison);
                double totalWeight = 0;
                for (Integer index : commonIndices) {
                    totalWeight += 1.0 / comparisonsPerBlock[index];
                }
                return totalWeight;
            case CBS:
                return entityIndex.getTotalNoOfCommonBlocks(comparison);
            case ECBS:
                double commonBlocks = entityIndex.getTotalNoOfCommonBlocks(comparison);
                if (commonBlocks < 0) {
                    return commonBlocks;
                }
                return commonBlocks * Math.log10(totalBlocks / entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0)) * Math.log10(totalBlocks / entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0));
            case JS:
                double commonBlocksJS = entityIndex.getTotalNoOfCommonBlocks(comparison);
                if (commonBlocksJS < 0) {
                    return commonBlocksJS;
                }
                return commonBlocksJS / (entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0) + entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0) - commonBlocksJS);
            case EJS:
                double commonBlocksEJS = entityIndex.getTotalNoOfCommonBlocks(comparison);
                if (commonBlocksEJS < 0) {
                    return commonBlocksEJS;
                }

                double probability = commonBlocksEJS / (entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0) + entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0) - commonBlocksEJS);
                return probability * Math.log10(validComparisons / comparisonsPerEntity[comparison.getEntityId1()]) * Math.log10(validComparisons / comparisonsPerEntity[comparison.isCleanCleanER() ? comparison.getEntityId2() + entityIndex.getDatasetLimit() : comparison.getEntityId2()]);
        }

        return -1;
    }

    public void resetEntityIndex() {
        entityIndex = null;
    }

    public void setWeightingScheme(WeightingScheme wScheme) {
        weightingScheme = wScheme;
    }
}

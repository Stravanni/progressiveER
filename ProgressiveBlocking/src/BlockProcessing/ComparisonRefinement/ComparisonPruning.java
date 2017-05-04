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

import DataStructures.*;
import BlockProcessing.AbstractEfficiencyMethod;
import Utilities.ComparisonIterator;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class ComparisonPruning extends AbstractEfficiencyMethod {
    
    private final static double A = 0.20;
    
    private double minEntitiesSimilarity;
    private EntityIndex entityIndex;

    public ComparisonPruning() {
        super("Comparisons Pruning");
    }

    public ComparisonPruning(EntityIndex eIndex) {
        super("Comparisons Pruning");
        entityIndex = eIndex;
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        minEntitiesSimilarity = getEntitiesSimilarityThreshold(blocks);
        if (entityIndex == null) {
            entityIndex = new EntityIndex(blocks);
        }
        
        double totalComparisons = 0;
        final List<AbstractBlock> newBlocks = new ArrayList<>();
        for (AbstractBlock block : blocks) {
            final List<Integer> entities1 = new ArrayList<>();
            final List<Integer> entities2 = new ArrayList<>();
            
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (!isNonMatching(block.getBlockIndex(), comparison)) {
                    totalComparisons++;
                    entities1.add(comparison.getEntityId1());
                    entities2.add(comparison.getEntityId2());
                }
            }
            
            int[] entityIds1 = Converter.convertCollectionToArray(entities1);
            int[] entityIds2 = Converter.convertCollectionToArray(entities2);
            boolean cleanCleanER = block instanceof BilateralBlock;
            newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
        }
        blocks.clear();
        blocks.addAll(newBlocks);
        
        System.out.println("Comparisons after filtering\t:\t" + totalComparisons);
    }

    private double getBilateralEntitySimThreshold(List<AbstractBlock> blocks) {
        double d1BlockAssignments = 0;
        double d2BlockAssignments = 0;
        final Set<Integer> d1Entities = new HashSet<>();
        final Set<Integer> d2Entities = new HashSet<>();
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            d1BlockAssignments += bilBlock.getIndex1Entities().length;
            d2BlockAssignments += bilBlock.getIndex2Entities().length;

            for (int id1 : bilBlock.getIndex1Entities()) {
                d1Entities.add(id1);
            }

            for (int id2 : bilBlock.getIndex2Entities()) {
                d2Entities.add(id2);
            }
        }

        double iBC1 = d1BlockAssignments / d1Entities.size();
        double iBC2 = d2BlockAssignments / d2Entities.size();
        return A * Math.min(iBC1, iBC2) / (iBC1 + iBC2 - A * Math.min(iBC1, iBC2));
    }
    
    private double getEntitiesSimilarityThreshold(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            return getBilateralEntitySimThreshold(blocks);
        } else {
            return getUnilateralEntitySimThreshold(blocks);
        }
    }
    
    private double getUnilateralEntitySimThreshold(List<AbstractBlock> blocks) {
        double blockAssignments = 0;
        final Set<Integer> entities = new HashSet<>();
        for (AbstractBlock block : blocks) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            blockAssignments += uniBlock.getTotalBlockAssignments();

            for (int id : uniBlock.getEntities()) {
                entities.add(id);
            }
        }

        double bc = blockAssignments / entities.size();
        return A * bc / ((2-A)*bc);
    }

    public boolean isNonMatching(int blockIndex, Comparison comparison) {
        double commonBlocks = entityIndex.getNoOfCommonBlocks(blockIndex, comparison);
        if (commonBlocks < 0) {
            return true;
        }

        int noOfBlocks1 = entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0);
        int noOfBlocks2 = entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER()?1:0);
        double entitiesSimilarity = commonBlocks / (noOfBlocks1 + noOfBlocks2 - commonBlocks);
        if (minEntitiesSimilarity < entitiesSimilarity) {
            comparison.setUtilityMeasure(entitiesSimilarity);
            return false;
        }

        return true;
    }
}
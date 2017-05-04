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
package BlockBuilding.Progressive.ProgressiveMetaBlocking;

import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import Comparators.ComparisonWeightComparatorMax;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import MetaBlocking.EnhancedMetaBlocking.FastImplementations.RedundancyCardinalityNodePruning;
import MetaBlocking.FastImplementations.CardinalityEdgePruning;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.*;

/**
 * @author giovanni
 */
public class CepRedudancyCnp extends RedundancyCardinalityNodePruning implements Iterator<Comparison>, AbstractProgressiveMetaBlocking {

    protected int counter;
    protected int firstId;
    protected int lastId;

    protected Queue<Comparison> topComparisons;
    protected List<AbstractBlock> blocks;


    public CepRedudancyCnp(WeightingScheme scheme) {
        super("CEP+CNP", scheme);
        counter = 0;
        nodeCentric = true;
    }


    @Override
    public boolean hasNext() {
        return !topComparisons.isEmpty();
    }

    @Override
    public Comparison next() {
        return topComparisons.poll();
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        nearestEntities = new Set[noOfEntities];
        //topKEdges = new PriorityQueue<Comparison>((int) (2 * threshold), new MetablockingComparator());
        topKEdges = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparatorMax())
                .maximumSize((int) threshold)
                .create();
        topComparisons = MinMaxPriorityQueue.orderedBy(new ComparisonWeightComparatorMax())
                .maximumSize((int) threshold * noOfEntities)
                .create();

        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < noOfEntities; i++) {
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                processEntity(i);
                verifyValidEntities(i);
            }
        }
        retainValidComparisons(newBlocks);
    }

    @Override
    protected void retainValidComparisons(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < noOfEntities; i++) {
            if (nearestEntities[i] != null) {
                for (Comparison comparison : nearestEntities[i]) {
                    if (isValidComparison(i, comparison)) {
                        topComparisons.offer(comparison);
                    }
                }
            }
        }
    }
    @Override
    protected void verifyValidEntities(int entityId) {
        if (validEntities.isEmpty()) {
            return;
        }

        topKEdges.clear();
        minimumWeight = Double.MIN_VALUE;
        for (int neighborId : validEntities) {
            double weight = getWeight(entityId, neighborId);
//            if (weight < minimumWeight) {
//                continue;
//            }

            Comparison comparison = getComparison(entityId, neighborId);
            comparison.setUtilityMeasure(weight);

            topKEdges.offer(comparison);
        }
        nearestEntities[entityId] = new HashSet<>(topKEdges);
    }

    @Override
    public String getName(){
        return "CepCepRedundancy";
    }
}
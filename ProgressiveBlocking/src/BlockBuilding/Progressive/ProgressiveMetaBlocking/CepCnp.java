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
import BlockBuilding.Progressive.DataStructures.InverseMetablockingComparator;
import BlockBuilding.Progressive.DataStructures.MetablockingComparator;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import MetaBlocking.FastImplementations.*;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import MetaBlocking.WeightingScheme;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author gap2
 * @author giovanni
 */
public class CepCnp extends CardinalityEdgePruning implements Iterator<Comparison>, AbstractProgressiveMetaBlocking {

    protected final static double FILTER_RATIO = 0.80;
    protected final static double SMOOTH_FACTOR = 1.005;

    protected int counter;
    protected int firstId;
    protected int lastId;

    protected int fixedThreshold;

    protected Comparison[] sortedTopComparisons;
    protected final Set<Comparison> topComparisons;
    protected List<AbstractBlock> blocks;

    public CepCnp(List<EntityProfile>[] profiles, WeightingScheme scheme) {
        super("CEP+CNP", scheme);
        counter = 0;
        nodeCentric = true;
        topComparisons = new HashSet<>();
        this.fixedThreshold = 0;

        buildBlocks(profiles);
        this.applyProcessing(blocks);
    }

    public CepCnp(WeightingScheme scheme) {
        super("CEP+CNP", scheme);
        counter = 0;
        nodeCentric = true;
        topComparisons = new HashSet<>();
        this.fixedThreshold = 0;
    }

    public CepCnp(WeightingScheme scheme, int fixedThreshold) {
        super("CEP+CNP", scheme);
        counter = 0;
        nodeCentric = true;
        topComparisons = new HashSet<>();
        this.fixedThreshold = fixedThreshold;
    }

    private void buildBlocks(List<EntityProfile>[] profiles) {
        TokenBlocking tb = new TokenBlocking(profiles);
        blocks = tb.buildBlocks();

        ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTH_FACTOR);
        cbbp.applyProcessing(blocks);

        BlockFiltering bf = new BlockFiltering(FILTER_RATIO);
        bf.applyProcessing(blocks);
    }

    @Override
    public boolean hasNext() {
        return counter < sortedTopComparisons.length;
    }

    @Override
    public Comparison next() {
        return sortedTopComparisons[counter++];
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        setLimits();
        //topKEdges = new PriorityQueue<>((int) (2 * threshold), new MetablockingComparator());
        topKEdges = MinMaxPriorityQueue.orderedBy(new InverseMetablockingComparator()).maximumSize((int) (threshold)).create();
        /*topKEdges = MinMaxPriorityQueue.orderedBy(new MetablockingComparator()).create();*/

        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = firstId; i < lastId; i++) {
                minimumWeight = Double.MIN_VALUE;
                topKEdges.clear();
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = firstId; i < lastId; i++) {
                minimumWeight = Double.MIN_VALUE;
                topKEdges.clear();
                processEntity(i);
                verifyValidEntities(i);
            }
        }
        // topComparison has no duplicated comparisons
        List<Comparison> sortedComparisons = new ArrayList<>(topComparisons);
        topComparisons.clear();
        // sort
        Collections.sort(sortedComparisons, new InverseMetablockingComparator());

        sortedTopComparisons = sortedComparisons.toArray(new Comparison[sortedComparisons.size()]);
        System.out.println(sortedTopComparisons.length + " comparisons ready!");
    }

    protected void setLimits() {
        firstId = 0;
        lastId = noOfEntities;
    }

    @Override
    protected void setThreshold() {
        threshold = this.fixedThreshold != 0 ?
                this.fixedThreshold
                : 10 * Math.max(1, blockAssingments / noOfEntities);
        System.out.println("Threshold: " + threshold);
    }

    @Override
    protected void verifyValidEntities(int entityId) {
        for (int neighborId : validEntities) {
            double weight = getWeight(entityId, neighborId);
            if (weight < minimumWeight) {
                continue;
            }

            Comparison comparison = getComparison(entityId, neighborId);
            comparison.setUtilityMeasure(weight);

            topKEdges.offer(comparison);
            // Using MinMaxPriorityQueue from Guava the following is not needed anymore:
            /*topKEdges.add(comparison);
            if (threshold < topKEdges.size()) {
                Comparison lastComparison = topKEdges.poll();
                minimumWeight = lastComparison.getUtilityMeasure();
            }*/
        }
        topComparisons.addAll(topKEdges);
    }

    @Override
    public String getName() {
        return threshold != 0 ? "CepCnpFixed" + threshold : "CepCnp";
    }

    public void setThreshold(int t) {
        this.fixedThreshold = t;
    }
}


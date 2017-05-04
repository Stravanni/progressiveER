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
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import Comparators.BlockCardinalityComparator;
import MetaBlocking.*;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import Utilities.ComparisonIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author gap2
 * @author giovanni
 */
public class CepBlockScheduling extends AbstractMetablocking implements Iterator<Comparison>, AbstractProgressiveMetaBlocking {

    protected final static double FILTER_RATIO = 0.80;
    protected final static double SMOOTH_FACTOR = 1.005;

    protected int blockCounter;
    protected long kThreshold;

    protected AbstractBlock[] blocksArray;
    protected Queue<Comparison> topComparisons;
    protected List<AbstractBlock> blocks;

    public CepBlockScheduling(List<EntityProfile>[] profiles, WeightingScheme scheme) {
        super("CEP+BLOCK", scheme);
        blockCounter = 0;
        topComparisons = new PriorityQueue<>(new InverseMetablockingComparator());

        buildBlocks(profiles);
        this.applyProcessing(blocks);
    }

    public CepBlockScheduling(WeightingScheme scheme) {
        super("CEP+BLOCK", scheme);
        blockCounter = 0;
        topComparisons = new PriorityQueue<>(new InverseMetablockingComparator());
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        //order blocks from smallest to largest one
        Collections.sort(blocks, new BlockCardinalityComparator());
        blocksArray = blocks.toArray(new AbstractBlock[blocks.size()]);

        getStatistics(blocks);
    }

    private void buildBlocks(List<EntityProfile>[] profiles) {
        TokenBlocking tb = new TokenBlocking(profiles);
        blocks = tb.buildBlocks();

        ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTH_FACTOR);
        cbbp.applyProcessing(blocks);

        BlockFiltering bf = new BlockFiltering(FILTER_RATIO);
        bf.applyProcessing(blocks);
    }

    protected void filterComparisons(AbstractBlock block) {
        topComparisons.clear();
        ComparisonIterator iterator = block.getComparisonIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            double weight = getWeight(blockCounter, comparison);
            if (weight < 0) {
                continue;
            }
            comparison.setUtilityMeasure(weight);
            topComparisons.add(comparison);
        }
        blockCounter++;
    }

    @Override
    public boolean hasNext() {
        while (topComparisons.isEmpty() && blockCounter < blocksArray.length) {
            filterComparisons(blocksArray[blockCounter]);
        }
        return blockCounter < blocksArray.length || !topComparisons.isEmpty();
    }

    @Override
    public Comparison next() {
        while (topComparisons.isEmpty()) {
            filterComparisons(blocksArray[blockCounter]);
        }
        return topComparisons.poll();
    }

    @Override
    public String getName() {
        return "CepBlcok";
    }
}

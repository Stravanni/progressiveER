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

package BlockProcessing.BlockRefinement;

import Comparators.BlockCardinalityComparator;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.UnilateralBlock;
import BlockProcessing.AbstractEfficiencyMethod;
import Utilities.Converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gap2
 */
public class BlockFiltering extends AbstractEfficiencyMethod {

    protected final double ratio;

    protected int entitiesD1;
    protected int entitiesD2;
    protected int[] counterD1;
    protected int[] counterD2;
    protected int[] limitsD1;
    protected int[] limitsD2;

    private List<AbstractBlock> reserveBlocks;
    private Boolean retainDiscarted;

    public BlockFiltering(double r) {
        this(r, "Block Filtering");
        retainDiscarted = false;
    }

    public BlockFiltering(double r, Boolean reserve) {
        this(r, "Block Filtering with reserve blocks");
        reserveBlocks = new ArrayList<>();
        retainDiscarted = reserve;
    }

    public BlockFiltering(double r, String description) {
        super(description);
        reserveBlocks = new ArrayList<>();
        ratio = r;
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        countEntities(blocks);
        sortBlocks(blocks);
        getLimits(blocks);
        initializeCounters();
        restructureBlocks(blocks);
    }

    protected void countEntities(List<AbstractBlock> blocks) {
        entitiesD1 = Integer.MIN_VALUE;
        entitiesD2 = Integer.MIN_VALUE;
        if (blocks.get(0) instanceof BilateralBlock) {
            for (AbstractBlock block : blocks) {
                BilateralBlock bilBlock = (BilateralBlock) block;
                for (int id1 : bilBlock.getIndex1Entities()) {
                    if (entitiesD1 < id1 + 1) {
                        entitiesD1 = id1 + 1;
                    }
                }
                for (int id2 : bilBlock.getIndex2Entities()) {
                    if (entitiesD2 < id2 + 1) {
                        entitiesD2 = id2 + 1;
                    }
                }
            }
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            for (AbstractBlock block : blocks) {
                UnilateralBlock uniBlock = (UnilateralBlock) block;
                for (int id : uniBlock.getEntities()) {
                    if (entitiesD1 < id + 1) {
                        entitiesD1 = id + 1;
                    }
                }
            }
        }
    }

    protected void getBilateralLimits(List<AbstractBlock> blocks) {
        limitsD1 = new int[entitiesD1];
        limitsD2 = new int[entitiesD2];
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                limitsD1[id1]++;
            }
            for (int id2 : bilBlock.getIndex2Entities()) {
                limitsD2[id2]++;
            }
        }

        for (int i = 0; i < limitsD1.length; i++) {
            limitsD1[i] = (int) Math.round(ratio * limitsD1[i]);
        }
        for (int i = 0; i < limitsD2.length; i++) {
            limitsD2[i] = (int) Math.round(ratio * limitsD2[i]);
        }
    }

    protected void getLimits(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            getBilateralLimits(blocks);
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            getUnilateralLimits(blocks);
        }
    }

    protected void getUnilateralLimits(List<AbstractBlock> blocks) {
        limitsD1 = new int[entitiesD1];
        limitsD2 = null;
        for (AbstractBlock block : blocks) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            for (int id : uniBlock.getEntities()) {
                limitsD1[id]++;
            }
        }

        for (int i = 0; i < limitsD1.length; i++) {
            limitsD1[i] = (int) Math.round(ratio * limitsD1[i]);
        }
    }

    protected void initializeCounters() {
        counterD1 = new int[entitiesD1];
        counterD2 = null;
        if (0 < entitiesD2) {
            counterD2 = new int[entitiesD2];
        }
    }

    protected void restructureBilateraBlocks(List<AbstractBlock> blocks) {
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock block : blocks) {
            /*System.out.println("block size: " + block.getNoOfComparisons());*/
            BilateralBlock oldBlock = (BilateralBlock) block;
            final List<Integer> retainedEntitiesD1 = new ArrayList<Integer>();
            final List<Integer> discardedEntitiesD1 = new ArrayList<Integer>();
            for (int entityId : oldBlock.getIndex1Entities()) {
                if (counterD1[entityId] < limitsD1[entityId]) {
                    retainedEntitiesD1.add(entityId);
                } else {
                    discardedEntitiesD1.add(entityId);
                }
            }

            final List<Integer> retainedEntitiesD2 = new ArrayList<Integer>();
            final List<Integer> discardedEntitiesD2 = new ArrayList<Integer>();
            for (int entityId : oldBlock.getIndex2Entities()) {
                if (counterD2[entityId] < limitsD2[entityId]) {
                    retainedEntitiesD2.add(entityId);
                } else {
                    discardedEntitiesD2.add(entityId);
                }
            }

            int[] blockEntitiesD1 = Converter.convertCollectionToArray(retainedEntitiesD1);
            int[] blockEntitiesD2 = Converter.convertCollectionToArray(retainedEntitiesD2);
            if (!retainedEntitiesD1.isEmpty() && !retainedEntitiesD2.isEmpty()) {
                for (int entityId : blockEntitiesD1) {
                    counterD1[entityId]++;
                }
                for (int entityId : blockEntitiesD2) {
                    counterD2[entityId]++;
                }
                newBlocks.add(new BilateralBlock(blockEntitiesD1, blockEntitiesD2));
            }

            if (retainDiscarted) {
                int[] blockEntitiesD1_ = Converter.convertCollectionToArray(discardedEntitiesD1);
                int[] blockEntitiesD2_ = Converter.convertCollectionToArray(discardedEntitiesD2);
                if (!discardedEntitiesD1.isEmpty() && !discardedEntitiesD2.isEmpty()) {
                    reserveBlocks.add(new BilateralBlock(blockEntitiesD1_, blockEntitiesD2_));
                } else if (!discardedEntitiesD1.isEmpty() && discardedEntitiesD2.isEmpty()) {
                    reserveBlocks.add(new BilateralBlock(blockEntitiesD1_, blockEntitiesD2));
                } else if (discardedEntitiesD1.isEmpty() && !discardedEntitiesD2.isEmpty()) {
                    reserveBlocks.add(new BilateralBlock(blockEntitiesD1, blockEntitiesD2_));
                }
            }
        }
        blocks.clear();
        blocks.addAll(newBlocks);
        /*for (AbstractBlock bb : blocks) {
            System.out.println("blocks size: " + bb.getAggregateCardinality());
        }*/
    }

    protected void restructureBlocks(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            restructureBilateraBlocks(blocks);
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            restructureUnilateraBlocks(blocks);
        }
    }

    protected void restructureUnilateraBlocks(List<AbstractBlock> blocks) {
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock block : blocks) {
            UnilateralBlock oldBlock = (UnilateralBlock) block;
            final List<Integer> retainedEntities = new ArrayList<Integer>();
            final List<Integer> discardedEntities = new ArrayList<Integer>();
            for (int entityId : oldBlock.getEntities()) {
                if (counterD1[entityId] < limitsD1[entityId]) {
                    retainedEntities.add(entityId);
                } else {
                    discardedEntities.add(entityId);
                }
            }

            if (1 < retainedEntities.size()) {
                int[] blockEntities = Converter.convertCollectionToArray(retainedEntities);
                for (int entityId : blockEntities) {
                    counterD1[entityId]++;
                }
                newBlocks.add(new UnilateralBlock(blockEntities));
            }

            if (retainDiscarted) {
                if (1 < discardedEntities.size()) {
                    int[] blockEntities = Converter.convertCollectionToArray(discardedEntities);
                    reserveBlocks.add(new UnilateralBlock(blockEntities));
                }
            }
        }
        blocks.clear();
        blocks.addAll(newBlocks);
    }

    protected void sortBlocks(List<AbstractBlock> blocks) {
        Collections.sort(blocks, new BlockCardinalityComparator());
    }

    public List<AbstractBlock> getReserveBlocks() {
        return reserveBlocks;
    }
}

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

package BlockProcessing.IterativeMethods;

import ExhaustiveMethods.UnilateralRSwoosh;
import Comparators.BlockCardinalityComparator;
import DataStructures.AbstractBlock;
import DataStructures.EntityIndex;
import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;
import DataStructures.UnilateralBlock;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import Utilities.Converter;
import Utilities.ExportBlocks;
import Utilities.ProfileComparison;
import Utilities.SerializationUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */
public class UnilateralIterativeBlockingWithInstanceMatching {

    private boolean[] inQueue;

    private List<UnilateralBlock> blocksQueue;
    private EntityIndex entityIndex;
    private final Set<IdDuplicates> groundTruth;
    private HashSet<Integer>[] maxRecords;
    private Set<String>[] maxProfiles;
    private Set<HashSet<Integer>>[] previousCondition;
    private UnilateralRSwoosh rswoosh;
    private UnilateralBlock[] blocks;

    public UnilateralIterativeBlockingWithInstanceMatching(List<AbstractBlock> initialBlocks,
            Set<IdDuplicates> duplicates,
            String entityPath) {
        groundTruth = duplicates;
        System.out.println("Block queue size\t:\t" + initialBlocks.size());
        System.out.println("Pair-wise matches in ground-truth\t:\t" + groundTruth.size());

        prepareDataStructures(initialBlocks, entityPath);
    }

    public void applyProcessing() {
        System.out.println("\n\nApplying processing...");

        int iterationIndex = 0;
        long startingTime = System.currentTimeMillis();
        while (!blocksQueue.isEmpty()) {
            iterationIndex++;

            UnilateralBlock currentBlock = (UnilateralBlock) blocksQueue.remove(0);
            inQueue[currentBlock.getBlockIndex()] = false;

            Set<HashSet<Integer>> matches = null;
            if (previousCondition[currentBlock.getBlockIndex()] == null) {
                Set<HashSet<Integer>> blockEntities = updateBlock(currentBlock);
                matches = rswoosh.applyProcessing(blockEntities);
                matches.removeAll(blockEntities);
            } else {
                Set<HashSet<Integer>> updatedEntities = updateBlock(currentBlock);
                updatedEntities.removeAll(previousCondition[currentBlock.getBlockIndex()]);

                Set<HashSet<Integer>> cleanEntities = updateBlock(currentBlock);
                cleanEntities.removeAll(updatedEntities);

                matches = rswoosh.applyPartialProcessing(cleanEntities, updatedEntities);
                matches.removeAll(cleanEntities);
                matches.removeAll(updatedEntities);
            }
            previousCondition[currentBlock.getBlockIndex()] = updateBlock(currentBlock);

            for (HashSet<Integer> eqCluster : matches) {
                final Set<String> totalProfile = getTotalProfile(eqCluster);
                for (Integer entityId : eqCluster) {
                    maxRecords[entityId] = eqCluster;
                    maxProfiles[entityId] = totalProfile;
                }

                final Set<Integer> recheckedBlocks = getRecheckedBlocks(eqCluster);
                for (Integer blockId : recheckedBlocks) {
                    if (blockId != currentBlock.getBlockIndex() && !inQueue[blockId]) {
                        blocksQueue.add(blocks[blockId]);
                        inQueue[blockId] = true;
                    }
                }
            }
        }
        long endingTime = System.currentTimeMillis();

        System.out.println("Iterations\t:\t" + iterationIndex);
        System.out.println("Detected duplicates\t:\t" + getDetectedDuplicates());
        System.out.println("Executed comparisons\t:\t" + rswoosh.getComparisons());
        System.out.println("Response time\t:\t" + (endingTime - startingTime));
    }

    private int getDetectedDuplicates() {
        Set<IdDuplicates> detectedDuplicates = new HashSet<IdDuplicates>();
        for (int i = 0; i < entityIndex.getNoOfEntities(); i++) {
            int[] equCluster = Converter.convertCollectionToArray(maxRecords[i]);
            for (int j = 0; j < equCluster.length; j++) {
                for (int k = j + 1; k < equCluster.length; k++) {
                    if (equCluster[j] < equCluster[k]) {
                        detectedDuplicates.add(new IdDuplicates(equCluster[j], equCluster[k]));
                    } else {
                        detectedDuplicates.add(new IdDuplicates(equCluster[k], equCluster[j]));
                    }
                }
            }
        }
        return detectedDuplicates.size();
    }

    private Set<String> getTotalProfile(Set<Integer> equivalenceCluster) {
        final Set<String> totalProfile = new HashSet<String>(100 * equivalenceCluster.size());
        for (Integer entityId : equivalenceCluster) {
            totalProfile.addAll(maxProfiles[entityId]);
        }
        return totalProfile;
    }

    private Set<Integer> getRecheckedBlocks(Set<Integer> entityIds) {
        final Set<Integer> blockIds = new HashSet<Integer>(10 * entityIds.size());
        for (Integer entityId : entityIds) {
            int[] blocks = entityIndex.getEntityBlocks(entityId, 0);
            for (int blockId : blocks) {
                blockIds.add(blockId);
            }
        }
        return blockIds;
    }

    private void prepareDataStructures(List<AbstractBlock> initialBlocks, String entitiesPath) {
        System.out.println("\n\nPreparing data structures...");

        entityIndex = new EntityIndex(initialBlocks);

        blocks = new UnilateralBlock[initialBlocks.size()];
        blocksQueue = new ArrayList<UnilateralBlock>();
        inQueue = new boolean[initialBlocks.size()];
        previousCondition = new HashSet[initialBlocks.size()];
        for (AbstractBlock block : initialBlocks) {
            blocks[block.getBlockIndex()] = (UnilateralBlock) block;
            blocksQueue.add((UnilateralBlock) block);
            inQueue[block.getBlockIndex()] = true;
            previousCondition[block.getBlockIndex()] = null;
        }

        //determine the processing order of blocks
        Collections.sort(blocksQueue, new BlockCardinalityComparator());

        List<EntityProfile> profiles = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(entitiesPath);
        int noOfEntities = entityIndex.getNoOfEntities();
        maxRecords = new HashSet[noOfEntities];
        maxProfiles = new HashSet[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            maxRecords[i] = new HashSet<Integer>();
            maxRecords[i].add(i);

            maxProfiles[i] = new HashSet<String>();
            maxProfiles[i].addAll(ProfileComparison.getDistinctTokens(profiles.get(i).getAttributes()));
        }
        rswoosh = new UnilateralRSwoosh(groundTruth, maxProfiles);
        System.out.println("Data structures were successfully prepared!");
    }

    private Set<HashSet<Integer>> updateBlock(UnilateralBlock uBlock) {
        final Set<HashSet<Integer>> blockEntities = new HashSet<HashSet<Integer>>(2 * uBlock.getEntities().length);
        for (int entityId : uBlock.getEntities()) {
            blockEntities.add(maxRecords[entityId]);
        }

        return blockEntities;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String mainDirectory = "/home/gpapadis/data/";
        String[] datasets = {"10K", "50K", "100K", "200K", "300K", "1M", "2M"};

        for (int j = 5; j < datasets.length; j++) {
            String dataset = datasets[j];
            System.out.println("\n\n\n\n\nCurrent dataset\t:\t" + dataset);
            String[] indexDir = {mainDirectory + "indices/" + dataset + "synthetic"};
            String groundTruthPath = mainDirectory + "groundtruth/" + dataset + "IdDuplicates";
            String entitiesPath = mainDirectory + "profiles/" + dataset + "profiles";

            for (int i = 0; i < 10; i++) {
                ExportBlocks exportBlocks = new ExportBlocks(indexDir);
                List<AbstractBlock> blocks = exportBlocks.getBlocks();
                System.out.println("Blocks\t:\t" + blocks.size());

                ComparisonsBasedBlockPurging blockPurging = new ComparisonsBasedBlockPurging(1.005);
                blockPurging.applyProcessing(blocks);

                Set<IdDuplicates> duplicates = (HashSet<IdDuplicates>) SerializationUtilities.loadSerializedObject(groundTruthPath);

                UnilateralIterativeBlockingWithInstanceMatching uniItBlocking = new UnilateralIterativeBlockingWithInstanceMatching(blocks, duplicates, entitiesPath);
                uniItBlocking.applyProcessing();
//            UnilateralIterativeBlocking uniItBlocking = new UnilateralIterativeBlocking(blocks,  duplicates);
//            uniItBlocking.applyProcessing();
            }
        }
    }
}

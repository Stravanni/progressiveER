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

package Utilities;

import DataStructures.AbstractBlock;
import BlockBuilding.MemoryBased.AttributeClusteringBlocking;
import BlockBuilding.MemoryBased.TokenBlocking;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author gap2
 */

public class BlocksDistribution implements Constants {
    
    private final static int NO_OF_SLOTS = 14;
    
    private double[] frequencies;
    private final List<AbstractBlock> blocks;
    
    public BlocksDistribution(List<AbstractBlock> blocks) {
        this.blocks = blocks;
        System.out.println("Total blocks\t:\t" + blocks.size());
    }
    
    public void getDistribution() {
        getAbsoluteFrequencies();
        normalizeFrequencies();
        printFrequencies();
    }
    
    private void getAbsoluteFrequencies() {
        frequencies = new double[NO_OF_SLOTS];
        for (AbstractBlock block : blocks) {
            double comparisons = block.getNoOfComparisons();
            int index = (int) Math.log10(comparisons);
            if (index < 0) {
                index = 0;
            }
            frequencies[index]++;
        }
    }
    
    private void printFrequencies() {
        System.out.println("\n\nPrinting frequencies...");
        for (int i = 0; i < NO_OF_SLOTS; i++) {
            System.out.println(fourDigitsDouble.format(frequencies[i]));
        }
    }
    
    private void normalizeFrequencies() {
        for (int i = 0; i < NO_OF_SLOTS; i++) {
            frequencies[i] = Math.log10(frequencies[i]);
            if (frequencies[i] < 0) {
                frequencies[i] = 0;
            } 
        }
    }
    
    public static void main (String[] args) throws IOException, Exception {
        String mainDirectory = "/home/gpapadis/data/profiles/";
        String[] entitiesPaths = { mainDirectory+"dbpediaMoviesUPD", mainDirectory+"imdbMoviesUPD" };
        String stopWordsPath = mainDirectory + "stopword-list.txt";
        
//        TokenBlocking imtb = new TokenBlocking(entitiesPaths);
//        List<AbstractBlock> blocks = imtb.buildBlocks();
//        
//        BlocksDistribution bds = new BlocksDistribution(blocks);
//        bds.getDistribution();
//        
//        AttributeClusteringBlocking imac = new AttributeClusteringBlocking(RepresentationModel.TOKEN_UNIGRAMS, entitiesPaths);
//        blocks = imac.buildBlocks();
//        
//        bds = new BlocksDistribution(blocks);
//        bds.getDistribution();
        
//        CanopyClustering imcc = new CanopyClustering(0.05, 0.55, 3, entitiesPaths);
//        blocks = imcc.buildBlocks();
//        
//        bds = new BlocksDistribution(blocks);
//        bds.getDistribution();
//        
//        ExtendedCanopyClustering imccwnn = new ExtendedCanopyClustering(1, 19, 3, entitiesPaths);
//        blocks = imccwnn.buildBlocks();
//        
//        bds = new BlocksDistribution(blocks);
//        bds.getDistribution();
        
        final List<String> stopWords = FileUtilities.getFileLines(stopWordsPath);
        final HashSet<String> stopWordsList = new HashSet<String>(stopWords);
        System.out.println("Stop words\t:\t" + stopWordsList.size());
//        
//        TYPiMatch typm = new TYPiMatch(0.15, 0.40, stopWordsList, entitiesPaths);
//        blocks = typm.buildBlocks();
        
//        bds = new BlocksDistribution(blocks);
//        bds.getDistribution();
    }
}
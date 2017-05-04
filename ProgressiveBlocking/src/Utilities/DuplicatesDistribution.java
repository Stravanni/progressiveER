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
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.BilateralDuplicatePropagation;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author gap2
 */

public class DuplicatesDistribution implements Constants {
    
    private final static int NO_OF_SLOTS = 14;
    
    private final int noOfMatches;
    private double[] frequencies;
    private final AbstractDuplicatePropagation duplicatePropagation;
    private final List<AbstractBlock> blocks;
    
    public DuplicatesDistribution(AbstractDuplicatePropagation abp, List<AbstractBlock> blocks) {
        this.blocks = blocks;
        System.out.println("Total blocks\t:\t" + blocks.size());
        duplicatePropagation = abp;
        duplicatePropagation.resetDuplicates();
        noOfMatches = duplicatePropagation.getExistingDuplicates();
        System.out.println("Original matches\t:\t" + noOfMatches);
    }
    
    public void getDistribution() {
        getAbsoluteFrequencies();
        normalizeFrequencies();
        printFrequencies();
    }
    
    private void getAbsoluteFrequencies() {
        frequencies = new double[NO_OF_SLOTS];
        for (AbstractBlock block : blocks) {
            int existingMatches = duplicatePropagation.getNoOfDuplicates();
            block.processBlock(duplicatePropagation);
            int newMatches = duplicatePropagation.getNoOfDuplicates()-existingMatches;
            if (newMatches < 1) {
                continue;
            }
            
            double comparisons = block.getNoOfComparisons();
            int index = (int) Math.log10(comparisons);
            if (index < 0) {
                index = 0;
            }
            frequencies[index]+=newMatches;
        }
        System.out.println("Total detected duplicates\t:\t" + duplicatePropagation.getNoOfDuplicates());
    }
    
    private void printFrequencies() {
        System.out.println("\n\nPrinting frequencies...");
        for (int i = 0; i < NO_OF_SLOTS; i++) {
            System.out.println(fourDigitsDouble.format(frequencies[i]));
        }
    }
    
    private void normalizeFrequencies() {
        for (int i = 0; i < NO_OF_SLOTS; i++) {
            frequencies[i] = frequencies[i]*100/noOfMatches;
        }
    }
    
    public static void main (String[] args) throws IOException, Exception {
        String mainDirectory = "/home/gpapadakis/data/CCERdata/dbpedia/";
        String gtDirectory = mainDirectory+"groundtruth";
        String[] indexPaths = { mainDirectory+"newIndex1", mainDirectory+"newIndex2" };
        AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(gtDirectory);
        
        ExportBlocks eb = new ExportBlocks(indexPaths);
        List<AbstractBlock> blocks = eb.getBlocks();
        
        DuplicatesDistribution dudi = new DuplicatesDistribution(adp, blocks);
        dudi.getDistribution();
    }
}
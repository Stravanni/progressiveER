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

import DataStructures.*;

import java.util.Iterator;

/**
 * @author gap2
 */

public class ComparisonIterator implements Iterator<Comparison> {

    private double executedComparisons;
    private final double totalComparisons;

    private int innerLoop;
    private int innerLimit;
    private int outerLoop;
    private int outerLimit;

    private boolean hasNextIndicator;

    private final AbstractBlock block;

    public ComparisonIterator(AbstractBlock block) {
        hasNextIndicator = true;
        this.block = block;
        totalComparisons = block.getNoOfComparisons();

        if (block instanceof BilateralBlock) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            innerLoop = -1; // so that counting in function next() starts from 0
            innerLimit = bilBlock.getIndex2Entities().length - 1;
            outerLoop = 0;
            outerLimit = bilBlock.getIndex1Entities().length - 1;
        } else if (block instanceof UnilateralBlock) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            innerLoop = 0;
            innerLimit = uniBlock.getEntities().length - 1;
            outerLoop = 0;
            outerLimit = uniBlock.getEntities().length - 1;
        } else if (block instanceof DecomposedBlock) {
            innerLoop = -1;
            innerLimit = -1;
            outerLoop = -1; // so that counting in function next() starts from 0
            outerLimit = -1;
        }
    }

    @Override
    public boolean hasNext() {
        return executedComparisons < totalComparisons && hasNextIndicator;
    }

    @Override
    public Comparison next() {
        if (totalComparisons <= executedComparisons) {
            System.err.println("All comparisons were already executed!");
            return null;
        }

        executedComparisons++;

        if (block instanceof BilateralBlock) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            innerLoop++;
            if (innerLimit < innerLoop) {
                innerLoop = 0;
                outerLoop++;
                if (outerLimit < outerLoop) {
                    System.err.println("All comparisons were already executed!");
                    return null;
                }
            }

            return new Comparison(true, bilBlock.getIndex1Entities()[outerLoop], bilBlock.getIndex2Entities()[innerLoop]);
        } else if (block instanceof UnilateralBlock) {
            UnilateralBlock uniBlock = (UnilateralBlock) block;
            innerLoop++;
            if (innerLimit < innerLoop) {
                outerLoop++;
                if (outerLimit < outerLoop) {
                    System.err.println("All comparisons were already executed!");
                    return null;
                }
                innerLoop = outerLoop + 1;
            }

            return new Comparison(false, uniBlock.getEntities()[outerLoop], uniBlock.getEntities()[innerLoop]);
        } else if (block instanceof DecomposedBlock) {
            DecomposedBlock deBlock = (DecomposedBlock) block;
            outerLoop++;
            return new Comparison(deBlock.isCleanCleanER(), deBlock.getEntities1()[outerLoop], deBlock.getEntities2()[outerLoop]);
        }
//        else if (block instanceof UnilateralHirearchicalBlock) {
//            return nextHierUni();
//        }
//        else if (block instanceof BilateralHierarchicalBlock) {
//            return nextHierUni();
//        }

        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    public Comparison nextHierUni() {
//        if (!finishedChildrenInner) {
//            // comparisons among chindren
//
//
//        } else if (!finishedChildrenOuter) {
//            // comparisons among children and parent
//        } else {
//            // comparisons inside parent w/o children
//        }
//    }
//
//    public Comparison nextHierBil() {
//
//    }
}
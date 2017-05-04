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

package DataStructures;

import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import Utilities.ComparisonIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author G.A.P. II
 */

public abstract class AbstractBlock implements Serializable {

    private static final long serialVersionUID = 7526443743449L;

    protected double comparisons;
    protected int blockIndex;
    protected double utilityMeasure;

    public AbstractBlock() {
        blockIndex = -1;
        utilityMeasure = -1;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public ComparisonIterator getComparisonIterator() {
        return new ComparisonIterator(this);
    }

    public double getNoOfComparisons() {
        return comparisons;
    }

    public double getUtilityMeasure() {
        return utilityMeasure;
    }

    public double processBlock(AbstractDuplicatePropagation adp) {
        double noOfComparisons = 0;

        ComparisonIterator iterator = getComparisonIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (!adp.isSuperfluous(comparison)) {
                noOfComparisons++;
            }
        }

        return noOfComparisons;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public List<Comparison> getComparisons() {
        final List<Comparison> comparisons = new ArrayList<>();

        ComparisonIterator iterator = getComparisonIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            comparisons.add(comparison);
        }

        return comparisons;
    }

    public abstract double getTotalBlockAssignments();
    public abstract double getAggregateCardinality();
    public abstract void setUtilityMeasure();
}
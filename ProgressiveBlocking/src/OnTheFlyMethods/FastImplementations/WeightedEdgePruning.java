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
package OnTheFlyMethods.FastImplementations;

import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import MetaBlocking.WeightingScheme;
import java.util.List;

/**
 *
 * @author gap2
 */
public class WeightedEdgePruning extends MetaBlocking.FastImplementations.WeightedEdgePruning {

    protected double totalComparisons;
    protected final AbstractDuplicatePropagation duplicatePropagation;

    public WeightedEdgePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        this(adp, "Fast Weighted Edge Pruning", scheme);
    }

    protected WeightedEdgePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme) {
        super(description, scheme);
        duplicatePropagation = adp;
        if (duplicatePropagation != null) {
            duplicatePropagation.resetDuplicates();
        }
        totalComparisons = 0;
    }

    public double[] getPerformance() {
        double[] metrics = new double[3];
        metrics[0] = duplicatePropagation.getNoOfDuplicates() / ((double) duplicatePropagation.getExistingDuplicates()); //PC
        metrics[1] = duplicatePropagation.getNoOfDuplicates() / totalComparisons; //PQ
        metrics[2] = totalComparisons;
        return metrics;
    }

    @Override
    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        if (!cleanCleanER) {
            for (int neighborId : validEntities) {
                double weight = getWeight(entityId, neighborId);
                if (threshold <= weight) {
                    totalComparisons++;
                    duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                }
            }
        } else {
            if (entityId < datasetLimit) {
                for (int neighborId : validEntities) {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        totalComparisons++;
                        duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                    }
                }
            } else {
                for (int neighborId : validEntities) {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        totalComparisons++;
                        duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                    }
                }
            }
        }
    }
}

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

package MetaBlocking.IntegratedMatching;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import MetaBlocking.WeightingScheme;
import Utilities.ProfileComparison;
import java.util.List;

/**
 *
 * @author gap2
 */
public class WeightedNodePruning extends MetaBlocking.WeightedNodePruning {

    private final EntityProfile[] profiles1;
    private final EntityProfile[] profiles2;

    public WeightedNodePruning(List<EntityProfile> pr1, List<EntityProfile> pr2, WeightingScheme scheme) {
        super("Node Pruning with integrated matching", scheme);
        profiles1 = pr1.toArray(new EntityProfile[pr1.size()]);
        if (pr2 == null) {
            profiles2 = null;
        } else {
            profiles2 = pr2.toArray(new EntityProfile[pr2.size()]);
        }
    }

    @Override
    protected void processPartition(int firstId, int lastId, List<AbstractBlock> blocks) {
        for (int i = firstId; i < lastId; i++) {
            final Integer[] neighbors = getAdjacentEntities(i);
            if (neighbors == null) {
                continue;
            }

            double averageWeight = 0;
            final double[] weights = new double[neighbors.length];
            for (int j = 0; j < weights.length; j++) {
                Comparison comparison = getComparison(i, neighbors[j]);
                weights[j] = getWeight(comparison);
                averageWeight += weights[j];
            }
            averageWeight /= weights.length;

            for (int j = 0; j < weights.length; j++) {
                if (averageWeight <= weights[j]) {
                    Comparison comparison = getComparison(i, neighbors[j]);
                    if (cleanCleanER) {
                        ProfileComparison.getJaccardSimilarity(profiles1[comparison.getEntityId1()].getAttributes(),
                                profiles2[comparison.getEntityId2()].getAttributes());
                    } else {
                        ProfileComparison.getJaccardSimilarity(profiles1[comparison.getEntityId1()].getAttributes(),
                                profiles1[comparison.getEntityId2()].getAttributes());
                    }
                }
            }
        }
    }
}

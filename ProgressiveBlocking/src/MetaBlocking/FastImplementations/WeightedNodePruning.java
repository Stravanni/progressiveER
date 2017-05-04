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
package MetaBlocking.FastImplementations;

import DataStructures.AbstractBlock;
import MetaBlocking.WeightingScheme;
import java.util.List;

/**
 *
 * @author gap2
 */
public class WeightedNodePruning extends WeightedEdgePruning {

    protected int firstId;
    protected int lastId;

    public WeightedNodePruning(WeightingScheme scheme) {
        this("Fast Weighted Node Pruning ("+scheme+")", scheme);
    }

    protected WeightedNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
        nodeCentric = true;
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        setLimits();
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = firstId; i < lastId; i++) {
                processArcsEntity(i);
                setThreshold(i);
                verifyValidEntities(i, newBlocks);
            }
        } else {
            for (int i = firstId; i < lastId; i++) {
                processEntity(i);
                setThreshold(i);
                verifyValidEntities(i, newBlocks);
            }
        }
    }

    protected void setLimits() {
        firstId = 0;
        lastId = noOfEntities;
    }

    @Override
    protected void setThreshold() {
    }

    protected void setThreshold(int entityId) {
        threshold = 0;
        for (int neighborId : validEntities) {
            threshold += getWeight(entityId, neighborId);
        }
        threshold /= validEntities.size();
    }
}

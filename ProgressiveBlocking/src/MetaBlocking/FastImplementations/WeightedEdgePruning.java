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
 * @author gap2
 */
public class WeightedEdgePruning extends AbstractFastMetablocking {

    protected double noOfEdges;

    public WeightedEdgePruning(WeightingScheme scheme) {
        this("Fast Weighted Edge Pruning (" + scheme + ")", scheme);
    }

    protected WeightedEdgePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
        nodeCentric = false;
    }

    protected void processArcsEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            double blockComparisons = cleanCleanER ? bBlocks[blockIndex].getNoOfComparisons() : uBlocks[blockIndex].getNoOfComparisons();
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }

                counters[neighborId] += 1 / blockComparisons;
                validEntities.add(neighborId);
            }
        }
    }

    protected void processEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }

                counters[neighborId]++;
                validEntities.add(neighborId);
            }
        }
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        }
    }

    @Override
    protected void setThreshold() {
        noOfEdges = 0;
        threshold = 0;

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                updateThreshold(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                updateThreshold(i);
            }
        }

        threshold /= noOfEdges;
    }

    protected void updateThreshold(int entityId) {
        noOfEdges += validEntities.size();
        for (int neighborId : validEntities) {
            threshold += getWeight(entityId, neighborId);
        }
    }

    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        retainedNeighbors.clear();
        if (!cleanCleanER) {
            for (int neighborId : validEntities) {
                double weight = getWeight(entityId, neighborId);
                if (threshold <= weight) {
                    retainedNeighbors.add(neighborId);
                }
            }
            addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
        } else {
            if (entityId < datasetLimit) {
                for (int neighborId : validEntities) {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        retainedNeighbors.add(neighborId - datasetLimit);
                    }
                }
                addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
            } else {
                for (int neighborId : validEntities) {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        retainedNeighbors.add(neighborId);
                    }
                }
                addReversedDecomposedBlock(entityId - datasetLimit, retainedNeighbors, newBlocks);
            }
        }
    }
}

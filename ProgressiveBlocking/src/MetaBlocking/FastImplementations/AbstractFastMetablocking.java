package MetaBlocking.FastImplementations;

import BlockProcessing.AbstractFastEfficiencyMethod;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.UnilateralBlock;
import MetaBlocking.WeightingScheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author G.A.P. II
 */
public abstract class AbstractFastMetablocking extends AbstractFastEfficiencyMethod {

    protected boolean nodeCentric;

    protected int[] flags;

    protected double threshold = 0;
    protected double blockAssingments;
    protected double aggregateCardinality;
    protected double distinctComparisons;
    protected double[] comparisonsPerEntity;
    protected double[] counters;

    protected final List<Integer> neighbors;
    protected final List<Integer> retainedNeighbors;
    protected WeightingScheme weightingScheme;

    public AbstractFastMetablocking(String nm, WeightingScheme wScheme) {
        super(nm);
        neighbors = new ArrayList<>();
        retainedNeighbors = new ArrayList<>();
        weightingScheme = wScheme;
    }

    protected abstract void pruneEdges(List<AbstractBlock> blocks);

    protected abstract void setThreshold();

    @Override
    protected void applyMainProcessing(List<AbstractBlock> blocks) {
        counters = new double[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        blockAssingments = 0;
        aggregateCardinality = 0;
        if (cleanCleanER) {
            for (BilateralBlock bBlock : bBlocks) {
                blockAssingments += bBlock.getTotalBlockAssignments();
                aggregateCardinality += bBlock.getAggregateCardinality();
            }
        } else {
            for (UnilateralBlock uBlock : uBlocks) {
                blockAssingments += uBlock.getTotalBlockAssignments();
                aggregateCardinality += uBlock.getAggregateCardinality();
            }
        }

        if (weightingScheme.equals(WeightingScheme.EJS)) {
            setStatistics();
        }

        setThreshold();
        pruneEdges(blocks);
    }

    protected void freeMemory() {
        bBlocks = null;
        flags = null;
        counters = null;
        uBlocks = null;
    }

    protected Comparison getComparison(int entityId, int neighborId) {
        if (!cleanCleanER) {
            if (entityId < neighborId) {
                return new Comparison(cleanCleanER, entityId, neighborId);
            } else {
                return new Comparison(cleanCleanER, neighborId, entityId);
            }
        } else {
            if (entityId < datasetLimit) {
                return new Comparison(cleanCleanER, entityId, neighborId - datasetLimit);
            } else {
                return new Comparison(cleanCleanER, neighborId, entityId - datasetLimit);
            }
        }
    }

    protected int[] getNeighborEntities(int blockIndex, int entityId) {
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                return bBlocks[blockIndex].getIndex2Entities();
            } else {
                return bBlocks[blockIndex].getIndex1Entities();
            }
        }
        return uBlocks[blockIndex].getEntities();
    }

    protected double getWeight(int entityId, int neighborId) {
        switch (weightingScheme) {
            case ARCS:
                return counters[neighborId];
            case CBS:
                return counters[neighborId];
            case ECBS:
                return counters[neighborId] * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(entityId, 0)) * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(neighborId, 0));
            case JS:
                return counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
            case EJS:
                double probability = counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
                return probability * Math.log10(distinctComparisons / comparisonsPerEntity[entityId]) * Math.log10(distinctComparisons / comparisonsPerEntity[neighborId]);
        }
        return -1;
    }

    protected void setNormalizedNeighborEntities(int blockIndex, int entityId) {
        neighbors.clear();
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                for (int originalId : bBlocks[blockIndex].getIndex2Entities()) {
                    neighbors.add(originalId + datasetLimit);
                }
            } else {
                for (int originalId : bBlocks[blockIndex].getIndex1Entities()) {
                    neighbors.add(originalId);
                }
            }
        } else {
            if (!nodeCentric) {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId < entityId) {
                        neighbors.add(neighborId);
                    }
                }
            } else {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId != entityId) {
                        neighbors.add(neighborId);
                    }
                }
            }
        }
    }

    protected void setStatistics() {
        distinctComparisons = 0;
        comparisonsPerEntity = new double[noOfEntities];
        final Set<Integer> distinctNeighbors = new HashSet<Integer>();
        for (int i = 0; i < noOfEntities; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                distinctNeighbors.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : getNeighborEntities(blockIndex, i)) {
                        distinctNeighbors.add(neighborId);
                    }
                }
                comparisonsPerEntity[i] = distinctNeighbors.size();
                if (!cleanCleanER) {
                    comparisonsPerEntity[i]--;
                }
                distinctComparisons += comparisonsPerEntity[i];
            }
        }
        distinctComparisons /= 2;
    }
}

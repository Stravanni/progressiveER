package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import DataStructures.AbstractBlock;
import MetaBlocking.FastImplementations.WeightedNodePruning;
import MetaBlocking.WeightingScheme;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class RedundancyWeightedNodePruning extends WeightedNodePruning {

    protected double[] averageWeight;

    public RedundancyWeightedNodePruning(WeightingScheme scheme) {
        this("Redundancy Weighted Node Pruning ("+scheme+")", scheme);
    }

    protected RedundancyWeightedNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }
    
    protected boolean isValidComparison(int entityId, int neighborId) {
        double weight = getWeight(entityId, neighborId);
        boolean inNeighborhood1 = averageWeight[entityId] <= weight;
        boolean inNeighborhood2 = averageWeight[neighborId] <= weight;

        if (inNeighborhood1 || inNeighborhood2) {
            return entityId < neighborId;
        }
        
        return false;
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < noOfEntities; i++) {
                processArcsEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                processEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        }
    }

    @Override
    protected void setThreshold() {
        averageWeight = new double[noOfEntities];
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < noOfEntities; i++) {
                processArcsEntity(i);
                setThreshold(i);
                averageWeight[i] = threshold;
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                processEntity(i);
                setThreshold(i);
                averageWeight[i] = threshold;
            }
        }
    }

    @Override
    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        retainedNeighbors.clear();
        if (!cleanCleanER) {
            for (int neighborId : validEntities) {
                if (isValidComparison(entityId, neighborId)) {
                    retainedNeighbors.add(neighborId);
                }
            }
            addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
        } else {
            if (entityId < datasetLimit) {
                for (int neighborId : validEntities) {
                    if (isValidComparison(entityId, neighborId)) {
                        retainedNeighbors.add(neighborId - datasetLimit);
                    }
                }
                addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
            } else {
                for (int neighborId : validEntities) {
                    if (isValidComparison(entityId, neighborId)) {
                        retainedNeighbors.add(neighborId);
                    }
                }
                addReversedDecomposedBlock(entityId - datasetLimit, retainedNeighbors, newBlocks);
            }
        }
    }
}

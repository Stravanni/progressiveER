package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import MetaBlocking.WeightingScheme;

/**
 *
 * @author G.A.P. II
 */
public class ReciprocalWeightedNodePruning extends RedundancyWeightedNodePruning {

    public ReciprocalWeightedNodePruning(WeightingScheme scheme) {
        super("Reciprocal Weighted Node Pruning ("+scheme+")", scheme);
    }

    @Override
    protected boolean isValidComparison(int entityId, int neighborId) {
        double weight = getWeight(entityId, neighborId);
        boolean inNeighborhood1 = averageWeight[entityId] <= weight;
        boolean inNeighborhood2 = averageWeight[neighborId] <= weight;
        
        if (inNeighborhood1 && inNeighborhood2) {
            return entityId < neighborId;
        }
        
        return false;
    }
}

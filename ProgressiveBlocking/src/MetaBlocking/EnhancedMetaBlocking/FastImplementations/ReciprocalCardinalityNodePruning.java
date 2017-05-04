package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import DataStructures.Comparison;
import MetaBlocking.WeightingScheme;

/**
 *
 * @author G.A.P. II
 */

public class ReciprocalCardinalityNodePruning extends RedundancyCardinalityNodePruning {

    public ReciprocalCardinalityNodePruning(WeightingScheme scheme) {
        super("Fast Reciprocal Cardinality Node Pruning ("+scheme+")", scheme);
    }

    @Override
    protected boolean isValidComparison (int entityId, Comparison comparison) {
        int neighborId = comparison.getEntityId1()==entityId?comparison.getEntityId2():comparison.getEntityId1();
        if (cleanCleanER && entityId < datasetLimit) {
            neighborId += datasetLimit;
        }

        if (nearestEntities[neighborId] == null) {
            return false;
        }

        if (nearestEntities[neighborId].contains(comparison)) {
            return entityId < neighborId;
        }

        return false;
    }
}

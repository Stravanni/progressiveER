package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import MetaBlocking.WeightingScheme;

/**
 *
 * @author G.A.P. II
 */
public class PartitionCardinalityNodePruning extends MetaBlocking.FastImplementations.CardinalityNodePruning {

    public PartitionCardinalityNodePruning(WeightingScheme scheme) {
        super("Fast Partition Cardinality Node Pruning ("+scheme+")", scheme);
    }

    @Override
    protected void setLimits() {
        if (!cleanCleanER) {
            System.err.println("Partition Cardinality Node Pruning does not apply to unilateral block collections!");
            System.exit(-1);
        }

        //choose partition
        firstId = 0;
        lastId = datasetLimit;
        int dataset2Entities = noOfEntities-datasetLimit;
        if (dataset2Entities < datasetLimit) {
            //smaller partition
            firstId = datasetLimit;
            lastId = noOfEntities;
        }
    }
}

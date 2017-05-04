package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import MetaBlocking.WeightingScheme;

/**
 *
 * @author G.A.P. II
 */
public class PartitionWeightedNodePruning extends MetaBlocking.FastImplementations.WeightedNodePruning {

    public PartitionWeightedNodePruning(WeightingScheme scheme) {
        super("Fast Partition Weighted Node Pruning ("+scheme+")", scheme);
    }
    
    @Override
    protected void setLimits() {
        if (!cleanCleanER) {
            System.err.println("Partition Weighted Node Pruning does not apply to unilateral block collections!");
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

package OnTheFlyMethods.FastImplementations;

import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import MetaBlocking.WeightingScheme;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class RedundancyWeightedNodePruning extends MetaBlocking.EnhancedMetaBlocking.FastImplementations.RedundancyWeightedNodePruning {

    protected double totalComparisons;
    protected final AbstractDuplicatePropagation duplicatePropagation;

    public RedundancyWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        this(adp, "Redundancy Weighted Node Pruning ("+scheme+")", scheme);
    }

    protected RedundancyWeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme) {
        super(description, scheme);
        duplicatePropagation = adp;
        duplicatePropagation.resetDuplicates();
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
                if (isValidComparison(entityId, neighborId)) {
                    totalComparisons++;
                    duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                }
            }
        } else {
            if (entityId < datasetLimit) {
                for (int neighborId : validEntities) {
                    if (isValidComparison(entityId, neighborId)) {
                        totalComparisons++;
                        duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                    }
                }
            } else {
                for (int neighborId : validEntities) {
                    if (isValidComparison(entityId, neighborId)) {
                        totalComparisons++;
                        duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                    }
                }
            }
        }
    }
}

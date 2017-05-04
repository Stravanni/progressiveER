package ProgressiveSortedNeighborhood;

import DataStructures.Comparison;
import DataStructures.EntityProfile;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gap2
 */

public class LocalNcfWeightedProgressiveSnIterator extends LocalAcfWeightedProgressiveSnIterator implements Iterator<Comparison> {

    /* Ncf stands for Normalized Co-occurrence Frequency */
    
    public LocalNcfWeightedProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);
    }
    
    @Override
    protected double getWeight(int entityId1, int entityId2, int coOccurrenceFreq) {
        double denominator = sPositionIndex.getEntityPositions(entityId1).length+sPositionIndex.getEntityPositions(entityId2).length-coOccurrenceFreq;
        return coOccurrenceFreq/denominator;
    }
}

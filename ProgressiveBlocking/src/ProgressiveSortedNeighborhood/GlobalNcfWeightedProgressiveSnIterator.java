package ProgressiveSortedNeighborhood;

import DataStructures.Comparison;
import DataStructures.EntityProfile;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gap2
 */
public class GlobalNcfWeightedProgressiveSnIterator extends GlobalAcfWeightedProgressiveSnIterator implements Iterator<Comparison> {

    /* Ncf stands for Normalized Co-occurrence Frequency */
    
    public GlobalNcfWeightedProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);
    }

    @Override
    protected double getWeight(int entityId, int neighborId) {
        double denominator = sPositionIndex.getEntityPositions(entityId).length+sPositionIndex.getEntityPositions(neighborId).length-counters[neighborId];
        return counters[neighborId]/denominator;
    }
}

package ProgressiveSortedNeighborhood;

import DataStructures.Comparison;
import DataStructures.EntityProfile;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gap2
 */

public class GlobalIdWeightedProgressiveSnIterator extends GlobalAcfWeightedProgressiveSnIterator implements Iterator<Comparison> {

    /* Id stands for Inverse Distance */
    
    public GlobalIdWeightedProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);
    }

    @Override
    protected void updateLocalWeight(int neighborId, int window) {
        counters[neighborId]+= 1.0/window;
        distinctNeighbors.add(neighborId);
    }
}
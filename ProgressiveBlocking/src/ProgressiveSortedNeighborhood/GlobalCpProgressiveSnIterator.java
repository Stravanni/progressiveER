package ProgressiveSortedNeighborhood;

import DataStructures.Comparison;
import DataStructures.EntityProfile;
import ProgressiveSortedNeighborhood.DataStructures.SimplePositionIndex;

import java.util.List;

/**
 *
 * @author gap2
 */

public class GlobalCpProgressiveSnIterator extends NaiveProgressiveSnIterator {
    
    protected final SimplePositionIndex sPositionIndex;

    public GlobalCpProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);

        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);
    }

    @Override
    public Comparison next() {
        Comparison newComp;
        do {
            newComp = super.next();
        } while (isRedundant());
        return newComp;
    }

    protected boolean isRedundant() {
        int[] positions1 = sPositionIndex.getEntityPositions(sortedEntities[currentPosition]);
        int[] positions2 = sPositionIndex.getEntityPositions(sortedEntities[currentPosition + currentWindow]);
        if (positions2.length < positions1.length) {
            int[] temp = positions1;
            positions1 = positions2;
            positions2 = temp;
        }
        
        for (int p1 : positions1) {
            for (int p2 : positions2) {
                if (Math.abs(p1 - p2) < currentWindow) {
                    return true;
                } 
                
                if (p1 < p2) {
                    break;
                }
            }
        }
        
        for (int p1 : positions1) {
            for (int p2 : positions2) {
                if (Math.abs(p1 - p2) == currentWindow) {
                    return Math.min(p1, p2) < currentPosition;
                }
                
                if (p1 < p2) {
                    break;
                }
            }
        }

        return false;
    }
}